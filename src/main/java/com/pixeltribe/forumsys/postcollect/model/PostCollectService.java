package com.pixeltribe.forumsys.postcollect.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PostCollectService {

    @Autowired
    private PostCollectRepository postCollectRepository;

    public List<PostCollectDto> getPostCollectionsByMemberId(Integer memberId) {
        // 查詢這個會員的所有收藏文章
        List<PostCollect> collects = postCollectRepository.findByMember_Id(memberId);

        // 組成 DTO 列表（回傳前端需要的資料）
        List<PostCollectDto> result = new ArrayList<>();
        for (PostCollect collect : collects) {
            PostCollectDto dto = new PostCollectDto(
                collect.getId(),
                collect.getPostNo().getPostTitle(),    // 文章標題
                collect.getPcollUpdate()          	   // 收藏時)
            );
            result.add(dto);
        }
        return result;
    }

}
