package com.pixeltribe.forumsys.articlereport.model;

import com.pixeltribe.forumsys.exception.ConflictException;
import com.pixeltribe.forumsys.exception.ResourceNotFoundException;
import com.pixeltribe.forumsys.forumpost.model.ForumPost;
import com.pixeltribe.forumsys.forumpost.model.ForumPostRepository;
import com.pixeltribe.forumsys.reporttype.model.ReportTypeRepository;
import com.pixeltribe.membersys.member.model.MemRepository;
import com.pixeltribe.membersys.member.model.Member;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service("articleReportService")
public class ArticleReportService {

    private ArticleReportRepository articleReportRepository;
    private ForumPostRepository forumPostRepository;
    private final MemRepository memRepository;
    private final ReportTypeRepository reportTypeRepository;

    public ArticleReportService(
            ArticleReportRepository articleReportRepository,
            ForumPostRepository forumPostRepository,
            MemRepository memRepository,
            ReportTypeRepository reportTypeRepository) {
        this.articleReportRepository = articleReportRepository;
        this.forumPostRepository = forumPostRepository;
        this.memRepository = memRepository;
        this.reportTypeRepository = reportTypeRepository;
    }

    @Transactional
    public ArticleReportDTO add(Integer memberId, ArticleReportCreateDTO articleReportCreateDTO) {
        ForumPost forumPost = forumPostRepository.findById(articleReportCreateDTO.getPostNo())
                .orElseThrow(() -> new ResourceNotFoundException("找不到文章"));
        Member member = memRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到會員"));

        articleReportRepository.findByPostNoAndReporter(forumPost, member)
                .ifPresent(
                        articleReport -> {
                            throw new ConflictException("文章檢舉" + articleReportCreateDTO + "已經存在");
                        });
        ArticleReport articleReport = new ArticleReport();
        articleReport.setPostNo(forumPost);
        articleReport.setReporter(member);
        articleReport.setArtRepStatus('0'); // 或其他預設狀態
        articleReport.setCreateTime(Instant.now());
        articleReport.setRpiNo(reportTypeRepository.findById(articleReportCreateDTO.getReportTypeNo())
                .orElseThrow(() -> new ResourceNotFoundException("找不到文章檢舉類型編號")));
        return ArticleReportDTO.convertToArticleReportDTO(articleReportRepository.save(articleReport));
    }

    @Transactional
    public ArticleReportDTO update(Integer articleReportNo, ArticleReportUpdateDTO articleReportUpdateDTO) {
        ArticleReport articleReport = articleReportRepository.findById(articleReportNo)
                .orElseThrow(() -> new ResourceNotFoundException("找不到文章檢舉編號" + articleReportNo));
        articleReport.setArtRepStatus(articleReportUpdateDTO.getArtRepStatus());
        articleReport.setFinishTime(Instant.now());

        if (Boolean.TRUE.equals(articleReportUpdateDTO.getHidePost())) {
            ForumPost postStatusToUpdate = articleReport.getPostNo();
            if (postStatusToUpdate != null) {
                postStatusToUpdate.setPostStatus('1');
            }
        }
        return ArticleReportDTO.convertToArticleReportDTO(articleReportRepository.save(articleReport));
    }

    public List<ArticleReportDTO> getAllArticleReport() {
        return articleReportRepository.findAll().stream()
                .map(ArticleReportDTO::convertToArticleReportDTO)
                .toList();
    }

    public ArticleReportDTO getoneArticleReport(Integer articleReportNo) {
        ArticleReport articleReport = articleReportRepository.findById(articleReportNo)
                .orElseThrow(() -> new ResourceNotFoundException("找不到文章檢舉編號" + articleReportNo));
        return ArticleReportDTO.convertToArticleReportDTO(articleReport);
    }
}
