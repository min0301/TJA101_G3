package com.pixeltribe.forumsys.articlecomreport.model;

import com.pixeltribe.forumsys.exception.ConflictException;
import com.pixeltribe.forumsys.exception.ResourceNotFoundException;
import com.pixeltribe.forumsys.forumpost.model.ForumPost;
import com.pixeltribe.forumsys.message.model.ForumMes;
import com.pixeltribe.forumsys.message.model.ForumMesRepository;
import com.pixeltribe.forumsys.reporttype.model.ReportTypeRepository;
import com.pixeltribe.membersys.member.model.MemRepository;
import com.pixeltribe.membersys.member.model.Member;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service("articleComReportService")
public class ArticleComReportService {

    private final ArticleComReportRepository articleComReportRepository;
    private final ForumMesRepository forumMesRepository;
    private final MemRepository memRepository;
    private final ReportTypeRepository reportTypeRepository;

    public ArticleComReportService(
            ArticleComReportRepository articleComReportRepository,
            ForumMesRepository forumMesRepository,
            MemRepository memRepository,
            ReportTypeRepository reportTypeRepository) {
        this.articleComReportRepository = articleComReportRepository;
        this.forumMesRepository = forumMesRepository;
        this.memRepository = memRepository;
        this.reportTypeRepository = reportTypeRepository;
    }

    @Transactional
    public ArticleComReportDTO add(Integer memberId, ArticleComReportCreateDTO articleComReportCreateDTO) {
        ForumMes message = forumMesRepository.findById(articleComReportCreateDTO.getMessageNo())
                .orElseThrow(() -> new ResourceNotFoundException("找不到訊息"));
        Member member = memRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到會員"));

        articleComReportRepository.findByMesNoAndReporter(message, member)
                .ifPresent(
                        articleComReport -> {
                            throw new ConflictException("留言檢舉 '" + articleComReportCreateDTO + "' 已經存在");
                        });
        ArticleComReport articleComReport = new ArticleComReport();
        articleComReport.setMesNo(message);
        articleComReport.setReporter(member);
        articleComReport.setRpiNo(reportTypeRepository.findById(articleComReportCreateDTO.getReportTypeNo())
                .orElseThrow(() -> new ResourceNotFoundException("找不到文章留言檢舉類型編號")));
        return ArticleComReportDTO.convertToArticleComReportDTO(articleComReportRepository.save(articleComReport));
    }

    @Transactional
    public ArticleComReportDTO update(Integer articleComReportNo, ArticleComReportUpdateDTO articleComReportUpdateDTO) {
        ArticleComReport articleComReport = articleComReportRepository.findById(articleComReportNo)
                .orElseThrow(() -> new ResourceNotFoundException("找不到文章留言檢舉編號: " + articleComReportNo));
        articleComReport.setArtComRepStatus(articleComReportUpdateDTO.getArtComRepStatus());
        articleComReport.setFinishTime(Instant.now());

        if (Boolean.TRUE.equals(articleComReportUpdateDTO.getHideMessage())) {
            ForumMes messageToUpdate = articleComReport.getMesNo();
            if (messageToUpdate != null) {
                messageToUpdate.setMesStatus('1');
                ForumPost forumPost = messageToUpdate.getPostNo();
                forumPost.setMesNumbers(forumPost.getMesNumbers() - 1);
            }
        }


        return ArticleComReportDTO.convertToArticleComReportDTO(articleComReportRepository.save(articleComReport));
    }


    public List<ArticleComReportDTO> getAllArticleComReport() {
        return articleComReportRepository.findAll().stream()
                .map(ArticleComReportDTO::convertToArticleComReportDTO)
                .toList();
    }

    public ArticleComReportDTO getOneArticleComReport(Integer articleComReportNo) {
        ArticleComReport articleComReport = articleComReportRepository.findById(articleComReportNo)
                .orElseThrow(() -> new ResourceNotFoundException("找不到文章留言檢舉編號: " + articleComReportNo));
        return ArticleComReportDTO.convertToArticleComReportDTO(articleComReport);
    }

    public Long getCountByArtComRepStatus(Character artComRepStatus) {
        if (articleComReportRepository.countByArtComRepStatus(artComRepStatus) == 0) {
            return 0L;
        }
        return articleComReportRepository.countByArtComRepStatus(artComRepStatus);
    }


}
