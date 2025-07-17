package com.pixeltribe.newssys.newsimage.model;

import com.pixeltribe.newssys.news.model.News;
import com.pixeltribe.newssys.news.model.NewsRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class NewsImageService {

    private final NewsImageRepository newsImageRepository;
    private final NewsRepository newsRepository;
    private final Path storageRoot;
    private final String baseUrl;
    private static final List<String> ALLOWED = List.of(
            "image/jpeg",
            "image/png",
            "image/webp");

    public NewsImageService(NewsImageRepository newsImageRepository,
                            NewsRepository newsRepository,
                            @Value("${file.upload-dir}") String storageRoot,
                            @Value("${file.base-url}") String baseUrl) {
        this.newsImageRepository = newsImageRepository;
        this.newsRepository = newsRepository;
        this.storageRoot = Paths.get("static", "uploads").toAbsolutePath().normalize();

        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    @Transactional(readOnly = true)
    public List<NewsImageDTO> getNewsImage(Integer id) {
        return newsImageRepository.findNewsImageByNewsNo_Id(id);
    }

    @Transactional(readOnly = true)
    public List<NewsImageDTO> findById(Integer id) {
        return newsImageRepository.findNewsImageByNewsNo_Id(id);
    }

    @Transactional
    public String uploadAndGetUrl(MultipartFile file, Integer newsId) {

        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new EntityNotFoundException("沒找到新聞"));

        String mimeType = file.getContentType();
        if (mimeType == null || !ALLOWED.contains(mimeType.toLowerCase())) {
            throw new IllegalArgumentException("只允許上傳 JPEG、PNG、WebP 圖片\n不支援的圖片格式：" + mimeType);
        }

        String ext = switch (mimeType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> "";
        };

        String fileName = UUID.randomUUID() + "-" + newsId + ext;

        try (InputStream input = file.getInputStream()) {
            Files.createDirectories(storageRoot);
            Path target = storageRoot.resolve(fileName);
            Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new RuntimeException("無法儲存圖片至路徑：" + storageRoot + "，新聞 ID：" + newsId, ex);
        }

        NewsImage newsImage = new NewsImage();
        newsImage.setNewsNo(news);
        newsImage.setImgUrl(baseUrl + "/uploads/" + fileName);
        newsImage.setImgType(ext.substring(1));
        newsImageRepository.save(newsImage);

        return newsImage.getImgUrl();
    }

    @Transactional(readOnly = true)
    public List<NewsImageIndexDTO> getImage5() {
        return newsImageRepository.getTopFiveImage();
    }
}
