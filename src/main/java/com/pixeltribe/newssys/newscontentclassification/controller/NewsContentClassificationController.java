package com.pixeltribe.newssys.newscontentclassification.controller;

import com.pixeltribe.newssys.newscontentclassification.model.NewsContentClassificationCreationDTO;
import com.pixeltribe.newssys.newscontentclassification.model.NewsContentClassificationService;
import com.pixeltribe.newssys.newscontentclassification.model.NewsContentClassificationUpdateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
public class NewsContentClassificationController {

    private final NewsContentClassificationService svc;

    public NewsContentClassificationController(NewsContentClassificationService svc) {
        this.svc = svc;

    }

    /* 建立 */
    @PostMapping("classification/add")
    public NewsContentClassificationUpdateDTO create(
            @RequestBody @Valid NewsContentClassificationCreationDTO dto) {
        return svc.create(dto);
    }

    /* 查詢 */
    @GetMapping("classification")
    public List<NewsContentClassificationUpdateDTO> list(
            @RequestParam Integer newsId) {
        return svc.list(newsId);
    }

    /* 更新 */
    @PutMapping("classification/{id}")
    public NewsContentClassificationUpdateDTO update(
            @PathVariable Integer id,
            @RequestBody @Valid NewsContentClassificationUpdateDTO dto) {
        dto.setId(id);                       // 路徑變數覆寫，避免前端亂填
        return svc.update(dto);
    }

    /* 刪除 */
    @DeleteMapping("classification/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        svc.delete(id);
    }


}
