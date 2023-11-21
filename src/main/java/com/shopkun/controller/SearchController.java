package com.shopkun.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.shopkun.dao.ProductDao;
import com.shopkun.entity.Product;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class SearchController {
	@Autowired
	private HttpServletResponse response;
	@Autowired
	private ProductDao daoProduct;

	@GetMapping("/search")
	public String search(Model model, @RequestParam("q") String query,
			@RequestParam("page") Optional<Integer> pageNumber) throws UnsupportedEncodingException {
		// xóa cookie ban đầu và lưu lại cookie tìm kiếm mới
		if (!query.isEmpty()) {
			String name = "search_" + URLEncoder.encode(query, "UTF-8");
			String value = URLEncoder.encode(query, "UTF-8");
			Cookie removeCookie = new Cookie(name, value);
			removeCookie.setMaxAge(0);
			removeCookie.setPath("/");
			response.addCookie(removeCookie);

			Cookie addCookie = new Cookie(name, value);
			addCookie.setMaxAge(30 * 24 * 60 * 60); // 1 tháng
			addCookie.setPath("/");
			response.addCookie(addCookie);
		}
		model.addAttribute("vlQuery", query);

		// hiển thị kết quả tìm kiếm
		List<Product> searchProducts = new ArrayList<>();
		List<Product> nameProducts = daoProduct.findAllByName("%" + query + "%");

		if (nameProducts.size() != 0) {
			searchProducts.add(nameProducts.get(0));

			Long categoryId = 0l;
			for (Product nameP : nameProducts) {
				if (nameP.getCategory().getId() != categoryId) {
					List<Product> categoryProduct = daoProduct.findAllByCategory(nameP.getCategory());
					for (Product product : categoryProduct) {
						if (product.getId() != nameProducts.get(0).getId()) {
							searchProducts.add(product);
						}
					}
				}
				categoryId = nameP.getCategory().getId();
			}

			int totalSearchP = searchProducts.size();
			int sizePage = 18;
			int totalPage = totalSearchP / sizePage;
			if (totalSearchP % sizePage != 0) {
				totalPage++;
			}
			if (totalSearchP > sizePage) {
				int start = (pageNumber.orElse(1) - 1) * sizePage;
				int end = Math.min(start + sizePage, searchProducts.size());
				searchProducts = searchProducts.subList(start, end);
			}

			model.addAttribute("searchProducts", searchProducts);
			model.addAttribute("totalNameP", nameProducts.size());
			model.addAttribute("totalSearchP", totalSearchP);
			model.addAttribute("totalPage", totalPage);
			model.addAttribute("pageNumber", pageNumber.orElse(0));
		} else {
			model.addAttribute("totalNameP", nameProducts.size());
		}

		String right = "<i class=\"fa-solid fa-angle-right\"></i>";
		String instruction = "<a href=\"/\"><i class=\"fa-solid fa-house\"></i></a>" + right + "<a href=\"search?q="
				+ query + "\">Tìm kiếm sản phẩm</a>";
		model.addAttribute("instruction", instruction);
		return "search/search";
	}

	@PostMapping("/search")
	public ResponseEntity<String> search(@RequestParam("q") String query) throws UnsupportedEncodingException {
		String body = "";
		List<Product> products = daoProduct.findAllByName("%" + query + "%");
		if (products.size() > 8) {
			products = products.subList(0, 8);
		}

		for (Product product : products) {
			body += "<a class=\"rs\" href=\"/search?q=" + product.getName().trim() + "\">" + "<li>"
					+ product.getName().trim() + "</li></a>";
		}
		return ResponseEntity.ok(body);
	}

	@GetMapping("/delete-search-history")
	public ResponseEntity<String> deleteSearchHistory(@RequestParam("q") String query)
			throws UnsupportedEncodingException {
		String name = "search_" + URLEncoder.encode(query, "UTF-8");
		String value = URLEncoder.encode(query, "UTF-8");
		Cookie removeCookie = new Cookie(name, value);
		removeCookie.setMaxAge(0);
		removeCookie.setPath("/");
		response.addCookie(removeCookie);
		return ResponseEntity.ok("");
	}
}
