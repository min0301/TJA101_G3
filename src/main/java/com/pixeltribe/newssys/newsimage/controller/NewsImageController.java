package com.pixeltribe.newssys.newsimage.controller;

import com.pixeltribe.newssys.newsimage.model.NewsImageDTO;
import com.pixeltribe.newssys.newsimage.model.NewsImageService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/news/image")
public class NewsImageController {

    private final NewsImageService newsImageSrv;

    public NewsImageController(NewsImageService newsImageSrv) {
        this.newsImageSrv = newsImageSrv;
    }


    @GetMapping("News/{id}")
    @Operation(summary = "依新聞取得所有圖片url")
    public List<NewsImageDTO> newsImageList(@PathVariable Integer id) {
        return newsImageSrv.getNewsImage(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "取得某個圖片")
    public List<NewsImageDTO> getImage(@PathVariable Integer id) {

        return newsImageSrv.findById(id);
    }

    @PostMapping(value = "/upload/{newsId}" , consumes = "multipart/form-data")
    @Operation(summary = "上傳圖片並關聯新聞及回傳圖片 URL（供富文本編輯器插入)")
    public ResponseEntity<Map<String,String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @PathVariable Integer newsId) {
        String url = newsImageSrv.uploadAndGetUrl(file,newsId);
        return ResponseEntity.ok(Map.of("url",url));
    }

}
