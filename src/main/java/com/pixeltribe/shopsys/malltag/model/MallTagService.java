package com.pixeltribe.shopsys.malltag.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MallTagService {
	
	@Autowired
	MallTagRepository mallTagRepository;
	
	public void add(MallTag mallTag) {
        mallTagRepository.save(mallTag);
    }
	
	@Transactional
    public MallTag update(MallTag mallTag) {
		// 檢查傳入的 mallTag 物件是否有 ID
        if (mallTag.getId() == null) {
            System.err.println("更新失敗：MallTag 物件中缺少 ID。");
            return null;
        }

        // 根據 ID 查詢現有的 MallTag 實體
        Optional<MallTag> existingMallTagOpt = mallTagRepository.findById(mallTag.getId());

        if (existingMallTagOpt.isPresent()) {
            MallTag existingMallTag = existingMallTagOpt.get();

            // 更新
            existingMallTag.setMallTagName(mallTag.getMallTagName());

            // 保存更新
            System.out.println("正在更新 MallTag ID: " + existingMallTag.getId() + " 為名稱: " + existingMallTag.getMallTagName()); // 打印日誌
            return mallTagRepository.save(existingMallTag);
        } else {
            // 如果找不到該 ID，更新目標不存在
            System.err.println("更新失敗：找不到 ID 為 " + mallTag.getId() + " 的商城標籤。"); 
            return null; 
        }

    }

    public void delete(MallTag mallTag) {
    	mallTagRepository.deleteById(mallTag.getId());
    }

    public MallTag getOneMallTag(Integer mallTagNO) {
        Optional<MallTag> malltag = mallTagRepository.findById(mallTagNO);
        return malltag.orElse(null);
    }

    public List<MallTag> getAllMallTags() {
        return mallTagRepository.findAll();

    }

}
