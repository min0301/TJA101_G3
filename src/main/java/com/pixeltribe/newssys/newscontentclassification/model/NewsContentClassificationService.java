package com.pixeltribe.newssys.newscontentclassification.model;

import com.pixeltribe.newssys.news.model.News;
import com.pixeltribe.newssys.news.model.NewsRepository;
import com.pixeltribe.newssys.newscategory.model.NewsCategory;
import com.pixeltribe.newssys.newscategory.model.NewsCategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NewsContentClassificationService {

    private final NewsContentClassificationRepository newsContentClassificationRepository;
    private final NewsRepository newsRepository;
    private final NewsCategoryRepository newsCategoryRepository;

    public NewsContentClassificationService(NewsContentClassificationRepository newsContentClassificationRepository, NewsRepository newsRepository, NewsCategoryRepository newsCategoryRepository) {
        this.newsContentClassificationRepository = newsContentClassificationRepository;
        this.newsRepository = newsRepository;
        this.newsCategoryRepository = newsCategoryRepository;
    }


    /* ---------- 建立關聯 (CREATE) ----------------------------- */
    public NewsContentClassificationUpdateDTO create(
            @Valid NewsContentClassificationCreationDTO dto) {

        // 檢查存在
        News news = newsRepository.getReferenceById(dto.getNewsId());
        NewsCategory cat = newsCategoryRepository.getReferenceById(dto.getCategoryId());

        // 檢查唯一
        if (newsContentClassificationRepository.existsByNewsNoIdAndNcatNoId(news.getId(), cat.getId())
        ) {
            throw new IllegalArgumentException("該新聞已包含此分類");
        }
        NewsContentClassification classification = new NewsContentClassification();
        classification.setNewsNo(news);
        classification.setNcatNo(cat);
        NewsContentClassification saved =
                newsContentClassificationRepository.save(classification);

        return toUpdateDto(saved);   // ← 手動轉成 UpdateDTO 當回傳
    }

    /* ---------- 讀取某新聞全部標籤 (READ) ---------------------- */
    @Transactional(readOnly = true)
    public List<NewsContentClassificationUpdateDTO> list(Integer newsId) {

        return newsContentClassificationRepository.findByNewsId(newsId).stream()
                .map(this::toUpdateDto)
                .toList();
    }

    /* ---------- 修改關聯 (UPDATE) ----------------------------- */
    public NewsContentClassificationUpdateDTO update(
            @Valid NewsContentClassificationUpdateDTO dto) {

        NewsContentClassification ncc = newsContentClassificationRepository.findById(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("關聯 id 不存在"));

        // 若要換分類
        if (dto.getNcatNoId() != null &&
                !dto.getNcatNoId().equals(ncc.getNcatNo().getId())) {

            Integer newsId = ncc.getNewsNo().getId();
            if (newsContentClassificationRepository.existsByNewsNoIdAndNcatNoId(newsId, dto.getNcatNoId())
                    ) {
                throw new IllegalArgumentException("新聞已連結該分類");
            }
            NewsCategory newCat = newsCategoryRepository.getReferenceById(dto.getNcatNoId());
            ncc.setNcatNo(newCat);           // Persistent 物件自動 flush
        }

        // 若要換新聞（較少見，示範用）
        if (dto.getNewsNoId() != null &&
                !dto.getNewsNoId().equals(ncc.getNewsNo().getId())) {

            if (newsContentClassificationRepository.existsByNewsNoIdAndNcatNoId(dto.newsNoId,ncc.getNcatNo().getId())
                    ) {
                throw new IllegalArgumentException("目標新聞已連結該分類");
            }
            News newNews = newsRepository.getReferenceById(dto.getNewsNoId());
            ncc.setNewsNo(newNews);
        }
        return toUpdateDto(ncc);
    }

    /* ---------- 刪除 (DELETE) --------------------------------- */
    public void delete(Integer nccId) {
        newsContentClassificationRepository.deleteById(nccId);
    }

    /* ========= 手動 Entity → UpdateDTO 映射 ==================== */
    private NewsContentClassificationUpdateDTO toUpdateDto(NewsContentClassification ncc) {

        NewsContentClassificationUpdateDTO dto = new NewsContentClassificationUpdateDTO();
        dto.setId(ncc.getId());
        dto.setNewsNoId(ncc.getNewsNo().getId());
        dto.setNcatNoId(ncc.getNcatNo().getId());
        return dto;
    }


}
