package com.example.videoplatform.service;

import com.example.videoplatform.entity.Video;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.json.JSONObject;
import org.json.JSONArray;
import java.net.URLEncoder;
import java.net.InetSocketAddress;
import java.net.Proxy;

@Service
public class PosterService {

    private final RestTemplate restTemplate;

    @Autowired
    private CoverImageService coverImageService;

    public CoverImageService getCoverImageService() {
        return coverImageService;
    }

    // TMDB API - 需要有效的API密钥，请到 https://www.themoviedb.org/settings/api 申请
    // 免费且支持中文内容，是获取中文影视海报的最佳选择
    private static final String TMDB_API_KEY = "1e73364933f272b2c203242f94bc0997";  // 请填入你的TMDB API密钥
    private static final String TMDB_SEARCH_URL = "https://api.themoviedb.org/3/search/%s?api_key=%s&query=%s&language=zh-CN";
    private static final String TMDB_BASE_URL = "https://image.tmdb.org/t/p/original";

    // TVMaze API - 免费且稳定的电视剧数据库（仅英文内容）
    private static final String TVMAZE_SEARCH_URL = "https://api.tvmaze.com/search/shows?q=%s";

    // OMDb API - 需要有效的API密钥，请到 http://www.omdbapi.com/apikey.aspx 申请
    // 免费版每天1000次请求，仅支持英文内容
    private static final String OMDB_API_KEY = "";  // 请填入你的OMDB API密钥

    public PosterService() {
        // 配置 RestTemplate 使用系统代理和增加超时时间
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);  // 10秒连接超时
        factory.setReadTimeout(15000);     // 15秒读取超时

        // 尝试使用系统代理（如果设置了）
        String httpProxy = System.getenv("http_proxy");
        String httpsProxy = System.getenv("https_proxy");
        String proxyUrl = (httpsProxy != null) ? httpsProxy : httpProxy;

        if (proxyUrl != null && !proxyUrl.isEmpty()) {
            try {
                // 解析代理地址，格式如 http://127.0.0.1:7890
                java.net.URI proxyUri = java.net.URI.create(proxyUrl);
                String host = proxyUri.getHost();
                int port = proxyUri.getPort();
                if (host != null && port > 0) {
                    Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
                    factory.setProxy(proxy);
                    System.out.println("PosterService 使用代理: " + host + ":" + port);
                }
            } catch (Exception e) {
                System.err.println("代理配置失败，使用直连: " + e.getMessage());
            }
        }

        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * 根据标题搜索获取海报 - 多源回退机制
     * 优先级: TMDB(需API密钥,中文支持最好) -> OMDB(需API密钥) -> TVMaze(仅剧集) -> 默认海报
     * 注意: Jikan API已移除，因为它是动漫专用数据库，会错误匹配非动漫内容
     * @param title 影视标题
     * @param isTV 是否是电视剧 (false=电影)
     * @return 海报URL，失败返回null
     */
    public String fetchPosterFromDouban(String title, boolean isTV) {
        try {
            System.out.println("===== 开始获取海报: " + title + " =====");
            System.out.println("类型: " + (isTV ? "剧集" : "电影"));

            // 优先尝试 TMDB（如果有API密钥）
            if (!TMDB_API_KEY.isEmpty()) {
                System.out.println("[1/3] 尝试 TMDB API (中文支持最好)...");
                String poster = fetchFromTMDB(title, isTV);
                if (poster != null) {
                    System.out.println("✓ TMDB 成功: " + poster);
                    return poster;
                }
                System.out.println("✗ TMDB 失败，回退到下一源");
            } else {
                System.out.println("[1/3] 跳过 TMDB (未配置API密钥 - 请到 https://www.themoviedb.org/settings/api 申请免费密钥)");
            }

            // 尝试 OMDB（如果有API密钥）
            if (!OMDB_API_KEY.isEmpty()) {
                System.out.println("[2/3] 尝试 OMDB API...");
                String poster = fetchFromOMDB(title, isTV);
                if (poster != null) {
                    System.out.println("✓ OMDB 成功: " + poster);
                    return poster;
                }
                System.out.println("✗ OMDB 失败，回退到下一源");
            } else {
                System.out.println("[2/3] 跳过 OMDB (未配置API密钥 - 请到 http://www.omdbapi.com/apikey.aspx 申请免费密钥)");
            }

            // 最后尝试 TVMaze（仅对剧集）
            if (isTV) {
                System.out.println("[3/3] 尝试 TVMaze API...");
                String poster = fetchFromTVMaze(title);
                if (poster != null) {
                    System.out.println("✓ TVMaze 成功: " + poster);
                    return poster;
                }
                System.out.println("✗ TVMaze 失败");
            }

            System.out.println("✗ 所有API源均失败");
            return null;

        } catch (Exception e) {
            System.err.println("获取海报异常: " + title + ", " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从 TMDB 获取海报（推荐 - 支持中文）
     */
    private String fetchFromTMDB(String title, boolean isTV) {
        try {
            String type = isTV ? "tv" : "movie";
            String url = String.format(TMDB_SEARCH_URL, type, TMDB_API_KEY, URLEncoder.encode(title, "UTF-8"));
            System.out.println("TMDB URL: " + url);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0");
            headers.set("Accept", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);

            String body = response.getBody();
            System.out.println("TMDB 响应状态: " + response.getStatusCode());

            if (body != null && !body.isEmpty()) {
                JSONObject json = new JSONObject(body);

                JSONArray results = json.optJSONArray("results");
                int resultCount = (results != null) ? results.length() : 0;
                System.out.println("TMDB 结果数量: " + resultCount);

                if (results != null && results.length() > 0) {
                    JSONObject first = results.getJSONObject(0);
                    String posterPath = first.optString("poster_path", null);

                    if (posterPath != null && !posterPath.isEmpty()) {
                        String fullUrl = TMDB_BASE_URL + posterPath;
                        System.out.println("✓ TMDB 获取到海报: " + title + " -> " + fullUrl);
                        return fullUrl;
                    } else {
                        System.out.println("TMDB 结果无 poster_path 字段");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("TMDB 获取失败: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * 从 OMDB 获取海报（回退源 - 英文内容较好）
     */
    private String fetchFromOMDB(String title, boolean isTV) {
        try {
            String type = isTV ? "series" : "movie";
            String url = String.format(
                "https://www.omdbapi.com/?s=%s&type=%s&apikey=%s",
                URLEncoder.encode(title, "UTF-8"), type, OMDB_API_KEY
            );
            System.out.println("OMDB URL: " + url);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);

            String body = response.getBody();
            System.out.println("OMDB 响应状态: " + response.getStatusCode());

            if (body != null && !body.isEmpty()) {
                JSONObject json = new JSONObject(body);

                if (json.has("Response") && "True".equals(json.getString("Response"))) {
                    JSONArray search = json.getJSONArray("Search");
                    System.out.println("OMDB 搜索结果数量: " + search.length());

                    if (search != null && search.length() > 0) {
                        JSONObject first = search.getJSONObject(0);
                        String poster = first.optString("Poster", null);

                        if (poster != null && !poster.isEmpty() && !"N/A".equals(poster)) {
                            System.out.println("✓ OMDB 获取到海报: " + title);
                            return poster;
                        }
                    }
                } else {
                    System.out.println("OMDB 响应: " + json.optString("Error", "Unknown error"));
                }
            }
        } catch (Exception e) {
            System.err.println("OMDB 获取失败: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * 从 TVMaze 获取海报（最后回退源 - 仅限剧集）
     */
    private String fetchFromTVMaze(String title) {
        try {
            String url = String.format(TVMAZE_SEARCH_URL, URLEncoder.encode(title, "UTF-8"));
            System.out.println("TVMaze URL: " + url);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0");
            headers.set("Accept", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);

            String body = response.getBody();
            System.out.println("TVMaze 响应状态: " + response.getStatusCode());

            if (body != null && !body.isEmpty()) {
                JSONArray array = new JSONArray(body);
                System.out.println("TVMaze 结果数量: " + array.length());

                if (array.length() > 0) {
                    JSONObject first = array.getJSONObject(0);
                    JSONObject show = first.getJSONObject("show");
                    JSONObject image = show.optJSONObject("image");

                    if (image != null) {
                        String originalImage = image.optString("original", null);
                        if (originalImage != null && !originalImage.isEmpty()) {
                            System.out.println("✓ TVMaze 获取到海报: " + title);
                            return originalImage;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("TVMaze 获取失败: " + e.getClass().getName() + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * 为视频对象自动获取并设置封面
     */
    public void fetchAndSetPoster(Video video) {
        if (video == null || video.getTitle() == null) {
            return;
        }

        boolean needsFetch = video.getCoverUrl() == null ||
            video.getCoverUrl().isEmpty() ||
            video.getCoverUrl().contains("placeholder.com") ||
            video.getCoverUrl().contains("example.com/poster") ||
            video.getCoverUrl().contains("example.com/video");

        if (needsFetch) {
            String poster = fetchPosterFromDouban(
                video.getTitle(),
                "SERIES".equals(video.getType())
            );
            if (poster != null && !poster.isEmpty()) {
                video.setCoverUrl(poster);

                // 下载封面到本地
                String localPath = coverImageService.downloadCover(poster, video.getId(), video.getTitle());
                if (localPath != null) {
                    video.setLocalCoverPath(localPath);
                }
            }
        } else if (video.getCoverUrl() != null && !video.getCoverUrl().isEmpty() &&
                   !video.getCoverUrl().startsWith("/covers/") &&
                   (video.getLocalCoverPath() == null || video.getLocalCoverPath().isEmpty())) {
            // 如果已有外部URL但没有本地封面，尝试下载
            String localPath = coverImageService.downloadCover(video.getCoverUrl(), video.getId(), video.getTitle());
            if (localPath != null) {
                video.setLocalCoverPath(localPath);
            }
        }
    }

    /**
     * 批量为视频列表获取海报
     */
    public void batchFetchPosters(java.util.List<Video> videos) {
        if (videos == null || videos.isEmpty()) {
            return;
        }

        for (Video video : videos) {
            try {
                fetchAndSetPoster(video);
                Thread.sleep(300);
            } catch (Exception e) {
                System.err.println("批量获取海报失败: " + video.getTitle());
            }
        }
    }

    /**
     * 仅为已有外部URL的视频下载封面到本地
     */
    public void downloadExistingCovers(java.util.List<Video> videos) {
        if (videos == null || videos.isEmpty()) {
            return;
        }

        for (Video video : videos) {
            try {
                if (video.getCoverUrl() != null && !video.getCoverUrl().isEmpty() &&
                    !video.getCoverUrl().startsWith("/covers/") &&
                    (video.getLocalCoverPath() == null || video.getLocalCoverPath().isEmpty())) {

                    String localPath = coverImageService.downloadCover(video.getCoverUrl(), video.getId(), video.getTitle());
                    if (localPath != null) {
                        video.setLocalCoverPath(localPath);
                    }
                }
                Thread.sleep(200);
            } catch (Exception e) {
                System.err.println("批量下载封面失败: " + video.getTitle());
            }
        }
    }
}
