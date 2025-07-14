package com.pixeltribe.forumsys.postcollect.controller;

import org.springframework.web.bind.annotation.*;

import com.pixeltribe.forumsys.postcollect.model.PostCollectDto;
import com.pixeltribe.forumsys.postcollect.model.PostCollectService;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@RestController
@RequestMapping("/api/post-collect")
public class PostCollectController {

 @Autowired
 private PostCollectService postCollectService;

 @GetMapping("/member/{id}")
 public List<PostCollectDto> getPostCollections(@PathVariable Integer memberId) {
     return postCollectService.getPostCollectionsByMemberId(memberId);
 }
}