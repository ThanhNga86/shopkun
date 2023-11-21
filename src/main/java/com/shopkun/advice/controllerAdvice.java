package com.shopkun.advice;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.shopkun.dao.CategoryDao;
import com.shopkun.entity.Category;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ControllerAdvice
public class controllerAdvice {
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private CategoryDao daoCategory;

	@ModelAttribute
	public void categoriesMenu(Model model, HttpServletResponse response) {
		List<Category> categories = daoCategory.findAll();
		model.addAttribute("categories", categories);
	}

	@ModelAttribute
	public void searchHistory(Model model) throws UnsupportedEncodingException {
		List<String> searchHistory = new ArrayList<>();
		Cookie cookies[] = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().startsWith("search_")) {
					String value = URLDecoder.decode(cookie.getValue(), "UTF-8");
					searchHistory.add(value);
				}
			}
		}
		Collections.reverse(searchHistory);
		if (searchHistory.size() > 8) {
			searchHistory = searchHistory.subList(0, 8);
		}
		
		model.addAttribute("totalHistory", searchHistory.size());
		model.addAttribute("searchHistory", searchHistory);
	}
}
