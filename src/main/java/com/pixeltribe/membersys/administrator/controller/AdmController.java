package com.pixeltribe.membersys.administrator.controller;

import com.pixeltribe.membersys.administrator.model.Administrator;
import com.pixeltribe.membersys.administrator.model.AdmService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/adm")
public class AdmController {

	@Autowired
	private AdmService admSvc;

	@GetMapping("addAdm")
	public String addAdm(ModelMap model) {
		Administrator admin = new Administrator();
		model.addAttribute("administrator", admin);
		return "back-end/adm/addAdm";
	}

	@PostMapping("insert")
	public String insert(@Valid Administrator administrator, BindingResult result, ModelMap model,
			@RequestParam("upFiles") MultipartFile[] parts) throws IOException {

		result = removeFieldError(administrator, result, "upFiles");

		if (parts[0].isEmpty()) {
			model.addAttribute("errorMessage", "請上傳頭像");
		} else {
			for (MultipartFile multipartFile : parts) {
				administrator.setAdmProfile(multipartFile.getBytes());
			}
		}

		if (result.hasErrors() || parts[0].isEmpty()) {
			return "back-end/adm/addAdm";
		}

		admSvc.addAdm(administrator);
		model.addAttribute("admListData", admSvc.getAll());
		model.addAttribute("success", "- (新增成功)");
		return "redirect:/adm/listAllAdm";
	}

	@PostMapping("getOne_For_Update")
	public String getOneForUpdate(@RequestParam("id") String id, ModelMap model) {
		Administrator administrator = admSvc.getOneAdm(Integer.valueOf(id));
		model.addAttribute("administrator", administrator);
		return "back-end/adm/update_adm_input";
	}

	@PostMapping("update")
	public String update(@Valid Administrator administrator, BindingResult result, ModelMap model,
			@RequestParam("upFiles") MultipartFile[] parts) throws IOException {

		result = removeFieldError(administrator, result, "upFiles");

		if (parts[0].isEmpty()) {
			byte[] origin = admSvc.getOneAdm(administrator.getId()).getAdmProfile();
			administrator.setAdmProfile(origin);
		} else {
			for (MultipartFile multipartFile : parts) {
				administrator.setAdmProfile(multipartFile.getBytes());
			}
		}

		if (result.hasErrors()) {
			return "back-end/adm/update_adm_input";
		}

		admSvc.updateAdm(administrator);
		model.addAttribute("success", "- (修改成功)");
		model.addAttribute("administrator", admSvc.getOneAdm(administrator.getId()));
		return "back-end/adm/listOneAdm";
	}

	@GetMapping("listAllAdm")
	public String listAllAdm(ModelMap model) {
		List<Administrator> list = admSvc.getAll();
		model.addAttribute("admListData", list);
		return "back-end/adm/listAllAdm";
	}
	
	public BindingResult removeFieldError(Administrator empVO, BindingResult result, String removedFieldname) {
		List<FieldError> errorsListToKeep = result.getFieldErrors().stream()
				.filter(fieldname -> !fieldname.getField().equals(removedFieldname))
				.collect(Collectors.toList());
		result = new BeanPropertyBindingResult(empVO, "empVO");
		for (FieldError fieldError : errorsListToKeep) {
			result.addError(fieldError);
		}
		return result;
	}
}
