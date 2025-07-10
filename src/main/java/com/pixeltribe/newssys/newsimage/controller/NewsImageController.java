package com.pixeltribe.newssys.newsimage.controller;

import com.pixeltribe.newssys.newsimage.model.NewsImage;
import com.pixeltribe.newssys.newsimage.model.NewsImageDTO;
import com.pixeltribe.newssys.newsimage.model.NewsImageService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/news/image")
public class NewsImageController {

    private final NewsImageService newsImageSrv;

    public NewsImageController(NewsImageService newsImageSrv) {
        this.newsImageSrv = newsImageSrv;
    }


    @GetMapping("{id}")
    @Operation(summary = "取得某個圖片")
    public List<NewsImageDTO> newsImageList(@PathVariable int id) {
        return newsImageSrv.getNewsImage(id);
    }

    @GetMapping("/News/{id}")
    @Operation(summary = "某個新聞的所有圖片url")
    public ResponseEntity<byte[]> getImage(@PathVariable int id) {
        NewsImage img = newsImageSrv.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        MediaType mediaType = switch (img.getImgType()) {
            case "image/png" -> MediaType.IMAGE_JPEG;
            case "image/jpeg" -> MediaType.IMAGE_JPEG;
            case "image/gif" -> MediaType.IMAGE_GIF;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
        return ResponseEntity.ok().contentType(mediaType).body(img.getImgData());
    }

//    @PostMapping("/upload")
//    public ResponseEntity<NewsImageDTO> uploadImage(@RequestParam("file") MultipartFile file) {
//        return ResponseEntity.ok().contentType(MediaType.asMediaType())
//    }

}
