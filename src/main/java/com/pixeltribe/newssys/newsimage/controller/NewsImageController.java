package com.pixeltribe.newssys.newsimage.controller;

import com.pixeltribe.newssys.newsimage.model.NewsImage;
import com.pixeltribe.newssys.newsimage.model.NewsImageDTO;
import com.pixeltribe.newssys.newsimage.model.NewsImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/news/image")
class NewsImageController {

    @Autowired
    NewsImageService newsImageSrv;

    @GetMapping("{id}/NewsImage")
    public List<NewsImageDTO> newsImageList(@PathVariable int id) {
        return newsImageSrv.getNewsImage(id);
    }

    @GetMapping("{id}")
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

}
