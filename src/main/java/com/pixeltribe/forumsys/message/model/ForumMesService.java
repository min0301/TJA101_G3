package com.pixeltribe.forumsys.message.model;

import com.pixeltribe.forumsys.forumcategory.model.ForumCategory;
import com.pixeltribe.forumsys.forumpost.model.ForumPostRepository;
import com.pixeltribe.membersys.member.model.MemRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.stream.Collectors;

@Service("forumMesService")
public class ForumMesService {

    private final ForumMesRepository forumMesRepository;
    private final ForumPostRepository forumPostRepository;
    private final MemRepository memRepository;

    public ForumMesService(ForumMesRepository forumMesRepository, ForumPostRepository forumPostRepository, MemRepository memRepository){
        this.forumMesRepository = forumMesRepository;
        this.forumPostRepository = forumPostRepository;
        this.memRepository = memRepository;
    }

    public List<ForumMesDTO> getAllForumMes() {
        List<ForumMes> forumMess = forumMesRepository.findAll();
        return forumMess.stream()
                .map(ForumMesDTO::convertToForumMesDTO)
                .collect(Collectors.toList());
    }

    public ForumMesDTO getOneForumMes(Integer mesNo){
        ForumMes forumMes = forumMesRepository.findById(mesNo).get();
        return ForumMesDTO.convertToForumMesDTO(forumMes);
    }

    public List<ForumMesDTO> getForumMesByPost(Integer postNo){
        List<ForumMes> forumMes = forumMesRepository.findByPostNo_Id(postNo);
        return forumMes.stream()
                .map(ForumMesDTO::convertToForumMesDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ForumMesDTO addForumMes(ForumMesUptateDTO forumMesUptateDTO) {

        ForumMes forumMes = new ForumMes();
        forumMes.setPostNo(forumPostRepository.findById(forumMesUptateDTO.getPostId()).get());
        forumMes.setMemNo(memRepository.findById(forumMesUptateDTO.getMemId()).get());
        forumMes.setMesCon(forumMesUptateDTO.getMesCon());

        return ForumMesDTO.convertToForumMesDTO(forumMesRepository.save(forumMes));
    }

    public ForumMesDTO updateForumMes(Integer mesNo,ForumMesUptateDTO forumMesUptateDTO) {
        ForumMes forumMes = forumMesRepository.findById(mesNo).get();
        forumMes.setPostNo(forumPostRepository.findById(forumMesUptateDTO.getPostId()).get());
        forumMes.setMemNo(memRepository.findById(forumMesUptateDTO.getMemId()).get());
        forumMes.setMesCon(forumMesUptateDTO.getMesCon());

        return ForumMesDTO.convertToForumMesDTO(forumMesRepository.save(forumMes));

    }

}
