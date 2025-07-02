package com.pixeltribe.shopsys.malltag.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pixeltribe.shopsys.malltag.model.MallTag;
import com.pixeltribe.shopsys.malltag.model.MallTagService;

@RestController
@RequestMapping("/malltag")
public class MallTagController {
	
	@Autowired
	MallTagService mallTagService;
	
	@GetMapping("MallTag")
	public List<MallTag> getAllMallTags() {
        return mallTagService.getAllMallTags();
    }
}
