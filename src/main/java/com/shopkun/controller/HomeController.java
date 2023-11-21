package com.shopkun.controller;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.shopkun.dao.CategoryDao;
import com.shopkun.dao.OrderDetailDao;
import com.shopkun.dao.ProductDao;
import com.shopkun.entity.Category;
import com.shopkun.entity.Product;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class HomeController {
	@Autowired
	private HttpServletResponse response;
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private ProductDao daoProduct;
	@Autowired
	private CategoryDao daoCategory;
	@Autowired
	private OrderDetailDao daoOrderDetail;

	@GetMapping()
	public String index(Model model) {
		categories(model);

		promotionProducts(model);

		suggestProducts(model);

		cookieProducts(model);
		return "home/index";
	}

	@GetMapping("/featured-categories")
	public ResponseEntity<String> featuredCategory(@RequestParam("page") Optional<Integer> pageNumber) {
		String body = "";
		int sizePage = 6;
		Pageable page = PageRequest.of(pageNumber.orElse(1) - 1, sizePage);
		Page<Category> categoryFeatured = daoOrderDetail.findAllByCategory(page);
		long totalCF = categoryFeatured.getTotalElements();

		if (totalCF >= sizePage) {
			for (Category category : categoryFeatured) {
				body += "<div class=\"item col-xl-2 col-lg-2 col-md-2 col-sm-4 my-1\">\r\n"
						+ "					<a href=\"/category?id=" + category.getId() + "&name=" + category.getName()
						+ "\">\r\n" + "						<img src=\"/images/" + category.getImage()
						+ "\" width=\"60%\" height=\"60px\">\r\n" + "						<div class=\"name\">"
						+ category.getName() + "</div>\r\n" + "					</a>\r\n" + "				</div>";
			}
			body += "|" + totalCF;
		} else {
			Page<Category> categories = daoCategory.findAll(page);
			long totalC = categories.getTotalElements();
			for (Category category : categories) {
				body += "<div class=\"item col-xl-2 col-lg-2 col-md-2 col-sm-4 my-1\">\r\n"
						+ "					<a href=\"/category?id=" + category.getId() + "&name=" + category.getName()
						+ "\">\r\n" + "						<img src=\"/images/" + category.getImage()
						+ "\" width=\"60%\" height=\"60px\">\r\n" + "						<div class=\"name\">"
						+ category.getName() + "</div>\r\n" + "					</a>\r\n" + "				</div>";
			}
			body += "|" + totalC;
		}
		return ResponseEntity.ok(body);
	}

	public void categories(Model model) {
		int sizePage = 6;
		Pageable page = PageRequest.of(0, sizePage);
		Page<Category> categoryFeatured = daoOrderDetail.findAllByCategory(page);
		long totalCF = categoryFeatured.getTotalElements();

		if (totalCF >= sizePage) {
			model.addAttribute("categoryFeatured", categoryFeatured);
			model.addAttribute("totalCF", totalCF);
		} else {
			Page<Category> categories = daoCategory.findAll(page);
			model.addAttribute("categoryFeatured", categories);
			model.addAttribute("totalCF", categories.getTotalElements());
		}
	}

	@GetMapping("/products-promotion")
	public ResponseEntity<String> productsPromotion(@RequestParam("page") Optional<Integer> pageNumber) {
		String body = "";
		NumberFormat numb = NumberFormat.getInstance(new Locale("Vi", "VN"));
		int sizePage = 18;
		Pageable page = PageRequest.of(pageNumber.orElse(1) - 1, sizePage, Sort.by("category.name"));
		Page<Product> productsPromotion = daoProduct.findAllByPromotion(page);
		long total = productsPromotion.getTotalElements();
		for (Product product : productsPromotion) {
			body += "<div class=\"col-xl-2 col-lg-3 col-md-4 col-sm-4 my-2 p-1\">\r\n"
					+ "					<div class=\"product border\">\r\n"
					+ "						<div class=\"promotion\">-" + product.getDiscount() + "%</div>\r\n"
					+ "						<a href=\"product?id=" + product.getId() + "&name=" + product.getName()
					+ "\">\r\n" + "							<img src=\"/images/" + product.getImages().get(0)
					+ "\" width=\"100%\" height=\"200px\">\r\n"
					+ "							<div class=\"product-name\">" + product.getName() + "</div>\r\n"
					+ "							<div class=\"product-price\">"
					+ numb.format(product.getPrice() * (100 - product.getDiscount()) / 100) + " đ</div>\r\n"
					+ "							<del class=\"price-del\">" + numb.format(product.getPrice())
					+ " đ</del>\r\n" + "							<div><span class=\"add-cart\" id=\""
					+ product.getId() + "\"><i class=\"fa-solid fa-cart-shopping\"></i> Thêm vào giỏ</span></div>\r\n"
					+ "						</a>\r\n" + "					</div>\r\n" + "				</div>";
		}
		body += "|" + total;
		return ResponseEntity.ok(body);
	}

	public void promotionProducts(Model model) {
		int sizePage = 18;
		Pageable page = PageRequest.of(0, sizePage, Sort.by("category.name"));
		Page<Product> productsPromotion = daoProduct.findAllByPromotion(page);
		long totalPP = productsPromotion.getTotalElements();

		model.addAttribute("totalPP", totalPP);
		model.addAttribute("productsPromotion", productsPromotion);
	}

	@GetMapping("/suggest-products")
	public ResponseEntity<String> suggestProducts(@RequestParam("page") Optional<Integer> pageNumber) {
		String body = "";
		NumberFormat numb = NumberFormat.getInstance(new Locale("Vi", "VN"));
		int sizePage = 18;
		List<Product> suggestProducts = new ArrayList<>();
		Cookie cookies[] = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().startsWith("suggestP_")) {
					Optional<Product> product = daoProduct.findById(Long.parseLong(cookie.getValue()));
					if (product.isPresent()) {
						if (product.get().getQuantity() != 0) {
							suggestProducts.add(daoProduct.findById(Long.parseLong(cookie.getValue())).get());
						}
					}
				}
			}
		}
		long total = suggestProducts.size();
		int start = (pageNumber.orElse(1) - 1) * sizePage;
		int end = Math.min(start + sizePage, suggestProducts.size());
		suggestProducts = suggestProducts.subList(start, end);

		for (Product product : suggestProducts) {
			if (product.getQuantity() != 0) {
				body += "<div class=\"col-xl-2 col-lg-3 col-md-4 col-sm-4 my-2 p-1\">\r\n"
						+ "					<div class=\"product border\">\r\n"
						+ "						<div class=\"price-promotion\">\r\n";
				if (product.isPromotion()) {
					body += "								<div class=\"promotion\">-" + product.getDiscount()
							+ "%</div>\r\n";
				}
				body += "						</div>\r\n" + "						<a href=\"product?id="
						+ product.getId() + "&name=" + product.getName() + "\">\r\n"
						+ "							<img src=\"/images/" + product.getImages().get(0)
						+ "\" width=\"100%\" height=\"200px\">\r\n"
						+ "							<div class=\"product-name\">" + product.getName() + "</div>\r\n"
						+ "							<div class=\"product-priceDel\">\r\n"
						+ "								<div class=\"product-price\">"
						+ numb.format(product.getPrice() * (100 - product.getDiscount()) / 100) + " đ</div>\r\n";
				if (product.isPromotion()) {
					body += "								<del class=\"price-del\">" + numb.format(product.getPrice())
							+ " đ</del>\r\n";
				}
				body += "							</div>\r\n"
						+ "							<div><span class=\"add-cart\" id=\"" + product.getId()
						+ "\"><i class=\"fa-solid fa-cart-shopping\"></i> Thêm vào giỏ</span></div>\r\n"
						+ "						</a>\r\n" + "					</div>\r\n" + "				</div>";
			}
		}
		if ((start + sizePage) >= total) {
			body += "|end";
		}
		return ResponseEntity.ok(body);
	}

	public void suggestProducts(Model model) {
		int sizePage = 18;
		List<Product> suggestProducts = daoProduct.findAllShuffle();
		long totalSP = suggestProducts.size();

		// xóa dữ liệu cũ rồi nạp dữ liệu mới vào cookie
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().startsWith("suggestP_")) {
					Cookie removeCk = new Cookie(cookie.getName(), cookie.getValue());
					removeCk.setMaxAge(0);
					removeCk.setPath("/");
					response.addCookie(removeCk);
				}
			}
		}
		if (totalSP > sizePage) {
			for (Product product : suggestProducts) {
				Cookie cookie = new Cookie("suggestP_" + product.getId(), product.getId() + "");
				cookie.setMaxAge(-1);
				cookie.setPath("/");
				response.addCookie(cookie);
			}
			suggestProducts = suggestProducts.subList(0, sizePage);
		}
		model.addAttribute("totalSP", totalSP);
		model.addAttribute("suggestProducts", suggestProducts);
	}

	@GetMapping("/cookie-products")
	public ResponseEntity<String> cookieProducts(@RequestParam("page") Optional<Integer> pageNumber) {
		String body = "";
		NumberFormat numb = NumberFormat.getInstance(new Locale("Vi", "VN"));
		int sizePage = 6;
		int totalCookieP = 0;
		int countDeleteCkP = 0;
		List<Product> cookieProducts = new ArrayList<>();
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().startsWith("product_")) {
					Optional<Product> product = daoProduct.findById(Long.parseLong(cookie.getValue()));
					if (product.isPresent()) {
						cookieProducts.add(product.get());
					}
				}

				if (cookie.getName().equals("countDeleteCkP")) {
					countDeleteCkP = Integer.parseInt(cookie.getValue());
				}
			}
		}
		Collections.reverse(cookieProducts);
		for (Product p : cookieProducts) {
			if (p.getQuantity() != 0) {
				totalCookieP++;
			}
		}

		// Nếu có xóa sản phẩm đã xem thì sẽ - countDeleteCkP để thụt lùi lại số phần tử
		// đã xóa
		int start = (pageNumber.orElse(1) - 1) * sizePage - countDeleteCkP;
		int end = Math.min(start + sizePage, cookieProducts.size());
		cookieProducts = cookieProducts.subList(start, end);

		for (Product product : cookieProducts) {
			if (product.getQuantity() != 0 && product.getQuantity() != 0) {
				body += "<div class=\"col-xl-2 col-lg-3 col-md-4 col-sm-4 my-2 p-1\">\r\n"
						+ "						<div class=\"product border\">\r\n"
						+ "							<div class=\"price-promotion\">\r\n"
						+ "								<div class=\"delete-cookie\" id=\"" + product.getId()
						+ "\"><i class=\"fa-solid fa-trash-can\"></i> Xóa</div>\r\n";
				if (product.isPromotion()) {
					body += "								<div class=\"promotion\">-" + product.getDiscount()
							+ "%</div>\r\n";
				}
				body += "							</div>\r\n" + "							<a href=\"product?id="
						+ product.getId() + "&name=" + product.getName() + "\">\r\n"
						+ "								<img src=\"/images/" + product.getImages().get(0)
						+ "\" width=\"100%\" height=\"200px\">\r\n"
						+ "								<div class=\"product-name\">" + product.getName() + "</div>\r\n"
						+ "								<div class=\"product-priceDel\">\r\n"
						+ "									<div class=\"product-price\">"
						+ numb.format(product.getPrice() * (100 - product.getDiscount()) / 100) + " đ</div>\r\n";
				if (product.isPromotion()) {
					body += "									<del class=\"price-del\">"
							+ numb.format(product.getPrice()) + " đ</del>\r\n";
				}
				body += "								</div>\r\n";
				body += "								<div><span class=\"add-cart\" id=\"" + product.getId()
						+ "\"><i class=\"fa-solid fa-cart-shopping\"></i> Thêm vào giỏ</span></div>\r\n";
				body += "							</a>\r\n" + "						</div>\r\n"
						+ "					</div>";
			}
		}
		if ((start + sizePage) >= totalCookieP) {
			body += "|end";
		}
		return ResponseEntity.ok(body);
	}

	public void cookieProducts(Model model) {
		Cookie removeCountCkP = new Cookie("countDeleteCkP", "");
		removeCountCkP.setMaxAge(0);
		removeCountCkP.setPath("/");
		response.addCookie(removeCountCkP);

		int sizePage = 6;
		int totalCookieP = 0;
		List<Product> cookieProducts = new ArrayList<>();
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().startsWith("product_")) {
					Optional<Product> product = daoProduct.findById(Long.parseLong(cookie.getValue()));
					if (product.isPresent()) {
						cookieProducts.add(product.get());
					}
				}
			}
		}

		for (Product p : cookieProducts) {
			if (p.getQuantity() != 0) {
				totalCookieP++;
			}
		}

		Collections.reverse(cookieProducts);
		model.addAttribute("totalCookieP", totalCookieP);
		if (cookieProducts.size() > sizePage) {
			cookieProducts = cookieProducts.subList(0, sizePage);
		}
		model.addAttribute("cookieProducts", cookieProducts);
	}

	@PostMapping("/delete-cookieP")
	public ResponseEntity<String> deleteCookieP(@RequestParam("productId") Long productId) {
		Cookie removeCk = new Cookie("product_" + productId, productId + "");
		removeCk.setMaxAge(0);
		removeCk.setPath("/");
		response.addCookie(removeCk);

		String body = "";
		int totalCookieP = 0;
		Cookie[] cookies = request.getCookies();
		int flag = 0;
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().startsWith("product_")) {
					totalCookieP++;
				}

				if (cookie.getName().equals("countDeleteCkP")) {
					int count = Integer.parseInt(cookie.getValue()) + 1;
					Cookie countDeleteCkP = new Cookie("countDeleteCkP", String.valueOf(count));
					countDeleteCkP.setMaxAge(-1);
					countDeleteCkP.setPath("/");
					response.addCookie(countDeleteCkP);
					flag = 1;
				}
			}
		}
		if (flag == 0) {
			Cookie countDeleteCkP = new Cookie("countDeleteCkP", 1 + "");
			countDeleteCkP.setMaxAge(-1);
			countDeleteCkP.setPath("/");
			response.addCookie(countDeleteCkP);
		}
		body = totalCookieP + "";
		return ResponseEntity.ok(body);
	}
}
