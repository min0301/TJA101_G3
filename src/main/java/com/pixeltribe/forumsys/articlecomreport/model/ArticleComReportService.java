package com.pixeltribe.forumsys.articlecomreport.model;

import com.pixeltribe.forumsys.exception.ConflictException;
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
    public ArticleComReportDTO add(ArticleComReportCreateDTO articleComReportCreateDTO) {
        ForumMes message = forumMesRepository.findById(articleComReportCreateDTO.getMessageNo()).get();
        Member member = memRepository.findById(articleComReportCreateDTO.getMemberNo()).get();

        articleComReportRepository.findByMesNoAndReporter(message, member)
                .ifPresent(
                        articleComReport -> {
                            throw new ConflictException("留言檢舉 '" + articleComReportCreateDTO + "' 已經存在");
                        });
        ArticleComReport articleComReport = new ArticleComReport();
        articleComReport.setMesNo(forumMesRepository.findById(articleComReportCreateDTO.getMessageNo()).get());
        articleComReport.setReporter(memRepository.findById(articleComReportCreateDTO.getMemberNo()).get());
        articleComReport.setRpiNo(reportTypeRepository.findById(articleComReportCreateDTO.getReportTypeNo()).get());
        return ArticleComReportDTO.convertToArticleComReportDTO(articleComReportRepository.save(articleComReport));
    }

    public ArticleComReportDTO update(Integer articleComReportNo, ArticleComReportUpdateDTO articleComReportUpdateDTO) {
        ArticleComReport articleComReport = articleComReportRepository.findById(articleComReportNo).get();
        articleComReport.setArtComRepStatus(articleComReportUpdateDTO.getArtComRepStatus());
        articleComReport.setFinishTime(Instant.now());
        return ArticleComReportDTO.convertToArticleComReportDTO(articleComReportRepository.save(articleComReport));
    }


    public List<ArticleComReportDTO> getAllArticleComReport() {
        return articleComReportRepository.findAll().stream()
                .map(ArticleComReportDTO::convertToArticleComReportDTO)
                .toList();
    }


}
