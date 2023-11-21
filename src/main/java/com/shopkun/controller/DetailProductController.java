package com.shopkun.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.shopkun.dao.AccountDao;
import com.shopkun.dao.CategoryDao;
import com.shopkun.dao.ProductDao;
import com.shopkun.dao.ReviewDao;
import com.shopkun.dao.ReviewFeedbackDao;
import com.shopkun.entity.Account;
import com.shopkun.entity.Category;
import com.shopkun.entity.Product;
import com.shopkun.entity.Review;
import com.shopkun.entity.ReviewFeedback;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class DetailProductController {
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private HttpServletResponse response;
	@Autowired
	private ProductDao daoProduct;
	@Autowired
	private CategoryDao daoCategory;
	@Autowired
	private AccountDao daoAccount;
	@Autowired
	private ReviewDao daoReview;
	@Autowired
	private ReviewFeedbackDao daoReviewFB;

	@GetMapping("/product")
	public String detailProduct(Model model, @RequestParam("id") Long id, @RequestParam("name") String name) {
		Optional<Product> product = daoProduct.findById(id);
		model.addAttribute("product", product.get());

		Cookie removeCk = new Cookie("product_" + id, String.valueOf(id));
		removeCk.setMaxAge(0);
		removeCk.setPath("/");
		response.addCookie(removeCk);

		Cookie cookie = new Cookie("product_" + id, String.valueOf(id));
		cookie.setMaxAge(-1);
		cookie.setPath("/");
		response.addCookie(cookie);

		List<Product> cookieProducts = new ArrayList<>();
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie ck : cookies) {
				if (ck.getName().startsWith("product_")) {
					if (Integer.parseInt(ck.getValue()) != id) {
						Optional<Product> prod = daoProduct.findById(Long.parseLong(ck.getValue()));
						if (prod.isPresent()) {
							cookieProducts.add(prod.get());
						}
					}
				}
			}
		}
		Collections.reverse(cookieProducts);
		model.addAttribute("totalCookieP", cookieProducts.size());
		if (cookieProducts.size() >= 30) {
			cookieProducts = cookieProducts.subList(0, 30);
		}
		model.addAttribute("cookieProducts", cookieProducts);

		Category category = daoCategory.findById(product.get().getCategory().getId()).get();
		model.addAttribute("category", category);

		List<Product> productSimilar = daoProduct.findAllSimilar(product.get().getId(), category);
		model.addAttribute("productSimilar", productSimilar);
		model.addAttribute("totalPSimilar", productSimilar.size());

		Pageable pageReview = PageRequest.of(0, 10);
		Page<Review> reviews = daoReview.findAllByProductId(product.get(), pageReview);
		model.addAttribute("reviews", reviews);

		List<ReviewFeedback> reviewFBs = daoReviewFB.findAll();
		model.addAttribute("reviewFBs", reviewFBs);

		int countRvFB = 0;
		for (Review rv : daoReview.findAllByProduct(product.get())) {
			for (ReviewFeedback rvFB : daoReviewFB.findAll()) {
				if (rv.getId() == rvFB.getReview().getId()) {
					countRvFB++;
				}
			}
		}
		int countRv = daoReview.findAllByProduct(product.get()).size();
		long totalReview = countRv + countRvFB;
		model.addAttribute("totalReview", totalReview);
		model.addAttribute("totalCmt", countRv);

		DecimalFormat df = new DecimalFormat("#.#");
		double countR1 = 0, countR2 = 0, countR3 = 0, countR4 = 0, countR5 = 0;
		if (!reviews.isEmpty()) {
			for (Review review : reviews) {
				double rate = review.getRate();
				if (rate == 5) {
					countR5++;
				}
				if (rate == 4) {
					countR4++;
				}
				if (rate == 3) {
					countR3++;
				}
				if (rate == 2) {
					countR2++;
				}
				if (rate == 1) {
					countR1++;
				}
			}
			double rate = (countR5 * 5 + countR4 * 4 + countR3 * 3 + countR2 * 3 + countR1)
					/ (countR5 + countR4 + countR3 + countR2 + countR1);
			model.addAttribute("rate", df.format(rate));
		}

		String right = "<i class=\"fa-solid fa-angle-right\"></i>";
		String instruction = "<a href=\"/\"><i class=\"fa-solid fa-house\"></i></a>" + right + "<a href=\"category?id="
				+ category.getId() + "&name=" + category.getName() + "\">" + category.getName() + "</a>";
		model.addAttribute("instruction", instruction);
		model.addAttribute("title", "Shopkun - " + name);
		return "detail-product/product";
	}

	@GetMapping("/product/review")
	public ResponseEntity<String> pagingReviews(@RequestParam("pageNumber") int pageNumber,
			@RequestParam("productId") Long productId) {
		String body = "";
		int sizePage = 10;
		Pageable page = PageRequest.of(pageNumber, sizePage);

		Page<Review> reviews = daoReview.findAllByProductId(daoProduct.findById(productId).get(), page);
		for (Review review : reviews) {
			body += "<div class=\"cmt\" id=\"" + review.getId() + "\">\r\n"
					+ "							<span class=\"name-cmt\">" + review.getAccount().getFullName()
					+ "</span>\r\n" + "							<span class=\"star-cmt\" id=\"" + review.getRate()
					+ "\">" + "</span>\r\n" + "							<div class=\"text-cmt\">" + review.getComment()
					+ "</div>\r\n" + "							<div class=\"image-cmt\">\r\n";
			if (review.getImages() != null) {
				for (String image : review.getImages()) {
					body += "<img src=\"/images/" + image + "\" width=\"100\" height=\"100\">";
				}
			}
			body += "							</div>" + "							<label class=\"reply-cmt\">\r\n"
					+ "								Trả lời\r\n" + "							</label>\r\n"
					+ "							<span style=\"color: darkgray\">"
					+ new SimpleDateFormat("dd-MM-yyyy", new Locale("Vi", "VN")).format(review.getCreateDate())
					+ "</span>\r\n" + "							<div class=\"feedbackCmt\"></div>"
					+ "<div class=\"feedbacks\">";
			for (ReviewFeedback reviewFB : daoReviewFB.findAll()) {
				if (reviewFB.getReview().getId() == review.getId()) {
					body += "<div class=\"cmtFB\" id=\"" + review.getId() + "\">\r\n"
							+ "									<span class=\"name-cmtFB\">"
							+ reviewFB.getAccount().getFullName() + "</span>\r\n";
					if (!reviewFB.getRespondent().equals(review.getAccount().getFullName())) {
						body += "<i class=\"fa-solid fa-angle-right\"></i>\r\n" + "<span class=\"respondent\">"
								+ reviewFB.getRespondent() + "</span>\r\n";
					}
					body += "									<div class=\"text-cmtFB\">" + reviewFB.getComment()
							+ "</div>" + "<div class=\"image-cmtFB\">";
					if (reviewFB.getImages() != null) {
						for (String image : reviewFB.getImages()) {
							body += "<img src=\"/images/" + image + "\" width=\"100\" height=\"100\">";
						}
					}
					body += "</div>" + "<label class=\"reply-cmtFB\">\r\n"
							+ "										Trả lời\r\n"
							+ "									</label>\r\n"
							+ "							<span style=\"color: darkgray\">"
							+ new SimpleDateFormat("dd-MM-yyyy", new Locale("Vi", "VN")).format(review.getCreateDate())
							+ "</span>\r\n" + "									<div class=\"feedbackCmtFB\"></div>\r\n"
							+ "</div>";
				}
			}
			body += "</div>" + "</div>";
		}
		body += "|" + reviews.getTotalPages();
		return ResponseEntity.ok(body);
	}

	@PostMapping("/product/review")
	public ResponseEntity<String> reviewProduct(@RequestParam("productId") Long productId,
			@RequestParam("comment") String comment, @RequestParam("rate") double rate,
			@RequestPart("files") Optional<List<MultipartFile>> files) throws IllegalStateException, IOException {
		String body = "";
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
			Account account = null;
			if (auth.getPrincipal() instanceof OAuth2User) {
				OAuth2User user = (OAuth2User) auth.getPrincipal();
				String id = user.getAttribute("id");
				if (id != null) {
					account = daoAccount.findById(id).get();
				} else {
					String email = user.getAttribute("email");
					account = daoAccount.findById(email).get();
				}
			} else {
				account = daoAccount.findById(auth.getName()).get();
			}

			List<String> listImage = null;
			String path = System.getProperty("user.dir") + "/src/main/resources/static/images/";
			if (!Files.exists(Path.of(path))) {
				try {
					Files.createDirectory(Path.of(path));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (files.isPresent()) {
				listImage = new ArrayList<>();
				for (MultipartFile multipartFile : files.get()) {
					multipartFile.transferTo(new File(path + multipartFile.getOriginalFilename()));
					listImage.add(multipartFile.getOriginalFilename());
				}
			}
			
			daoReview.save(
					new Review(comment, listImage, rate, new Date(), account, daoProduct.findById(productId).get()));
			Pageable page = PageRequest.of(0, 10);
			Page<Review> reviews = daoReview.findAllByProductId(daoProduct.findById(productId).get(), page);
			for (Review review : reviews) {
				body += "<div class=\"cmt\" id=\"" + review.getId() + "\">\r\n"
						+ "							<span class=\"name-cmt\">" + review.getAccount().getFullName()
						+ "</span>\r\n" + "							<span class=\"star-cmt\" id=\"" + review.getRate()
						+ "\">" + "</span>\r\n" + "							<div class=\"text-cmt\">"
						+ review.getComment() + "</div>\r\n"
						+ "							<div class=\"image-cmt\">\r\n";
				if (review.getImages() != null) {
					for (String image : review.getImages()) {
						body += "<img src=\"/images/" + image + "\" width=\"100\" height=\"100\">";
					}
				}
				body += "							</div>" + "							<label class=\"reply-cmt\">\r\n"
						+ "								Trả lời\r\n" + "							</label>\r\n"
						+ "							<span style=\"color: darkgray\">"
						+ new SimpleDateFormat("dd-MM-yyyy", new Locale("Vi", "VN")).format(review.getCreateDate())
						+ "</span>\r\n" + "							<div class=\"feedbackCmt\"></div>"
						+ "<div class=\"feedbacks\">";
				for (ReviewFeedback reviewFB : daoReviewFB.findAll()) {
					if (reviewFB.getReview().getId() == review.getId()) {
						body += "<div class=\"cmtFB\" id=\"" + review.getId() + "\">\r\n"
								+ "									<span class=\"name-cmtFB\">"
								+ reviewFB.getAccount().getFullName() + "</span>\r\n";
						if (!reviewFB.getRespondent().equals(review.getAccount().getFullName())) {
							body += "<i class=\"fa-solid fa-angle-right\"></i>\r\n" + "<span class=\"respondent\">"
									+ reviewFB.getRespondent() + "</span>\r\n";
						}
						body += "									<div class=\"text-cmtFB\">" + reviewFB.getComment()
								+ "</div>" + "<div class=\"image-cmtFB\">";
						if (reviewFB.getImages() != null) {
							for (String image : reviewFB.getImages()) {
								body += "<img src=\"/images/" + image + "\" width=\"100\" height=\"100\">";
							}
						}
						body += "</div>" + "<label class=\"reply-cmtFB\">\r\n"
								+ "										Trả lời\r\n"
								+ "									</label>\r\n"
								+ "							<span style=\"color: darkgray\">"
								+ new SimpleDateFormat("dd-MM-yyyy", new Locale("Vi", "VN"))
										.format(review.getCreateDate())
								+ "</span>\r\n"
								+ "									<div class=\"feedbackCmtFB\"></div>\r\n" + "</div>";
					}
				}
				body += "</div>" + "</div>";
			}
			return ResponseEntity.ok(body);
		} else {
			return ResponseEntity.ok("not login");
		}
	}

	@PostMapping("/product/feedback")
	public ResponseEntity<String> feedbackCmt(@RequestParam("reviewId") Long reviewId,
			@RequestParam("cmt") String comment, Optional<List<MultipartFile>> files) throws IOException {
		String body = "";
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
			Account account = null;
			if (auth.getPrincipal() instanceof OAuth2User) {
				OAuth2User user = (OAuth2User) auth.getPrincipal();
				String id = user.getAttribute("id");
				if (id != null) {
					account = daoAccount.findById(id).get();
				} else {
					String email = user.getAttribute("email");
					account = daoAccount.findById(email).get();
				}
			} else {
				account = daoAccount.findById(auth.getName()).get();
			}

			Review review = daoReview.findById(reviewId).get();

			List<String> listImage = null;
			String path = System.getProperty("user.dir") + "/src/main/resources/static/images/";
			if (!Files.exists(Path.of(path))) {
				try {
					Files.createDirectory(Path.of(path));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (files.isPresent()) {
				listImage = new ArrayList<>();
				for (MultipartFile multipartFile : files.get()) {
					multipartFile.transferTo(new File(path + multipartFile.getOriginalFilename()));
					listImage.add(multipartFile.getOriginalFilename());
				}
			}

			daoReviewFB.save(new ReviewFeedback(comment, listImage, new Date(), account, review,
					review.getAccount().getFullName()));
			List<ReviewFeedback> reviewFBs = daoReviewFB.findAll();
			for (ReviewFeedback reviewFB : reviewFBs) {
				if (reviewFB.getReview().getId() == review.getId()) {
					body += "<div class=\"cmtFB\" id=\"" + review.getId() + "\">\r\n"
							+ "									<span class=\"name-cmtFB\">"
							+ reviewFB.getAccount().getFullName() + "</span>\r\n";
					if (!reviewFB.getRespondent().equals(review.getAccount().getFullName())) {
						body += "<i class=\"fa-solid fa-angle-right\"></i>\r\n" + "<span class=\"respondent\">"
								+ reviewFB.getRespondent() + "</span>\r\n";
					}
					body += "									<div class=\"text-cmtFB\">" + reviewFB.getComment()
							+ "</div>" + "<div class=\"image-cmtFB\">";
					if (reviewFB.getImages() != null
							&& reviewFBs.get(reviewFBs.size() - 1).getId() != reviewFB.getId()) {
						for (String image : reviewFB.getImages()) {
							body += "<img src=\"/images/" + image + "\" width=\"100\" height=\"100\">";
						}
					}
					body += "</div>" + "<label class=\"reply-cmtFB\">\r\n"
							+ "										Trả lời\r\n"
							+ "									</label>\r\n"
							+ "							<span style=\"color: darkgray\">"
							+ new SimpleDateFormat("dd-MM-yyyy", new Locale("Vi", "VN")).format(review.getCreateDate())
							+ "</span>\r\n" + "									<div class=\"feedbackCmtFB\"></div>\r\n"
							+ "</div>";
				}
			}
			return ResponseEntity.ok(body);
		} else {
			return ResponseEntity.ok("not login");
		}
	}

	@PostMapping("/product/feedback-comment")
	public ResponseEntity<String> feedbackInCmt(@RequestParam("respondent") String respondent,
			@RequestParam("reviewId") Long reviewId, @RequestParam("cmt") String comment,
			Optional<List<MultipartFile>> files) throws IOException {
		String body = "";
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
			Account account = null;
			if (auth.getPrincipal() instanceof OAuth2User) {
				OAuth2User user = (OAuth2User) auth.getPrincipal();
				String id = user.getAttribute("id");
				if (id != null) {
					account = daoAccount.findById(id).get();
				} else {
					String email = user.getAttribute("email");
					account = daoAccount.findById(email).get();
				}
			} else {
				account = daoAccount.findById(auth.getName()).get();
			}

			Review review = daoReview.findById(reviewId).get();

			List<String> listImage = null;
			String path = System.getProperty("user.dir") + "/src/main/resources/static/images/";
			if (!Files.exists(Path.of(path))) {
				try {
					Files.createDirectory(Path.of(path));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (files.isPresent()) {
				listImage = new ArrayList<>();
				for (MultipartFile multipartFile : files.get()) {
					multipartFile.transferTo(new File(path + multipartFile.getOriginalFilename()));
					listImage.add(multipartFile.getOriginalFilename());
				}
			}

			daoReviewFB.save(new ReviewFeedback(comment, listImage, new Date(), account, review, respondent));
			List<ReviewFeedback> reviewFBs = daoReviewFB.findAll();
			for (ReviewFeedback reviewFB : reviewFBs) {
				if (reviewFB.getReview().getId() == review.getId()) {
					body += "<div class=\"cmtFB\" id=\"" + review.getId() + "\">\r\n"
							+ "									<span class=\"name-cmtFB\">"
							+ reviewFB.getAccount().getFullName() + "</span>\r\n";
					if (!reviewFB.getRespondent().equals(review.getAccount().getFullName())) {
						body += "<i class=\"fa-solid fa-angle-right\"></i>\r\n" + "<span class=\"respondent\">"
								+ reviewFB.getRespondent() + "</span>\r\n";
					}
					body += "									<div class=\"text-cmtFB\">" + reviewFB.getComment()
							+ "</div>" + "<div class=\"image-cmtFB\">";
					if (reviewFB.getImages() != null
							&& reviewFBs.get(reviewFBs.size() - 1).getId() != reviewFB.getId()) {
						for (String image : reviewFB.getImages()) {
							body += "<img src=\"/images/" + image + "\" width=\"100\" height=\"100\">";
						}
					}
					body += "</div>" + "<label class=\"reply-cmtFB\">\r\n"
							+ "										Trả lời\r\n"
							+ "									</label>\r\n"
							+ "							<span style=\"color: darkgray\">"
							+ new SimpleDateFormat("dd-MM-yyyy", new Locale("Vi", "VN")).format(review.getCreateDate())
							+ "</span>\r\n" + "									<div class=\"feedbackCmtFB\"></div>\r\n"
							+ "</div>";
				}
			}
			return ResponseEntity.ok(body);
		} else {
			return ResponseEntity.ok("not login");
		}
	}
}
