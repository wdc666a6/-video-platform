package com.example.videoplatform.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

@RestController
@RequestMapping("/api/image")
public class ImageProxyController {

    private static final RestTemplate restTemplate = new RestTemplate();

    /**
     * 图片代理接口 - 绕过豆瓣防盗链
     * GET /api/image/proxy?url=xxx
     */
    @GetMapping("/proxy")
    public void proxyImage(@RequestParam String url, HttpServletResponse response) throws IOException {
        try {
            if (url == null || url.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            // 构建请求头，模拟浏览器访问
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            headers.set("Referer", "https://movie.douban.com/");
            headers.set("Accept", "image/webp,image/apng,image/*,*/*;q=0.8");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // 请求图片
            ResponseEntity<byte[]> imgResponse = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                byte[].class
            );

            if (imgResponse.getStatusCode() == HttpStatus.OK) {
                byte[] imageBytes = imgResponse.getBody();

                // 设置响应头
                response.setContentType(imgResponse.getHeaders().getContentType() != null ?
                    imgResponse.getHeaders().getContentType().toString() : "image/jpeg");
                response.setContentLength(imageBytes != null ? imageBytes.length : 0);

                // 设置缓存 - 减少缓存时间以便及时更新封面
                response.setHeader("Cache-Control", "public, max-age=300"); // 缓存5分钟
                response.setHeader("Access-Control-Allow-Origin", "*");

                // 写入图片数据
                try (OutputStream os = response.getOutputStream()) {
                    os.write(imageBytes);
                    os.flush();
                }
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            System.err.println("图片代理失败: " + url + ", " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
