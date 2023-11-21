package com.shopkun.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.shopkun.dao.CategoryDao;
import com.shopkun.dao.ProductDao;
import com.shopkun.entity.Category;
import com.shopkun.entity.Product;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class CategoryController {
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private HttpServletResponse response;
	@Autowired
	private CategoryDao daoCategory;
	@Autowired
	private ProductDao daoProduct;
	
	@GetMapping("category")
	public String category(@RequestParam("page") Optional<Integer> pageNumber,Model model, @RequestParam("id") Long id, @RequestParam("name") String name) {
		Cookie removeCountCkP = new Cookie("countDeleteCkP", "");
		removeCountCkP.setMaxAge(0);
		removeCountCkP.setPath("/");
		response.addCookie(removeCountCkP);
		
		Category category = daoCategory.findById(id).get();
		int sizePage = 18;
		Pageable page = PageRequest.of(pageNumber.orElse(1) - 1, sizePage);
		Page<Product> categoryProducts = daoProduct.findAllByCategory(category, page);
		long total = categoryProducts.getTotalElements();
		int totalPage = (int) (total / sizePage);
		if (total % sizePage != 0) {
			totalPage++;
		}
		
		model.addAttribute("categoryProducts", categoryProducts);
		model.addAttribute("page", categoryProducts);
		model.addAttribute("total", total);
		model.addAttribute("totalPage", totalPage);
		model.addAttribute("pageNumber", pageNumber.orElse(0));
		model.addAttribute("category", category);
		
		int sizePageCookie = 6;
		List<Product> cookieProducts = new ArrayList<>();
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().startsWith("product_")) {
					Optional<Product> product = daoProduct.findById(Long.parseLong(cookie.getValue()));
					if(product.isPresent()) {
						cookieProducts.add(product.get());
					}
				}
			}
		}
		Collections.reverse(cookieProducts);
		model.addAttribute("totalCookieP", cookieProducts.size());
		if (cookieProducts.size() >= sizePageCookie) {
			cookieProducts = cookieProducts.subList(0, sizePageCookie);
		}
		model.addAttribute("cookieProducts", cookieProducts);
		
		String right = "<i class=\"fa-solid fa-angle-right\"></i>";
		String instruction = "<a href=\"/\"><i class=\"fa-solid fa-house\"></i></a>" + right + "<a href=\"category?id="
				+ category.getId() + "&name=" + category.getName() + "\">" + category.getName() + "</a>";
		model.addAttribute("instruction", instruction);
		model.addAttribute("title", "Shopkun - " + name);
		return "category/category";
	}
}
