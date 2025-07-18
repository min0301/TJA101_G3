package com.pixeltribe.newssys.news.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pixeltribe.common.PageResponse;
import com.pixeltribe.common.PageResponseFactory;
import com.pixeltribe.membersys.administrator.model.AdmRepository;
import com.pixeltribe.membersys.administrator.model.Administrator;
import com.pixeltribe.newssys.newscategory.model.NewsCategory;
import com.pixeltribe.newssys.newscategory.model.NewsCategoryRepository;
import com.pixeltribe.newssys.newscontentclassification.model.NewsContentClassification;
import com.pixeltribe.newssys.newscontentclassification.model.NewsContentClassificationRepository;
import com.pixeltribe.newssys.newsimage.model.NewsImage;
import com.pixeltribe.newssys.newsimage.model.NewsImageRepository;
import com.pixeltribe.util.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class NewsService {

    private final NewsRepository newsRepository;
    private final AdmRepository admRepository;
    private final NewsCategoryRepository newsCategoryRepository;
    private final NewsContentClassificationRepository newsContentClassificationRepository;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final NewsImageRepository newsImageRepository;
    private final ObjectMapper objectMapper;

    public NewsService(NewsRepository newsRepository, AdmRepository admRepository, NewsCategoryRepository newsCategoryRepository, NewsContentClassificationRepository newsContentClassificationRepository, JwtUtil jwtUtil, RedisTemplate<String, Object> redisTemplate, NewsImageRepository newsImageRepository, ObjectMapper objectMapper) {
        this.newsRepository = newsRepository;
        this.admRepository = admRepository;
        this.newsCategoryRepository = newsCategoryRepository;
        this.newsContentClassificationRepository = newsContentClassificationRepository;
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
        this.newsImageRepository = newsImageRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public NewsDTO getOneNews(Integer id) {
        NewsDTO newsDto = newsRepository.getNewsById(id);
        return newsDto;
    }

    @Transactional(readOnly = true)
    public PageResponse<NewsDTO> findAll(int page, int size) {
        Page<NewsDTO> pageData = newsRepository.getPageNews(PageRequest.of(page, size));
        return PageResponseFactory.fromPage(pageData);
    }

    @Transactional
    public NewsCreationDTO createNews(String tit, String con, Integer adminNoId, List<Integer> tags) {

        Administrator admin = admRepository.findById(adminNoId).orElseThrow(() -> new EntityNotFoundException("admin"));

        News news = new News();
        news.setNewsTit(tit);
        news.setNewsCon(con);
        news.setAdminNo(admin);

        newsRepository.save(news);

        // 將每個 tag id 建立對應關聯
        for (Integer tagId : tags) {
            NewsCategory tag = newsCategoryRepository.findById(tagId)
                    .orElseThrow(() -> new EntityNotFoundException("分類編號不存在：" + tagId));

            NewsContentClassification ncc = new NewsContentClassification();
            ncc.setNewsNo(news);
            ncc.setNcatNo(tag);

            newsContentClassificationRepository.save(ncc);
        }

        NewsCreationDTO nCDTO = new NewsCreationDTO(news.getId(), tit, con, adminNoId, tags);

        return nCDTO;
    }

    @Transactional(readOnly = true)
    public PageResponse<NewsAdminDTO> findAllAdminNews(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<NewsAdminDTO> pageData = newsRepository.findAdminPageNewsByTitle(keyword, pageable);
        return PageResponseFactory.fromPage(pageData);
    }

    @Transactional
    public NewsAdminUpdateDto updateNews(@Valid NewsAdminUpdateDto nauDTO) {
        News news = newsRepository.findById(nauDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException("not found news"));

        // 更新主要欄位
        news.setNewsTit(nauDTO.getNewsTit());
        news.setNewsCon(nauDTO.getNewsCon());
        news.setIsShowed(nauDTO.getIsShowed());

        // 更新分類：先刪除舊有分類，再加入新的分類
        newsContentClassificationRepository.deleteByNewsNoId(news.getId());
        for (Long catId : nauDTO.getCategoryIds()) {
            NewsCategory category = newsCategoryRepository.findById(catId.intValue())
                    .orElseThrow(() -> new EntityNotFoundException("分類編號不存在: " + catId));
            NewsContentClassification ncc = new NewsContentClassification();
            ncc.setNewsNo(news);
            ncc.setNcatNo(category);
            newsContentClassificationRepository.save(ncc);
        }

        return nauDTO;
    }

    @Transactional
    public ResponseEntity<?> createAdmin(@Valid @RequestBody NewsCreationDTO dto, HttpServletRequest req) throws JsonProcessingException {
        Integer adminId = jwtUtil.getAdminIdFromJWT(req); // ✅ 建議改名一致

        Administrator admin = admRepository.findById(adminId).orElse(null);
        if (admin == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "errorCode", "ADM_401",
                    "errorMessage", "管理員驗證失敗"
            ));
        }

        // 建立新聞主體
        News news = new News();
        news.setNewsTit(dto.getNewsTit());
        news.setNewsCon(dto.getNewsCon());
        news.setAdminNo(admin);
        newsRepository.save(news);

        // 取得 Redis 暫存圖片清單
        String redisKey = "temp_news_images:admin:" + adminId;
        List<Object> rawList = redisTemplate.opsForList().range(redisKey, 0, -1);

        for (Object obj : rawList) {
            Map<String, String> data = objectMapper.readValue(
                    obj.toString(), new TypeReference<Map<String, String>>() {
                    });
            NewsImage img = new NewsImage();
            img.setNewsNo(news);
            img.setImgUrl(data.get("url"));
            img.setImgType(data.get("mime"));
            newsImageRepository.save(img);
        }

        // 清空暫存
        redisTemplate.delete(redisKey);

        return ResponseEntity.ok(Map.of("newsId", news.getId()));
    }

    public NewsAdminUpdateDto updateShowStatus(Integer id, Boolean isShowed) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("not found news"));

        news.setIsShowed(isShowed);
        newsRepository.save(news);

        NewsAdminUpdateDto nauDTO = new NewsAdminUpdateDto();
        nauDTO.setId(news.getId());
        nauDTO.setNewsTit(news.getNewsTit());
        nauDTO.setNewsCon(news.getNewsCon());
        nauDTO.setIsShowed(news.getIsShowed());

        return nauDTO;
    }

    @Transactional(readOnly = true)
    public NewsAdminDTO findOneAdmin(Integer newsId) {
        return newsRepository.findById(newsId)
                .map(news -> new NewsAdminDTO(
                        news.getId(),
                        news.getNewsTit(),
                        news.getNewsCon(),
                        news.getNewsUpdate(),
                        news.getNewsCrdate(),
                        news.getIsShowed(),
                        (long) news.getNewsImages().size(),
                        news.getNewContentClassifications().stream()
                                .map(ncc -> ncc.getNcatNo().getNcatName())
                                .collect(Collectors.joining(",")),
                        news.getAdminNo().getId(),
                        news.getAdminNo().getAdmName()))
                .orElseThrow(() -> new EntityNotFoundException("not found news"));
    }

    public Long getNewsCount() {
        return newsRepository.count();
    }
}
