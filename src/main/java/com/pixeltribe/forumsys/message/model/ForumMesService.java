package com.pixeltribe.forumsys.message.model;

import com.pixeltribe.forumsys.exception.ResourceNotFoundException;
import com.pixeltribe.forumsys.forumpost.model.ForumPostRepository;
import com.pixeltribe.membersys.member.model.MemRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("forumMesService")
public class ForumMesService {

    private final ForumMesRepository forumMesRepository;
    private final ForumPostRepository forumPostRepository;
    private final MemRepository memRepository;

    public ForumMesService(ForumMesRepository forumMesRepository, ForumPostRepository forumPostRepository, MemRepository memRepository) {
        this.forumMesRepository = forumMesRepository;
        this.forumPostRepository = forumPostRepository;
        this.memRepository = memRepository;
    }

    public List<ForumMesDTO> getAllForumMes() {
        List<ForumMes> forumMess = forumMesRepository.findByMesStatus('0');
        return forumMess.stream()
                .map(ForumMesDTO::convertToForumMesDTO)
                .toList();
    }

    public List<ForumMesDTO> getAllAdminForumMes() {
        List<ForumMes> forumMess = forumMesRepository.findAll();
        return forumMess.stream()
                .map(ForumMesDTO::convertToForumMesDTO)
                .toList();
    }

    public ForumMesDTO getOneForumMes(Integer mesNo) {
        ForumMes forumMes = forumMesRepository.findById(mesNo)
                .orElseThrow(() -> new ResourceNotFoundException("找不到留言ID: " + mesNo));
        return ForumMesDTO.convertToForumMesDTO(forumMes);
    }

    public List<ForumMesDTO> getForumMesByPost(Integer postNo) {
        List<ForumMes> forumMes = forumMesRepository.findByPostNo_Id(postNo);
        return forumMes.stream()
                .map(ForumMesDTO::convertToForumMesDTO)
                .toList();
    }

    @Transactional
    public ForumMesDTO addForumMes(Integer postNo, Integer memberId, ForumMesUpdateDTO forumMesUpdateDTO) {

        ForumMes forumMes = new ForumMes();
        forumMes.setPostNo(forumPostRepository.findById(postNo)
                .orElseThrow(() -> new ResourceNotFoundException("找不到文章ID: " + postNo)));
        forumMes.setMemNo(memRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("找不到會員")));
        forumMes.setMesCon(forumMesUpdateDTO.getMesCon());

        return ForumMesDTO.convertToForumMesDTO(forumMesRepository.save(forumMes));
    }

    @Transactional
    public ForumMesDTO addMessageFromTask(ForumMesUpdateDTO forumMesUpdateDTO) {
        ForumMes forumMes = new ForumMes();
        forumMes.setPostNo(forumPostRepository.findById(forumMesUpdateDTO.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("找不到文章ID: " + forumMesUpdateDTO.getPostId())));
        forumMes.setMemNo(memRepository.findById(forumMesUpdateDTO.getMemId())
                .orElseThrow(() -> new ResourceNotFoundException("找不到會員ID: " + forumMesUpdateDTO.getMemId())));
        forumMes.setMesCon(forumMesUpdateDTO.getMesCon());
        return ForumMesDTO.convertToForumMesDTO(forumMesRepository.save(forumMes));
    }

    @Transactional
    public ForumMesDTO updateForumMes(Integer mesNo, ForumMesUpdateDTO forumMesUpdateDTO) {
        ForumMes forumMes = forumMesRepository.findById(mesNo)
                .orElseThrow(() -> new ResourceNotFoundException("找不到留言ID: " + mesNo));
        forumMes.setPostNo(forumPostRepository.findById(forumMesUpdateDTO.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("找不到文章")));
        forumMes.setMemNo(memRepository.findById(forumMesUpdateDTO.getMemId())
                .orElseThrow(() -> new ResourceNotFoundException("找不到會員")));
        forumMes.setMesCon(forumMesUpdateDTO.getMesCon());

        if (forumMesUpdateDTO.getMesStatus() != null) {
            forumMes.setMesStatus(forumMesUpdateDTO.getMesStatus());
        }
        return ForumMesDTO.convertToForumMesDTO(forumMesRepository.save(forumMes));

    }

}
