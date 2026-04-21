package com.example.videoplatform.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.util.UUID;
import javax.net.ssl.*;

@Service
public class CoverImageService {

    private final RestTemplate restTemplate;
    private static final String COVERS_DIR = "covers";
    private String coversPath;

    public CoverImageService() {
        // 配置 RestTemplate 增加超时时间
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);  // 10秒连接超时
        factory.setReadTimeout(30000);     // 30秒读取超时（下载图片可能需要更长时间）

        // 禁用SSL验证（某些图片服务器可能使用自签名证书）
        disableSSLVerification();

        this.restTemplate = new RestTemplate(factory);
    }

    @PostConstruct
    public void init() {
        // 确保封面目录存在
        coversPath = System.getProperty("user.dir") + File.separator + COVERS_DIR;
        File dir = new File(coversPath);
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.println("创建封面目录: " + coversPath);
        }
    }

    /**
     * 从URL下载封面图片并保存到本地
     * @param imageUrl 图片URL
     * @param videoId 视频ID（用于生成文件名）
     * @param title 视频标题（用于生成文件名）
     * @return 本地封面路径（相对路径，如 /covers/xxx.jpg），失败返回null
     */
    public String downloadCover(String imageUrl, Long videoId, String title) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            System.err.println("封面URL为空，跳过下载");
            return null;
        }

        // 检查URL是否为本地路径（已经是本地封面）
        if (imageUrl.startsWith("/covers/")) {
            System.out.println("封面已是本地路径: " + imageUrl);
            return imageUrl;
        }

        try {
            System.out.println("开始下载封面: " + imageUrl);

            // 生成文件名
            String extension = getExtensionFromUrl(imageUrl);
            String fileName = generateFileName(videoId, title, extension);
            String localFilePath = coversPath + File.separator + fileName;

            // 如果文件已存在，直接返回
            File existingFile = new File(localFilePath);
            if (existingFile.exists()) {
                System.out.println("封面文件已存在，跳过下载: " + fileName);
                return "/covers/" + fileName;
            }

            // 方式1: 尝试使用 RestTemplate（适合大多数HTTP服务器）
            byte[] imageBytes = downloadWithRestTemplate(imageUrl);

            // 方式2: 如果 RestTemplate 失败，尝试使用原生 HttpURLConnection
            if (imageBytes == null) {
                imageBytes = downloadWithHttpURLConnection(imageUrl);
            }

            if (imageBytes != null && imageBytes.length > 0) {
                // 保存图片到本地
                try (FileOutputStream fos = new FileOutputStream(localFilePath)) {
                    fos.write(imageBytes);
                }

                System.out.println("封面下载成功: " + fileName + " (大小: " + imageBytes.length + " bytes)");
                return "/covers/" + fileName;
            } else {
                System.err.println("下载的图片数据为空");
                return null;
            }

        } catch (Exception e) {
            System.err.println("下载封面失败: " + imageUrl + ", 错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 使用 RestTemplate 下载图片
     */
    private byte[] downloadWithRestTemplate(String imageUrl) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            headers.set("Referer", getRefererFromUrl(imageUrl));
            headers.set("Accept", "image/webp,image/apng,image/*,*/*;q=0.8");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                imageUrl,
                HttpMethod.GET,
                entity,
                byte[].class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
        } catch (Exception e) {
            System.err.println("RestTemplate 下载失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 使用原生 HttpURLConnection 下载图片（作为备用方案）
     */
    private byte[] downloadWithHttpURLConnection(String urlString) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            connection.setRequestProperty("Referer", getRefererFromUrl(urlString));
            connection.setRequestProperty("Accept", "image/webp,image/apng,image/*,*/*;q=0.8");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream is = connection.getInputStream()) {
                    return is.readAllBytes();
                }
            }
        } catch (Exception e) {
            System.err.println("HttpURLConnection 下载失败: " + e.getMessage());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    /**
     * 从URL中提取图片扩展名
     */
    private String getExtensionFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return ".jpg";
        }

        // 去除查询参数
        String cleanUrl = url.split("\\?")[0];

        // 提取扩展名
        int lastDot = cleanUrl.lastIndexOf('.');
        if (lastDot > 0 && lastDot < cleanUrl.length() - 1) {
            String ext = cleanUrl.substring(lastDot).toLowerCase();
            // 检查是否为常见图片格式
            if (ext.equals(".jpg") || ext.equals(".jpeg") || ext.equals(".png") ||
                ext.equals(".webp") || ext.equals(".gif") || ext.equals(".bmp")) {
                return ext;
            }
        }

        // 默认使用 .jpg
        return ".jpg";
    }

    /**
     * 生成文件名（使用视频ID和标题的拼音首字母或UUID）
     */
    private String generateFileName(Long videoId, String title, String extension) {
        if (videoId != null) {
            return videoId + extension;
        }

        // 使用标题的哈希值生成文件名
        int hashValue = title != null ? title.hashCode() : UUID.randomUUID().toString().hashCode();
        return "cover_" + Math.abs(hashValue) + extension;
    }

    /**
     * 根据图片URL获取合适的Referer
     */
    private String getRefererFromUrl(String imageUrl) {
        if (imageUrl.contains("tmdb.org")) {
            return "https://www.themoviedb.org/";
        } else if (imageUrl.contains("douban.com")) {
            return "https://movie.douban.com/";
        } else if (imageUrl.contains("tvmaze.com")) {
            return "https://www.tvmaze.com/";
        }
        return imageUrl;
    }

    /**
     * 禁用SSL验证（仅用于下载图片，不推荐用于其他场景）
     */
    private void disableSSLVerification() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {
            System.err.println("禁用SSL验证失败: " + e.getMessage());
        }
    }

    /**
     * 删除本地封面文件
     */
    public boolean deleteCover(String localCoverPath) {
        if (localCoverPath == null || localCoverPath.isEmpty()) {
            return false;
        }

        try {
            // 从相对路径获取完整路径
            String fileName = localCoverPath.replace("/covers/", "");
            File file = new File(coversPath + File.separator + fileName);

            if (file.exists()) {
                file.delete();
                System.out.println("删除封面文件: " + fileName);
                return true;
            }
        } catch (Exception e) {
            System.err.println("删除封面失败: " + e.getMessage());
        }
        return false;
    }

    /**
     * 获取封面目录路径
     */
    public String getCoversPath() {
        return coversPath;
    }

    /**
     * 获取封面URL（优先使用本地路径）
     */
    public String getCoverUrl(String localCoverPath, String originalUrl) {
        if (localCoverPath != null && !localCoverPath.isEmpty()) {
            return localCoverPath;
        }
        return originalUrl;
    }
}
