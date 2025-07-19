package com.pixeltribe.shopsys.malltag.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.pixeltribe.forumsys.forum.model.Forum;
import com.pixeltribe.shopsys.malltag.exception.MallTagExistException;
import com.pixeltribe.shopsys.malltag.model.MallTag;
import com.pixeltribe.shopsys.malltag.model.MallTagRepository;
import com.pixeltribe.shopsys.malltag.model.MallTagService;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class MallTagController {
	
	@Autowired
	MallTagService mallTagService;
	
	@PostMapping("/malltag/add")
	public ResponseEntity<?> addMallTag(@Valid MallTag malltag, BindingResult result) {
	    if (result.hasErrors()) {
	        return ResponseEntity.badRequest().body("輸入資料有誤");
	    }
	    
	    try {
	    	MallTag addMallTag = mallTagService.add(malltag);
	        return ResponseEntity.ok(addMallTag);
	    } catch (MallTagExistException e) {
	        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("新增過程中發生錯誤");
	    }
	}

	@PutMapping("/malltag/update/{mallTagNO}")
	public ResponseEntity<?> updateMallTag(
	    @PathVariable("mallTagNO") Integer mallTagNO,
	    @RequestBody @Valid MallTag mallTag,
	    BindingResult result) {
	    
	    if (result.hasErrors()) {
	        List<String> errors = result.getAllErrors().stream()
	            .map(ObjectError::getDefaultMessage)
	            .collect(Collectors.toList());
	        return ResponseEntity.badRequest().body(errors);
	    }
	    
	    mallTag.setId(mallTagNO);
	    
	    try {
	        MallTag updateMallTag = mallTagService.update(mallTag);
	        
	        if (updateMallTag == null) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body("ID 為 " + mallTagNO + " 的商城標籤未找到或更新失敗。");
	        }
	        return ResponseEntity.ok(updateMallTag);
	    } catch (MallTagExistException e) {
	        return ResponseEntity.status(HttpStatus.CONFLICT)
	                .body(e.getMessage());
	    }catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	            .body("更新過程中發生錯誤：");
	    }
	}
	
	
	@GetMapping("malltag")
	public List<MallTag> getAllMallTags() {
        return mallTagService.getAllMallTags();
    }
}
