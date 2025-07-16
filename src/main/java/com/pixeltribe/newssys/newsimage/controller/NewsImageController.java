package com.pixeltribe.newssys.newsimage.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pixeltribe.newssys.newsimage.model.NewsImageDTO;
import com.pixeltribe.newssys.newsimage.model.NewsImageIndexDTO;
import com.pixeltribe.newssys.newsimage.model.NewsImageService;
import com.pixeltribe.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/news/image")
public class NewsImageController {

    private final NewsImageService newsImageSrv;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;


    public NewsImageController(NewsImageService newsImageSrv, RedisTemplate<String, Object> redisTemplate, JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.newsImageSrv = newsImageSrv;
        this.redisTemplate = redisTemplate;
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
    }


    @GetMapping("News/{id}")
    @Operation(summary = "依新聞取得所有圖片url")
    public List<NewsImageDTO> newsImageList(@PathVariable Integer id) {
        return newsImageSrv.getNewsImage(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "取得某個圖片")
    public List<NewsImageDTO> getImage(@Valid @PathVariable Integer id) {

        return newsImageSrv.findById(id);
    }

    @PostMapping(value = "/upload/{newsId}", consumes = "multipart/form-data")
    @Operation(summary = "上傳圖片並關聯新聞及回傳圖片 URL（供富文本編輯器插入)")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @PathVariable Integer newsId) {
        String url = newsImageSrv.uploadAndGetUrl(file, newsId);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @PostMapping("/temp-image")
    @Operation(summary = "暫存圖片到redis")
    public ResponseEntity<Map<String, String>> uploadTempImage(@RequestParam MultipartFile file,
                                                               HttpServletRequest req) {
        String filename = UUID.randomUUID()+"-"+file.getOriginalFilename();
        Path path =  Paths.get("uploads",filename);
        try {
            file.transferTo(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String imageUrl = "/uploads/" +filename;
        String mime = file.getContentType();
        Integer adminId = jwtUtil.getAdminIdFromJWT(req);

        Map<String, String> imgInfo = Map.of(
                "url",imageUrl,
                "mime",mime,
                "filename",filename
        );

        if (mime == null || !mime.startsWith("image/")) {
            return ResponseEntity.status(400)
                    .body(Map.of("errorCode", "IMG_400", "errorMessage", "僅允許上傳圖片"));
        }

        String redisKey = "temp_news_images:admin:" + adminId;
        try {
            redisTemplate.opsForList()
                    .rightPush(redisKey, objectMapper.writeValueAsString(imgInfo));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.ok(Map.of("url", imageUrl));
    }

    @GetMapping("index")
    @Operation(summary = "給首頁5張最新新聞圖片")
    public List<NewsImageIndexDTO> getImage5() {
        return newsImageSrv.getImage5();
    }

}
