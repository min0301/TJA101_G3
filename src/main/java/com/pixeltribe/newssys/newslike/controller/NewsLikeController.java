package com.pixeltribe.newssys.newslike.controller;

import com.pixeltribe.newssys.newslike.model.NewsLikeDTO;
import com.pixeltribe.newssys.newslike.model.NewsLikeService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class NewsLikeController {

    private final NewsLikeService newsLikeService;

    public NewsLikeController(NewsLikeService newsLikeService) {
        this.newsLikeService = newsLikeService;
    }

    @GetMapping("/admin/AllNewsLike")
    @Operation(summary = "取得所有讚/倒讚/中立")
    public List<NewsLikeDTO> getAllNewsLike() {
        return newsLikeService.getAllNewsLike();
    }

    @GetMapping("/admin/AllNewsLikeByComment/{id}")
    @Operation(summary = "取得某個新聞評論的讚/倒讚/中立")
    public List<NewsLikeDTO> getAllNewsLikeByComment(@PathVariable Integer id) {
        return newsLikeService.getAllNewsLikeByComment(id);
    }

    @GetMapping("/admin/AllNewsLikeByMember/{id}")
    @Operation(summary = "取得某個會員的讚/倒讚/中立")
    public List<NewsLikeDTO> getAllNewsLikeByMember(@PathVariable Integer id) {
        return newsLikeService.getAllNewsLikeByMember(id);
    }

    @GetMapping("/NewsLikeByMember")
    @Operation(summary = "獲得某會員對某新聞的讚/倒讚")
    public ResponseEntity<NewsLikeDTO> getAllNewsLikeByMember(@RequestParam Integer memNoId,
                                                    @RequestParam Integer ncomNoId) {

        NewsLikeDTO dto = newsLikeService.getUserLikeStatus(memNoId, ncomNoId);
        return ResponseEntity.ok(dto);
    }


    @PostMapping("/NewsLike/add")
    @Operation(summary = "新增/修改評論讚/倒讚/中立")
    public NewsLikeDTO addNewsLike(@RequestBody NewsLikeDTO newsLikeDTO) {
        Integer commendId = newsLikeDTO.getNcomNoId();
        Integer memberId = newsLikeDTO.getMemNoId();
        Character status = newsLikeDTO.getNlikeStatus();

        return newsLikeService.addNewsLike(commendId,memberId,status);
    }

}
