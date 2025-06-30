package com.pixeltribe.shopsys.malltag.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MallTagService {
	
	@Autowired
	MallTagRepository mallTagRepository;
	
	public void add(MallTag mallTag) {
        mallTagRepository.save(mallTag);
    }

    public void update(MallTag mallTag) {
        mallTagRepository.save(mallTag);
    }

    public void delete(MallTag mallTag) {
    	mallTagRepository.deleteById(mallTag.getId());
    }

    public MallTag getOneMallTag(Integer mallTagNO) {
        Optional<MallTag> optional = mallTagRepository.findById(mallTagNO);
        return optional.orElse(null);
    }

    public List<MallTag> getAllMallTags() {
        return mallTagRepository.findAll();
    }

}
