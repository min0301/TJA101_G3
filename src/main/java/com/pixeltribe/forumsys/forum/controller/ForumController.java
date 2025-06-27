package com.pixeltribe.forumsys.forum.controller;

import com.pixeltribe.forumsys.forum.model.Forum;
import com.pixeltribe.forumsys.forum.model.ForumService;
import com.pixeltribe.forumsys.forumcategory.model.ForumCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api")
public class ForumController {

    @Autowired
    ForumService forumSvc;

    @Autowired
    ForumCategoryService forumCategorySvc;


//    @GetMapping("/listAllForum")
//    public String listAllForums(Model model) { // 1. 傳入 Model 物件
//
//        // 2. 直接在此方法內查詢資料
//        // 注意：請確認你的 Service 方法名是 getAllForum() 還是 getAllForums()，要完全一致
//        List<Forum> list = forumSvc.getAllForum();
//
//        // 3. 將查到的資料放入 Model
//        model.addAttribute("forums", list);
//
//        // 4. 返回視圖名稱
//        return "back-end/forum/listAllForum";
//    }

    @GetMapping("AllForum")
    public List<Forum> findAll() {

        return forumSvc.getAllForum();

    }


}
