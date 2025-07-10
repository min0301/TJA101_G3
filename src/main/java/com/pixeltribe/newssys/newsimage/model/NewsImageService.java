package com.pixeltribe.newssys.newsimage.model;

import com.pixeltribe.newssys.news.model.News;
import com.pixeltribe.newssys.news.model.NewsRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class NewsImageService {

    private final NewsImageRepository newsImageRepository;
    private final NewsRepository newsRepository;
    private final Path storageRoot;
    private final String baseUrl;

    public NewsImageService(NewsImageRepository newsImageRepository,
                            NewsRepository newsRepository,
                            @Value("${file.upload-dir}") String storageRoot,
                            @Value("${file.base-url}") String baseUrl) {
        this.newsImageRepository = newsImageRepository;
        this.newsRepository = newsRepository;
        this.storageRoot = Paths.get(storageRoot).toAbsolutePath().normalize();
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public List<NewsImageDTO> getNewsImage(Integer id) {
        return newsImageRepository.findNewsImageByNewsNo_Id(id);
    }

    public List<NewsImageDTO> findById(Integer id) {
        return newsImageRepository.findNewsImageByNewsNo_Id(id);
    }

    public String uploadAndGetUrl(MultipartFile file, Integer newsId) {
        try {
            News news = newsRepository.findById(newsId).orElseThrow(() -> new EntityNotFoundException("News not found"));

            Files.createDirectories(storageRoot);

            String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
            String fileName = UUID.randomUUID() + (ext != null ? ext : "");
            Path target = storageRoot.resolve(fileName);

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            NewsImage newsImage = new NewsImage();
            newsImage.setNewsNo(news);
            newsImage.setImgUrl(baseUrl + "/uploads/" + fileName);
            newsImageRepository.save(newsImage);

            return newsImage.getImgUrl();

        } catch (IOException e) {
            throw new RuntimeException("Error while uploading news image " + newsId, e);
        }
    }
}
