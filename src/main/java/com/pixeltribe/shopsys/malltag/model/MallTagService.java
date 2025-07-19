package com.pixeltribe.shopsys.malltag.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pixeltribe.shopsys.malltag.exception.MallTagExistException;
import com.pixeltribe.shopsys.product.exception.ProductExistException;
import com.pixeltribe.shopsys.product.model.Product;

@Service
public class MallTagService {
	
	@Autowired
	MallTagRepository mallTagRepository;
	
	@Transactional
	public MallTag add(MallTag mallTag) {
		boolean isExist = mallTagRepository.isExistMallTag(mallTag.getMallTagName()) != null;
	    
	    if (isExist) {
	        throw new MallTagExistException("平台已存在，請重新確認");
	    }
		return mallTagRepository.save(mallTag);
    }
	
	@Transactional
    public MallTag update(MallTag mallTag) {
		Integer exist = mallTagRepository.isExistMallTag(mallTag.getMallTagName());
		boolean isExist = exist != null && !exist.equals(mallTag.getId());
	    if (isExist) {
	        throw new MallTagExistException("平台已存在，請重新確認");
	    }
		return mallTagRepository.save(mallTag);
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
