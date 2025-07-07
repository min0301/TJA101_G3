package com.pixeltribe.newssys.newscomment.controller;

import com.pixeltribe.newssys.newscomment.model.NewsComment;
import com.pixeltribe.newssys.newscomment.model.NewsCommentCreationDTO;
import com.pixeltribe.newssys.newscomment.model.NewsCommentDTO;
import com.pixeltribe.newssys.newscomment.model.NewsCommentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
class NewsCommentController {

    @Autowired
    NewsCommentService newsCommentService;

    @GetMapping("NewsComment/{id}")
    public List<NewsCommentDTO> findAll(@PathVariable Integer id){
        return newsCommentService.findAll(id);
    }

    @PostMapping("NewsComment/add")
    public NewsCommentCreationDTO addComment(
            @Valid @RequestBody NewsCommentCreationDTO dto){

        return newsCommentService.add(
                dto.getNewsNoId(),
                dto.getMemNoId(),
                dto.getNcomCon()
        );
    }


}
