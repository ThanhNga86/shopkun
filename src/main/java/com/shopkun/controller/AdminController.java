package com.shopkun.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.shopkun.dao.AccountDao;
import com.shopkun.dao.CategoryDao;
import com.shopkun.dao.OrderDao;
import com.shopkun.dao.OrderDetailDao;
import com.shopkun.dao.ProductDao;
import com.shopkun.dao.ReviewDao;
import com.shopkun.dao.ReviewFeedbackDao;
import com.shopkun.entity.Account;
import com.shopkun.entity.Category;
import com.shopkun.entity.Order;
import com.shopkun.entity.OrderDetail;
import com.shopkun.entity.Product;
import com.shopkun.entity.Review;
import com.shopkun.entity.ReviewFeedback;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/admin")
public class AdminController {
	@Autowired
	private CategoryDao daoCategory;
	@Autowired
	private ProductDao daoProduct;
	@Autowired
	private AccountDao daoAccount;
	@Autowired
	private OrderDao daoOrder;
	@Autowired
	private ReviewDao daoReview;
	@Autowired
	private ReviewFeedbackDao daoReviewFB;
	@Autowired
	private OrderDetailDao daoOrderDetail;
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private BCryptPasswordEncoder encoder;
	
	@GetMapping
	public String dashboard(Model model) {
		model.addAttribute("totalUsers", daoAccount.findAll().size());
		model.addAttribute("totalProducts", daoProduct.findAll().size());
		model.addAttribute("totalOrders", daoOrder.findAll().size());
		model.addAttribute("totalReviews", daoReview.findAll().size());

		// Thống kê doanh thu và tổng đã bán
		model.addAttribute("revenueToday",
				daoOrderDetail.revenueToday("Đã giao") == null ? 0l : daoOrderDetail.revenueToday("Đã giao"));
		model.addAttribute("soldToday",
				daoOrderDetail.soldToday("Đã giao") == null ? 0l : daoOrderDetail.soldToday("Đã giao"));

		model.addAttribute("revenueWeek",
				daoOrderDetail.revenueWeek("Đã giao") == null ? 0l : daoOrderDetail.revenueWeek("Đã giao"));
		model.addAttribute("soldWeek",
				daoOrderDetail.soldWeek("Đã giao") == null ? 0l : daoOrderDetail.soldWeek("Đã giao"));

		model.addAttribute("revenueMonth",
				daoOrderDetail.revenueMonth("Đã giao") == null ? 0l : daoOrderDetail.revenueMonth("Đã giao"));
		model.addAttribute("soldMonth",
				daoOrderDetail.soldMonth("Đã giao") == null ? 0l : daoOrderDetail.soldMonth("Đã giao"));

		model.addAttribute("totalRevenue",
				daoOrderDetail.totalRevenue("Đã giao") == null ? 0l : daoOrderDetail.totalRevenue("Đã giao"));
		model.addAttribute("totalSold",
				daoOrderDetail.totalSold("Đã giao") == null ? 0l : daoOrderDetail.totalSold("Đã giao"));

		// Top sản phẩm bán chạy
		int sizePage = 20;
		Pageable page = PageRequest.of(0, sizePage);
		model.addAttribute("sellingProducts", daoOrderDetail.findTopSellingProducts("Đã giao", page));
		model.addAttribute("totalSellingProducts",
				daoOrderDetail.findTopSellingProducts("Đã giao", page).getTotalElements());
		return "admin/dashboard/dashboard";
	}

	// danh sách tài khoản
	@GetMapping("/users")
	public String users(Model model, @RequestParam("page") Optional<Integer> pageNumber) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Account account = null;
		if (auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
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
		}

		int sizePage = 10;
		Pageable page = PageRequest.of(pageNumber.orElse(1) - 1, sizePage);
		Page<Account> accounts = daoAccount.findAllOtherAdmin(account, page);
		long total = accounts.getTotalElements();
		int totalPage = (int) (total / sizePage);
		if (total % sizePage != 0) {
			totalPage++;
		}

		model.addAttribute("accounts", accounts);
		model.addAttribute("total", total);
		model.addAttribute("totalPage", totalPage);
		model.addAttribute("pageNumber", pageNumber.orElse(0));
		String iconRight = " <i style=\"color: darkgray; font-size: 14px;\" class=\"fa-solid fa-chevron-right\"></i> ";
		model.addAttribute("instruction", iconRight + "<a href=\"/admin/users\">khách hàng</a>");
		return "admin/users/users";
	}

	// cập nhật tài khoản
	@PostMapping("/update-account")
	public ResponseEntity<String> updateAccount(Account account) {
		String body = "";
		Optional<Account> checkAccount = daoAccount.findById(account.getUsername());
		if (checkAccount.isPresent()) {
			if (!account.getPassword().equals(checkAccount.get().getPassword())) {
				account.setPassword(encoder.encode(account.getPassword()));
			}
			daoAccount.saveAndFlush(account);
			body = "success";
		} else {
			body = "notUsername";
		}
		return ResponseEntity.ok(body);
	}

	// xóa tài khoản
	@GetMapping("/delete-account")
	public ResponseEntity<String> deleteAccount(@RequestParam("username") String username) {
		String body = "";
		Optional<Account> account = daoAccount.findById(username);
		if (account.isPresent()) {
			try {
				daoAccount.delete(account.get());
				body = "success";
			} catch (Exception e) {
				body = "errorr";
			}
		}
		return ResponseEntity.ok(body);
	}

	// tìm kiếm tài khoản
	@GetMapping("/search-account")
	public ResponseEntity<String> searchAccount(@RequestParam("query") String query) {
		String body = "";
		query = query.trim();
		List<Account> accounts = new ArrayList<>();
		accounts = daoAccount.findAllAccount("%" + query + "%");

		if (accounts.size() != 0) {
			if (query.isEmpty()) {
				accounts = accounts.subList(0, 10);
			}

			body += "<table class=\"table table-bordered mt-2 text-center\">\r\n" + "					<tr>\r\n"
					+ "						<th>Tên đăng nhập</th>\r\n" + "						<th>Mật khẩu</th>\r\n"
					+ "						<th>Họ và tên</th>\r\n" + "						<th>Địa chỉ</th>\r\n"
					+ "						<th>Email</th>\r\n" + "						<th>Số điện thoại</th>\r\n"
					+ "						<th>Trạng thái</th>\r\n" + "						<th>Vai trò</th>\r\n"
					+ "						<th>Hành động</th>\r\n" + "					</tr>";
			for (Account account : accounts) {
				body += "<tr>\r\n" + "						<td class=\"accountId\">" + account.getUsername()
						+ "</td>\r\n" + "						<td class=\"password\">********</td>\r\n"
						+ "						<td class=\"fullName\">" + account.getFullName() + "</td>\r\n";
				if (account.getAddress() != null) {
					body += "						<td class=\"address\">" + account.getAddress() + "</td>\r\n";
				} else {
					body += "						<td class=\"address\"></td>\r\n";
				}

				if (account.getEmail() != null) {
					body += "						<td class=\"email\">" + account.getEmail() + "</td>\r\n";
				} else {
					body += "						<td class=\"email\"></td>\r\n";
				}
				if (account.getPhone() != null) {
					body += "						<td class=\"phone\">" + account.getPhone() + "</td>\r\n";
				} else {
					body += "						<td class=\"phone\"></td>\r\n";
				}
				body += "						<td class=\"status\">\r\n";
				if (account.isActivated()) {
					body += "							<span class=\"active\">Hoạt động</span>\r\n";
				} else {

					body += "							<span class=\"unActive\">Khóa</span>\r\n";
				}
				body += "						</td>\r\n" + "						<td class=\"role\">"
						+ account.getRole().replace("ROLE_", "") + "</td>\r\n"
						+ "						<td class=\"action\">\r\n"
						+ "							<span class=\"edit\" id=\"" + account.getUsername()
						+ "\" data-bs-toggle=\"modal\" data-bs-target=\"#editAccount_" + account.getUsername()
						+ "\"><i class=\"fa-solid fa-pen\"></i> Sửa</span>\r\n"
						+ "							<div class=\"data-save modal fade\" id=\"editAccount_"
						+ account.getUsername()
						+ "\" tabindex=\"-1\" aria-labelledby=\"exampleModalLabel\" aria-hidden=\"true\">\r\n"
						+ "							  <div class=\"modal-dialog\">\r\n"
						+ "							    <div class=\"modal-content\">\r\n"
						+ "							      <div class=\"modal-header\">\r\n"
						+ "							        <h1 class=\"modal-title fs-5\" id=\"exampleModalLabel\">Cập nhật tài khoản</h1>\r\n"
						+ "							        <button type=\"button\" class=\"btn-close\" data-bs-dismiss=\"modal\" aria-label=\"Close\"></button>\r\n"
						+ "							      </div>\r\n"
						+ "							      <div class=\"data-product modal-body\">\r\n"
						+ "							        <form>\r\n"
						+ "									    <div class=\"form-group\">\r\n"
						+ "											<label>Tên đăng nhập</label>\r\n"
						+ "											<input type=\"text\" class=\"inpUsername form-control\" value=\""
						+ account.getUsername() + "\">\r\n"
						+ "											<div class=\"message msUsername\"></div>\r\n"
						+ "										</div>\r\n"
						+ "										\r\n"
						+ "										<div class=\"form-group\">\r\n"
						+ "											<label>Mật khẩu</label>\r\n";
				if (account.getPassword() != null) {
					body += "											<input type=\"password\" class=\"inpPassword form-control\" value=\""
							+ account.getPassword() + "\">\r\n";
				} else {
					body += "											<input type=\"password\" class=\"inpPassword form-control\">\r\n";
				}
				body += "											<div class=\"message msPassword\"></div>\r\n"
						+ "										</div>\r\n"
						+ "										\r\n"
						+ "										<div class=\"form-group\">\r\n"
						+ "											<label>Họ và tên</label>\r\n"
						+ "											<input type=\"text\" class=\"inpFullName form-control\" value=\""
						+ account.getFullName() + "\">\r\n"
						+ "											<div class=\"message msFullName\"></div>\r\n"
						+ "										</div>\r\n"
						+ "										\r\n"
						+ "										<div class=\"form-group\">\r\n"
						+ "											<label>Địa chỉ</label>\r\n";
				if (account.getAddress() != null) {
					body += "											<input type=\"text\" class=\"inpAddress form-control\" value=\""
							+ account.getAddress() + "\">\r\n";
				} else {
					body += "											<input type=\"text\" class=\"inpAddress form-control\">\r\n";
				}
				body += "											<div class=\"message msAddress\"></div>\r\n"
						+ "										</div>\r\n"
						+ "										\r\n"
						+ "										<class class=\"form-group\">\r\n"
						+ "											<label>Email</label>\r\n";
				if (account.getEmail() != null) {
					body += "											<input type=\"text\" class=\"inpEmail form-control\" value=\""
							+ account.getEmail() + "\">\r\n";
				} else {
					body += "											<input type=\"text\" class=\"inpEmail form-control\">\r\n";
				}
				body += "											<div class=\"message msEmail\"></div>\r\n"
						+ "										</class>\r\n"
						+ "										\r\n"
						+ "										<div class=\"form-group\">\r\n"
						+ "											<label>Số điện thoại</label>\r\n";
				if (account.getPhone() != null) {
					body += "											<input type=\"number\" class=\"inpPhone form-control\" value=\""
							+ account.getPhone() + "\">\r\n";
				} else {
					body += "											<input type=\"number\" class=\"inpPhone form-control\">\r\n";
				}
				body += "											<div class=\"message msPhone\"></div>\r\n"
						+ "										</div>\r\n"
						+ "										\r\n"
						+ "										<div class=\"form-group\">\r\n"
						+ "											<label>Trạng thái</label>\r\n";
				if (account.isActivated()) {
					body += "											<select class=\"inpActivated form-select\">\r\n"
							+ "												<option value=\"true\">Hoạt động</option>\r\n"
							+ "												<option value=\"false\">Khóa</option>\r\n"
							+ "											</select>\r\n";
				} else {
					body += "											<select class=\"inpActivated form-select\">\r\n"
							+ "												<option value=\"true\">Hoạt động</option>\r\n"
							+ "												<option value=\"false\" selected>Khóa</option>\r\n"
							+ "											</select>\r\n";
				}
				body += "											<div class=\"message msActivated\"></div>\r\n"
						+ "										</div>\r\n"
						+ "										\r\n"
						+ "										<div class=\"form-group\">\r\n"
						+ "											<label>Vai trò</label>\r\n";
				if (account.getRole().equals("ROLE_ADMIN")) {
					body += "											<select class=\"inpRole form-select\">\r\n"
							+ "												<option value=\"ROLE_ADMIN\">ADMIN</option>\r\n"
							+ "												<option value=\"ROLE_USER\">USER</option>\r\n"
							+ "											</select>\r\n";
				} else {
					body += "											<select class=\"inpRole form-select\">\r\n"
							+ "												<option value=\"ROLE_ADMIN\">ADMIN</option>\r\n"
							+ "												<option value=\"ROLE_USER\" selected>USER</option>\r\n"
							+ "											</select>\r\n";
				}
				body += "											<div class=\"message msActivated\"></div>\r\n"
						+ "										</div>\r\n"
						+ "									</form>\r\n"
						+ "							      </div>\r\n"
						+ "							      <div class=\"modal-footer\">\r\n"
						+ "							        <button type=\"button\" class=\"btn btn-secondary btnReset\" data-bs-dismiss=\"modal\">Hủy</button>\r\n"
						+ "							        <button type=\"button\" class=\"btn btn-primary btnSave\">Lưu thay đổi</button>\r\n"
						+ "							      </div>\r\n" + "							    </div>\r\n"
						+ "							  </div>\r\n" + "							</div>\r\n"
						+ "							\r\n" + "							<span class=\"remove\" id=\""
						+ account.getUsername() + "\"><i class=\"fa-solid fa-trash-can\"></i> Xóa</span>\r\n"
						+ "						</td>\r\n" + "					</tr>";
			}
			body += "</table>";
		} else {
			body += "<div class=\"resultQ\">Kết quả tìm kiếm không có tài khoản nào!</div>";
		}
		return ResponseEntity.ok(body);
	}

	// danh sách sản phẩm
	@GetMapping("/products")
	public String products(Model model, @RequestParam("page") Optional<Integer> pageNumber,
			@RequestParam("filter") Optional<String> filter) {
		int sizePage = 10;
		Pageable page = PageRequest.of(pageNumber.orElse(1) - 1, sizePage, Sort.by("id").reverse());
		if (!filter.isPresent()) {
			Page<Product> products = daoProduct.findAll(page);
			long total = products.getTotalElements();
			int totalPage = (int) (total / sizePage);
			if (total % sizePage != 0) {
				totalPage++;
			}

			model.addAttribute("pageFilter", "");
			model.addAttribute("products", products);
			model.addAttribute("total", total);
			model.addAttribute("totalPage", totalPage);
			model.addAttribute("pageNumber", pageNumber.orElse(0));
		} else {
			Page<Product> products = null;
			String nameFilter = "";
			if (filter.get().equals("promotion")) {
				products = daoProduct.findAllByPromotion(null, page);
				nameFilter = "Lọc theo giá khuyến mãi";
			}
			if (filter.get().equals("notPromotion")) {
				products = daoProduct.findAllByNotPromotion(null, page);
				nameFilter = "Lọc theo giá không khuyến mãi";
			}
			if (filter.get().equals("stocking")) {
				products = daoProduct.findAllByStocking(null, page);
				nameFilter = "Lọc theo sản phẩm còn hàng";
			}
			if (filter.get().equals("outStock")) {
				products = daoProduct.findAllByOutStock(null, page);
				nameFilter = "Lọc theo sản phẩm hết hàng";
			}
			if (filter.get().startsWith("category_")) {
				String subCategoryId = filter.get().substring(filter.get().indexOf("_") + 1, filter.get().length());
				Optional<Category> categoryId = daoCategory.findById(Long.parseLong(subCategoryId));
				if (categoryId.isPresent()) {
					products = daoProduct.findAllByCategoryId(null, categoryId.get(), page);
					nameFilter = "Lọc theo thể loại " + categoryId.get().getName();
				}
			}

			long total = products.getTotalElements();
			int totalPage = (int) (total / sizePage);
			if (total % sizePage != 0) {
				totalPage++;
			}

			model.addAttribute("filter", filter.get());
			model.addAttribute("nameFilter", nameFilter);
			model.addAttribute("pageFilter", "filter=" + filter.get() + "&");
			model.addAttribute("products", products);
			model.addAttribute("total", total);
			model.addAttribute("totalPage", totalPage);
			model.addAttribute("pageNumber", pageNumber.orElse(0));
		}

		String iconRight = " <i style=\"color: darkgray; font-size: 14px;\" class=\"fa-solid fa-chevron-right\"></i> ";
		model.addAttribute("instruction", iconRight + "<a href=\"/admin/products\">sản phẩm</a>");
		return "admin/products/products";
	}

	// thêm sản phẩm
	@GetMapping("/products/addProduct")
	public String addProduct(Model model) {
		String iconRight = " <i style=\"color: darkgray; font-size: 14px;\" class=\"fa-solid fa-chevron-right\"></i> ";
		model.addAttribute("instruction", iconRight + "<a href=\"/admin/products\">sản phẩm</a>" + iconRight
				+ "<a href=\"/admin/products/addProduct\">thêm sản phẩm</a>");
		return "admin/products/addProduct";
	}

	@PostMapping("/products/addProduct")
	public ResponseEntity<String> addProduct(@RequestParam("poster") MultipartFile poster,
			@RequestParam("images") Optional<List<MultipartFile>> images,
			@RequestParam("describe") List<String> listDescribe) throws IllegalStateException, IOException {
		String path = System.getProperty("user.dir") + "/src/main/resources/static/images/";
		if (!Files.exists(Path.of(path))) {
			try {
				Files.createDirectory(Path.of(path));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		String name = request.getParameter("name");
		Double price = Double.parseDouble(request.getParameter("price"));
		Integer quantity = Integer.parseInt(request.getParameter("quantity"));
		Boolean promotion = Boolean.parseBoolean(request.getParameter("promotion"));
		Integer discount = Integer
				.parseInt(request.getParameter("discount") != null ? request.getParameter("discount") : "0");
		Long categoryId = Long.parseLong(request.getParameter("category"));
		Category category = daoCategory.findById(categoryId).get();

		List<String> listImage = new ArrayList<>();
		listImage.add(poster.getOriginalFilename());
		poster.transferTo(new File(path + poster.getOriginalFilename()));

		if (images.isPresent()) {
			for (MultipartFile image : images.get()) {
				listImage.add(image.getOriginalFilename());
				image.transferTo(new File(path + image.getOriginalFilename()));
			}
		}

		daoProduct.save(
				new Product(name, listImage, price, discount, quantity, listDescribe, promotion, new Date(), category));
		return ResponseEntity.ok("success");
	}

	// cập nhật sản phẩm
	@PostMapping("/update-product")
	public ResponseEntity<String> updateProduct(@RequestParam("productId") Long productId,
			@RequestParam("poster") Optional<MultipartFile> poster,
			@RequestParam("images") Optional<List<MultipartFile>> images,
			@RequestParam("nameImagesOld") Optional<List<String>> nameImagesOld,
			@RequestParam("describe") List<String> listDescribe) throws IllegalStateException, IOException {
		String path = System.getProperty("user.dir") + "/src/main/resources/static/images/";
		if (!Files.exists(Path.of(path))) {
			try {
				Files.createDirectory(Path.of(path));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		String name = request.getParameter("name");
		Double price = Double.parseDouble(request.getParameter("price"));
		Integer quantity = Integer.parseInt(request.getParameter("quantity"));
		Boolean promotion = Boolean.parseBoolean(request.getParameter("promotion"));
		Integer discount = Integer
				.parseInt(request.getParameter("discount") != null ? request.getParameter("discount") : "0");
		Long categoryId = Long.parseLong(request.getParameter("category"));
		Category category = daoCategory.findById(categoryId).get();

		Product product = daoProduct.findById(productId).get();
		List<String> listImage = new ArrayList<>();
		if (poster.isPresent()) {
			listImage.add(poster.get().getOriginalFilename());
			poster.get().transferTo(new File(path + poster.get().getOriginalFilename()));
		} else {
			listImage.add(product.getImages().get(0));
		}

		if (nameImagesOld.isPresent()) {
			for (String imgOld : nameImagesOld.get()) {
				listImage.add(imgOld);
			}
		}

		if (images.isPresent()) {
			for (MultipartFile image : images.get()) {
				listImage.add(image.getOriginalFilename());
				image.transferTo(new File(path + image.getOriginalFilename()));
			}
		}

		product.setName(name);
		product.setImages(listImage);
		product.setPrice(price);
		product.setQuantity(quantity);
		product.setDescribes(listDescribe);
		product.setPromotion(promotion);
		product.setDiscount(discount);
		product.setCategory(category);
		product.setCreateDate(new Date());
		daoProduct.saveAndFlush(product);
		return ResponseEntity.ok("success");
	}

	// xóa sản phẩm
	@GetMapping("/delete-product")
	public ResponseEntity<String> deleteProduct(@RequestParam("productId") Long productId) {
		String body = "";
		Optional<Product> product = daoProduct.findById(productId);
		if (product.isPresent()) {
			try {
				daoProduct.delete(product.get());
				body = "success";
			} catch (Exception e) {
				body = "error";
			}
		}
		return ResponseEntity.ok(body);
	}

	// tìm kiếm sản phẩm theo id và name
	@GetMapping("/search-product")
	public ResponseEntity<String> searchProduct(@RequestParam("query") String query,
			@RequestParam("dataFilter") String filter) {
		String body = "";
		query = query.trim();
		NumberFormat numb = NumberFormat.getInstance(new Locale("Vi", "VN"));
		SimpleDateFormat simpDate = new SimpleDateFormat("dd-MM-yyyy");
		List<Product> products = new ArrayList<>();

		if (filter != null && !filter.isEmpty()) {
			try {
				Product product = null;
				Long productId = Long.parseLong(query);
				if (filter.equals("promotion")) {
					product = daoProduct.filterByIdPromotion(productId);
				}
				if (filter.equals("notPromotion")) {
					product = daoProduct.filterByIdNotPromotion(productId);
				}
				if (filter.equals("stocking")) {
					product = daoProduct.filterByIdStocking(productId);
				}
				if (filter.equals("outStock")) {
					product = daoProduct.filterByIdOutStock(productId);
				}
				if (filter.startsWith("category_")) {
					String subCategoryId = filter.substring(filter.indexOf("_") + 1, filter.length());
					product = daoProduct.filterByCategory(productId, Long.parseLong(subCategoryId));
				}

				if (product != null) {
					products.add(product);
				}
			} catch (NumberFormatException e) {
				if (filter.equals("promotion")) {
					products = daoProduct.filterByPromotion("%" + query + "%");
				}
				if (filter.equals("notPromotion")) {
					products = daoProduct.filterByNotPromotion("%" + query + "%");
				}
				if (filter.equals("stocking")) {
					products = daoProduct.filterByStocking("%" + query + "%");
				}
				if (filter.equals("outStock")) {
					products = daoProduct.filterByOutStock("%" + query + "%");
				}
				if (filter.startsWith("category_")) {
					String subCategoryId = filter.substring(filter.indexOf("_") + 1, filter.length());
					products = daoProduct.filterByCategory("%" + query + "%", Long.parseLong(subCategoryId));
				}
			}
		} else {
			try {
				Optional<Product> product = daoProduct.findById(Long.parseLong(query));
				if (product.isPresent()) {
					products.add(product.get());
				}
			} catch (NumberFormatException e) {
				products = daoProduct.findAllByNameAdmin("%" + query + "%");
			}
		}

		if (products.size() != 0) {
			if (query.isEmpty()) {
				if (products.size() > 10) {
					products = products.subList(0, 10);
				}
			}
			body += "<table class=\"table table-bordered mt-2 text-center\">\r\n" + "					<tr>\r\n"
					+ "						<th>Mã số</th>\r\n" + "						<th>Tên sản phẩm</th>\r\n"
					+ "						<th>Hình ảnh</th>\r\n" + "						<th>Giá</th>\r\n"
					+ "						<th>Khuyễn mãi</th>\r\n" + "						<th>Số lượng</th>\r\n"
					+ "						<th>Mô tả</th>\r\n" + "						<th>Ngày tạo</th>\r\n"
					+ "						<th>Thể loại</th>\r\n" + "						<th>Hành động</th>\r\n"
					+ "					</tr>";
			for (Product product : products) {
				body += "<tr>\r\n" + "						<td class=\"productId\">" + product.getId() + "</td>\r\n"
						+ "						<td class=\"name\">" + product.getName() + "</td>\r\n"
						+ "						<td class=\"image\">\r\n"
						+ "							<div class=\"viewDetailImage\">\r\n"
						+ "								<image src=\"/images/" + product.getImages().get(0)
						+ "\" width=\"100%\" height=\"100%\"></image>\r\n"
						+ "								<div class=\"viewImage\">Xem " + product.getImages().size()
						+ " hình</div>\r\n" + "							</div>\r\n" + "						</td>\r\n"
						+ "						<td class=\"priceAndDel\">\r\n"
						+ "							<div class=\"price\">"
						+ numb.format(product.getPrice() * (100 - product.getDiscount()) / 100) + " đ</div>\r\n";
				if (product.isPromotion()) {
					body += "							<del class=\"price-del\">" + numb.format(product.getPrice())
							+ " đ</del>\r\n";
				}
				body += "						</td>\r\n" + "						<td class=\"promotion\">\r\n";
				if (product.isPromotion()) {
					body += "							<span>" + product.getDiscount() + " %</span>\r\n";
				}
				body += "						</td>\r\n";
				if (product.getQuantity() == 0) {
					body += "						<td class=\"quantity\"><span class=\"out-stock\">Hết hàng</span></td>\r\n";
				} else {
					body += "						<td class=\"quantity\">" + product.getQuantity() + "</td>\r\n";
				}
				body += "						<td class=\"viewDescribe\"><span class=\"viewD\"><i class=\"fa-solid fa-eye\"></i> Xem</span></td>\r\n"
						+ "						<td class=\"createDate\">" + simpDate.format(product.getCreateDate())
						+ "</td>\r\n" + "						<td class=\"nameC\">" + product.getCategory().getName()
						+ "</td>\r\n" + "						<td class=\"action\">\r\n"
						+ "							<span class=\"edit\" data-bs-toggle=\"modal\" data-bs-target=\"#editProduct_"
						+ product.getId() + "\"><i class=\"fa-solid fa-pen\"></i> Sửa</span>\r\n"
						+ "							<div class=\"modal fade\" id=\"editProduct_" + product.getId()
						+ "\" tabindex=\"-1\" aria-labelledby=\"exampleModalLabel\" aria-hidden=\"true\">\r\n"
						+ "							  <div class=\"modal-dialog\">\r\n"
						+ "							    <div class=\"modal-content\">\r\n"
						+ "							      <div class=\"modal-header\">\r\n"
						+ "							        <h1 class=\"modal-title fs-5\" id=\"exampleModalLabel\">Cập nhật sản phẩm</h1>\r\n"
						+ "							        <button type=\"button\" class=\"btn-close btnCloseP\" data-bs-dismiss=\"modal\" aria-label=\"Close\"></button>\r\n"
						+ "							      </div>\r\n"
						+ "							      <div class=\"data-product modal-body\">\r\n"
						+ "									 <div class=\"form-group\">\r\n"
						+ "										<label>Tên sản phẩm</label>\r\n"
						+ "										<input type=\"text\" name=\"name\" class=\"inpName form-control\" value=\""
						+ product.getName() + "\">\r\n"
						+ "										<div class=\"message msName\"></div>\r\n"
						+ "									</div>\r\n" + "									\r\n"
						+ "									<div class=\"form-group\">\r\n"
						+ "										<label>Ảnh đại diện</label>\r\n"
						+ "										<div class=\"ctnPoster\">\r\n"
						+ "											<i class=\"fa-solid fa-x removePoster\"></i>\r\n"
						+ "											<img class=\"poster\" src=\"/images/"
						+ product.getImages().get(0) + "\" width=\"100%\" height=\"100%\">\r\n"
						+ "										</div>\r\n"
						+ "										<input type=\"file\" class=\"file\" name=\"poster\" style=\"display: none;\">\r\n"
						+ "										<div class=\"message msImage\"></div>\r\n"
						+ "									</div>\r\n" + "									\r\n"
						+ "									<div class=\"form-group\">\r\n"
						+ "										<label>Thêm nhiều ảnh khác</label>\r\n"
						+ "										<input type=\"file\" class=\"files form-control\" style=\"display: none;\" name=\"images\" multiple=\"multiple\">\r\n"
						+ "										<br><label class=\"iconImage\"><i class=\"fa-regular fa-image\"></i> chọn ảnh</label>\r\n"
						+ "										<div class=\"show-imagesP mt-2 row\">\r\n";
				for (int i = 0; i < product.getImages().size(); i++) {
					if (i > 0) {
						body += "											<div class=\"show-file\" id=\"old_" + i
								+ "\">\r\n"
								+ "												<button type=\"button\" class=\"btnRmImage btn-close\" aria-label=\"Close\"></button>\r\n"
								+ "												<img class=\"imgsP\" src=\"/images/"
								+ product.getImages().get(i) + "\" width=\"100%\">\r\n"
								+ "											</div>\r\n";
					}
				}
				body += "										</div>\r\n"
						+ "									</div>\r\n"
						+ "									<div class=\"form-group\">\r\n"
						+ "										<label>Giá sản phẩm</label>\r\n"
						+ "										<input class=\"inpPrice form-control\" type=\"number\" name=\"price\" value=\""
						+ (int) product.getPrice() + "\">\r\n"
						+ "										<div class=\"message msPrice\"></div>\r\n"
						+ "									</div>\r\n" + "									\r\n"
						+ "									<div class=\"form-group\">\r\n"
						+ "										<label>Số lượng sản phẩm</label>\r\n"
						+ "										<input class=\"inpQuantity form-control\" type=\"number\" name=\"quantity\" value=\""
						+ product.getQuantity() + "\">\r\n"
						+ "										<div class=\"message msQuantity\"></div>\r\n"
						+ "									</div>\r\n" + "									\r\n"
						+ "									<div class=\"form-group\">\r\n"
						+ "										<label>Mô tả sản phẩm</label>\r\n"
						+ "										<div class=\"btnAddDescribe\">Thêm mô tả</div>\r\n"
						+ "										<div class=\"ctnDescribe row\">\r\n";
				String typeD = product.getDescribes().get(0).substring(0, product.getDescribes().get(0).indexOf("|"));
				String describe = product.getDescribes().get(0).substring(
						product.getDescribes().get(0).indexOf("|") + 1, product.getDescribes().get(0).length());
				body += "											<input value=" + typeD
						+ " class=\"describeId form-control\" type=\"text\" name=\"describeId\" placeholder=\"Loại mô tả\">\r\n"
						+ "											<input value=\"" + describe
						+ "\" class=\"describe form-control\" type=\"text\" name=\"describe\" placeholder=\"Mô tả sản phẩm\">\r\n";
				for (int i = 0; i < product.getDescribes().size(); i++) {
					if (i > 0 && product.getDescribes().get(i).contains("|")) {
						String type = product.getDescribes().get(i).substring(0,
								product.getDescribes().get(i).indexOf("|"));
						String d = product.getDescribes().get(i).substring(
								product.getDescribes().get(i).indexOf("|") + 1, product.getDescribes().get(i).length());
						body += "											<div class=\"addDescribe\">\r\n"
								+ "												<input value=\"" + type
								+ "\" type=\"text\" class=\"describeId form-control mt-1\" name=\"describeId\" placeholder=\"Loại mô tả\">\r\n"
								+ "												<input value=\"" + d
								+ "\" type=\"text\" class=\"describe form-control mt-1\" name=\"describe\" placeholder=\"Mô tả sản phẩm\" style=\"margin-left: 4px;\">\r\n"
								+ "												<div class=\"btnRemoveDescribe\">Xóa</div>\r\n"
								+ "												</div>";
					}
				}
				body += "										</div>\r\n"
						+ "										<div class=\"message msDescribe\"></div>\r\n"
						+ "									</div>\r\n" + "									\r\n"
						+ "									<div class=\"form-group\">\r\n"
						+ "										<label>Khuyến mãi sản phẩm</label>\r\n";
				if (product.isPromotion()) {
					body += "										<select class=\"promotionP form-control\" name=\"promotion\">\r\n"
							+ "											<option value=\"false\">Không khuyến mãi</option>\r\n"
							+ "											<option value=\"true\" selected>Có khuyến mãi</option>\r\n"
							+ "										</select>\r\n";
				}
				if (!product.isPromotion()) {
					body += "										<select class=\"promotionP form-control\" name=\"promotion\">\r\n"
							+ "											<option value=\"false\">Không khuyến mãi</option>\r\n"
							+ "											<option value=\"true\">Có khuyến mãi</option>\r\n"
							+ "										</select>\r\n";
				}
				body += "									</div>\r\n" + "									\r\n"
						+ "									<div class=\"form-group mt-2\">\r\n";
				if (product.isPromotion()) {
					body += "										<div class=\"price-promotion\">\r\n"
							+ "											<input value=\"" + product.getDiscount()
							+ "\" class=\"inpDiscount form-control w-50\" type=\"number\" name=\"discount\" placeholder=\"Khuyến mãi phần trăm sản phẩm\">\r\n"
							+ "											<span class=\"dcPrice\">"
							+ numb.format(product.getPrice() * (100 - product.getDiscount()) / 100) + " đ</span>\r\n"
							+ "										</div>\r\n";
				}
				if (!product.isPromotion()) {
					body += "										<div class=\"price-promotion\">\r\n"
							+ "											<input value=\"" + product.getDiscount()
							+ "\" class=\"inpDiscount form-control w-50\" type=\"number\" name=\"discount\" placeholder=\"Khuyến mãi phần trăm sản phẩm\">\r\n"
							+ "											<span class=\"dcPrice\">"
							+ numb.format(product.getPrice() * (100 - product.getDiscount()) / 100) + " đ</span>\r\n"
							+ "										</div>\r\n";
				}
				body += "										<div class=\"message msDiscount\"></div>\r\n"
						+ "									</div>\r\n" + "									\r\n"
						+ "									<div class=\"form-group\">\r\n"
						+ "										<label>Thể loại sản phẩm</label>\r\n"
						+ "										<select class=\"category form-select\" name=\"categoryId\">\r\n"
						+ "											<option value=\"0\" hidden>Chọn loại sản phẩm</option>\r\n";
				for (Category c : daoCategory.findAll()) {
					if (c.getId() == product.getCategory().getId()) {
						body += "											<option selected value=\"" + c.getId()
								+ "\">" + c.getName() + "</option>\r\n";
					}
				}
				for (Category c : daoCategory.findAll()) {
					if (c.getId() != product.getCategory().getId()) {
						body += "											<option value=\"" + c.getId() + "\">"
								+ c.getName() + "</option>\r\n";
					}
				}
				body += "										</select>\r\n"
						+ "										<div class=\"message msCategory\"></div>\r\n"
						+ "									</div>\r\n" + "							      </div>\r\n"
						+ "							      <div class=\"modal-footer\">\r\n"
						+ "							        <button type=\"button\" class=\"btn btn-secondary btnReset\" data-bs-dismiss=\"modal\">Hủy</button>\r\n"
						+ "							        <button type=\"button\" class=\"btn btn-primary btnSave\" id=\""
						+ product.getId() + "\">Lưu thay đổi</button>\r\n"
						+ "							      </div>\r\n" + "							    </div>\r\n"
						+ "							  </div>\r\n" + "							</div>\r\n"
						+ "							<span class=\"remove\" id=\"" + product.getId()
						+ "\"><i class=\"fa-solid fa-trash-can\"></i> Xóa</span>\r\n"
						+ "							<a class=\"see\" href=\"/product?id=" + product.getId() + "&name="
						+ product.getName() + "\">\r\n"
						+ "								<i class=\"fa-solid fa-eye\"></i> Xem\r\n"
						+ "							</a>\r\n" + "					    </td>\r\n"
						+ "					    \r\n" + "					    <!--Show nhiều hình ảnh-->\r\n"
						+ "					    <div class=\"show-detailImage\">\r\n"
						+ "							<i class=\"hidden-detailImage fa-solid fa-x\"></i>\r\n"
						+ "						</div>\r\n" + "					    <div class=\"show-images\">\r\n"
						+ "							<div class=\"row\">\r\n";
				for (String image : product.getImages()) {
					body += "								<div class=\"col-xl-7 col-lg-8 col-md-9 col-sm-10\">\r\n"
							+ "									<img class=\"imagesP\" src=\"/images/" + image
							+ "\" width=\"100%\" height=\"100%\">\r\n" + "								</div>\r\n";
				}
				body += "							</div>\r\n" + "						</div>\r\n"
						+ "						\r\n" + "						<!--Show mô tả-->\r\n"
						+ "						<div class=\"show-describe\">\r\n"
						+ "							<i class=\"hidden-describe fa-solid fa-x\"></i>\r\n"
						+ "						</div>\r\n" + "						<div class=\"show-describes\">\r\n"
						+ "							<div class=\"row\">\r\n";
				for (String d : product.getDescribes()) {
					if (d.contains("|")) {
						body += "								<div>\r\n"
								+ "									<div class=\"type\">"
								+ d.substring(0, d.indexOf("|")) + "</div>\r\n"
								+ "									<div class=\"mx-2\">"
								+ d.substring(d.indexOf("|") + 1, d.length()) + "</div>\r\n"
								+ "								</div>\r\n";
					}
				}
				body += "							</div>\r\n" + "						</div>\r\n"
						+ "					</tr>";
			}
			body += "</table>\r\n" + "			</div>";
		} else {
			body += "<div class=\"resultQ\">Kết quả tìm kiếm không có sản phẩm nào!</div>";
		}
		return ResponseEntity.ok(body);
	}

	// danh sách danh mục
	@GetMapping("/categories")
	public String categories(Model model, @RequestParam("page") Optional<Integer> pageNumber) {
		int sizePage = 10;
		Pageable page = PageRequest.of(pageNumber.orElse(1) - 1, sizePage, Sort.by("id").reverse());
		Page<Category> categories = daoCategory.findAll(page);
		long total = categories.getTotalElements();
		int totalPage = (int) (total / sizePage);
		if (total % sizePage != 0) {
			totalPage++;
		}

		model.addAttribute("categories", categories);
		model.addAttribute("total", total);
		model.addAttribute("totalPage", totalPage);
		model.addAttribute("pageNumber", pageNumber.orElse(0));

		String iconRight = " <i style=\"color: darkgray; font-size: 14px;\" class=\"fa-solid fa-chevron-right\"></i> ";
		model.addAttribute("instruction", iconRight + "<a href=\"/admin/categories\">danh mục</a>");
		return "admin/categories/categories";
	}

	// thêm danh mục
	@GetMapping("/categories/addCategory")
	public String addCategory(Model model) {
		String iconRight = " <i style=\"color: darkgray; font-size: 14px;\" class=\"fa-solid fa-chevron-right\"></i> ";
		model.addAttribute("instruction", iconRight + "<a href=\"/admin/categories\">danh mục</a>" + iconRight
				+ "<a href=\"/admin/categories/addCategory\">thêm danh mục</a>");
		return "admin/categories/addCategory";
	}

	@PostMapping("/categories/addCategory")
	public String addCategory(Model model, @RequestParam("name") String name,
			@RequestParam("image") MultipartFile image) throws IllegalStateException, IOException {
		boolean check = true;

		if (name.isEmpty()) {
			model.addAttribute("msName", "Vui lòng nhập tên danh mục");
			check = false;
		}

		if (image.isEmpty()) {
			model.addAttribute("msImage", "Vui lòng chọn ảnh danh mục");
			check = false;
		}

		if (check) {
			String path = System.getProperty("user.dir") + "/src/main/resources/static/images/";
			if (!Files.exists(Path.of(path))) {
				try {
					Files.createDirectory(Path.of(path));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			image.transferTo(new File(path + image.getOriginalFilename()));

			daoCategory.save(new Category(name, image.getOriginalFilename()));
			model.addAttribute("addCategorySC", "");
		} else {
			model.addAttribute("vlName", name);
		}

		String iconRight = " <i style=\"color: darkgray; font-size: 14px;\" class=\"fa-solid fa-chevron-right\"></i> ";
		model.addAttribute("instruction", iconRight + "<a href=\"/admin/categories\">danh mục</a>" + iconRight
				+ "<a href=\"/admin/categories/addCategory\">thêm danh mục</a>");
		return "admin/categories/addCategory";
	}

	// cập nhật danh mục
	@PostMapping("/update-category")
	public ResponseEntity<String> updateCategory(@RequestParam("categoryId") Long categoryId,
			@RequestParam("name") String name, @RequestParam("image") Optional<MultipartFile> image)
			throws IllegalStateException, IOException {
		String body = "";
		Optional<Category> category = daoCategory.findById(categoryId);
		if (category.isPresent()) {
			if (!name.isEmpty()) {
				category.get().setName(name);
			}

			if (image.isPresent()) {
				String path = System.getProperty("user.dir") + "/src/main/resources/static/images/";
				if (!Files.exists(Path.of(path))) {
					try {
						Files.createDirectory(Path.of(path));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				image.get().transferTo(new File(path + image.get().getOriginalFilename()));
				category.get().setImage(image.get().getOriginalFilename());
			}

			daoCategory.saveAndFlush(category.get());
			body = "success";
		}
		return ResponseEntity.ok(body);
	}

	// xóa danh mục
	@GetMapping("/delete-category")
	public ResponseEntity<String> deleteCategory(@RequestParam("categoryId") Long categoryId) {
		String body = "";
		Optional<Category> category = daoCategory.findById(categoryId);
		if (category.isPresent()) {
			try {
				daoCategory.delete(category.get());
				body = "success";
			} catch (Exception e) {
				body = "error";
			}
		}
		return ResponseEntity.ok(body);
	}

	// tìm kiếm danh mục theo id và name
	@GetMapping("/search-category")
	public ResponseEntity<String> searchCategory(@RequestParam("query") String query) {
		String body = "";
		query = query.trim();
		List<Category> categories = new ArrayList<>();
		try {
			Optional<Category> category = daoCategory.findById(Long.parseLong(query));
			if (category.isPresent()) {
				categories.add(category.get());
			}
		} catch (NumberFormatException e) {
			categories = daoCategory.findAllByName("%" + query + "%");
		}

		if (categories.size() != 0) {
			if (query.isEmpty()) {
				if (categories.size() > 10) {
					categories = categories.subList(0, 10);
				}
			}

			body += "<table class=\"table table-bordered mt-2 text-center\">\r\n" + "					<tr>\r\n"
					+ "						<th>Mã số</th>\r\n" + "						<th>Tên danh mục</th>\r\n"
					+ "						<th>Hình ảnh</th>\r\n" + "						<th>Hành động</th>\r\n"
					+ "					</tr>";
			for (Category category : categories) {
				body += "<tr>\r\n" + "						<td class=\"id\">" + category.getId() + "</td>\r\n"
						+ "						<td class=\"name\">" + category.getName() + "</td>\r\n"
						+ "						<td class=\"image\">\r\n"
						+ "							<image src=\"/images/" + category.getImage()
						+ "\" width=\"60\" height=\"60\"></image>\r\n" + "						</td>\r\n"
						+ "						<td class=\"action\">\r\n"
						+ "							<span class=\"edit\" id=\"" + category.getId()
						+ "\" data-bs-toggle=\"modal\" data-bs-target=\"#editCategory\"><i class=\"fa-solid fa-pen\"></i> Sửa</span>\r\n"
						+ "							\r\n"
						+ "							<div class=\"modal fade\" id=\"editCategory\" tabindex=\"-1\" aria-labelledby=\"exampleModalLabel\" aria-hidden=\"true\">\r\n"
						+ "							  <div class=\"modal-dialog\">\r\n"
						+ "							    <div class=\"modal-content\">\r\n"
						+ "							      <div class=\"modal-header\">\r\n"
						+ "							        <h1 class=\"modal-title fs-5\" id=\"exampleModalLabel\">Cập nhật danh mục</h1>\r\n"
						+ "							        <button type=\"button\" class=\"btn-close\" data-bs-dismiss=\"modal\" aria-label=\"Close\"></button>\r\n"
						+ "							      </div>\r\n"
						+ "							      <div class=\"modal-body\">\r\n"
						+ "							        <form action=\"/admin/categories/addCategory\" method=\"post\" enctype=\"multipart/form-data\">\r\n"
						+ "									    <div class=\"form-group\">\r\n"
						+ "											  <label>Tên danh mục</label>\r\n"
						+ "											  <input type=\"text\" name=\"name\" id=\"nameC\" class=\"inp form-control\">\r\n"
						+ "											  <div class=\"message\" id=\"msName\"></div>\r\n"
						+ "									  	</div>\r\n"
						+ "									  	  \r\n"
						+ "									  	<div class=\"form-group\">\r\n"
						+ "											<label>Ảnh danh mục</label>\r\n"
						+ "											<div class=\"ctnImageC\">\r\n"
						+ "												<i class=\"fa-solid fa-x removeImageC\"></i>\r\n"
						+ "												<img class=\"imageC\" width=\"100%\" height=\"100%\">\r\n"
						+ "											</div>\r\n"
						+ "											<input type=\"file\" class=\"fileC\" name=\"image\" style=\"display: none;\">\r\n"
						+ "											<div class=\"message\" id=\"msImage\"></div>\r\n"
						+ "										</div>\r\n"
						+ "									</form>\r\n"
						+ "							      </div>\r\n"
						+ "							      <div class=\"modal-footer\">\r\n"
						+ "							        <button type=\"button\" class=\"btn btn-secondary btnReset\" data-bs-dismiss=\"modal\">Hủy</button>\r\n"
						+ "							        <button type=\"button\" class=\"btn btn-primary btnSave\">Lưu thay đổi</button>\r\n"
						+ "							      </div>\r\n" + "							    </div>\r\n"
						+ "							  </div>\r\n" + "							</div>\r\n"
						+ "							<span class=\"remove\" id=\"" + category.getId()
						+ "\"><i class=\"fa-solid fa-trash-can\"></i> Xóa</span>\r\n"
						+ "					    </td>\r\n" + "					</tr>";
			}
			body += "</table>";
		} else {
			body += "<div class=\"resultQ\">Kết quả tìm kiếm không có danh mục nào!</div>";
		}
		return ResponseEntity.ok(body);
	}

	// danh sách đơn hàng
	@GetMapping("/orders")
	public String orders(Model model, @RequestParam("page") Optional<Integer> pageNumber,
			@RequestParam("filter") Optional<String> filter) {
		int sizePage = 10;
		Pageable page = PageRequest.of(pageNumber.orElse(1) - 1, sizePage, Sort.by("id").reverse());
		List<OrderDetail> orderDetails = daoOrderDetail.findAll();
		model.addAttribute("orderDetails", orderDetails);
		if (!filter.isPresent()) {
			Page<Order> orders = daoOrder.findAll(page);
			long total = orders.getTotalElements();
			int totalPage = (int) (total / sizePage);
			if (total % sizePage != 0) {
				totalPage++;
			}

			model.addAttribute("pageFilter", "");
			model.addAttribute("orders", orders);
			model.addAttribute("total", total);
			model.addAttribute("totalPage", totalPage);
			model.addAttribute("pageNumber", pageNumber.orElse(0));
		} else {
			Page<Order> orders = null;
			String nameFilter = "";
			if (filter.get().equals("wait")) {
				orders = daoOrder.findAllByStatus("Chờ xác nhận", page);
				nameFilter = "Lọc theo chờ xác nhận";
			}
			if (filter.get().equals("confirmed")) {
				orders = daoOrder.findAllByStatus("Đã xác nhận", page);
				nameFilter = "Lọc theo Đã xác nhận";
			}
			if (filter.get().equals("delivering")) {
				orders = daoOrder.findAllByStatus("Đang giao", page);
				nameFilter = "Lọc theo đang giao";
			}
			if (filter.get().equals("delivered")) {
				orders = daoOrder.findAllByStatus("Đã giao", page);
				nameFilter = "Lọc theo đã giao";
			}
			if (filter.get().equals("cancelled")) {
				orders = daoOrder.findAllByStatus("Đã hủy", page);
				nameFilter = "Lọc theo đã hủy";
			}

			long total = orders.getTotalElements();
			int totalPage = (int) (total / sizePage);
			if (total % sizePage != 0) {
				totalPage++;
			}

			model.addAttribute("filter", filter.get());
			model.addAttribute("nameFilter", nameFilter);
			model.addAttribute("pageFilter", "filter=" + filter.get() + "&");
			model.addAttribute("orders", orders);
			model.addAttribute("total", total);
			model.addAttribute("totalPage", totalPage);
			model.addAttribute("pageNumber", pageNumber.orElse(0));
		}

		String iconRight = " <i style=\"color: darkgray; font-size: 14px;\" class=\"fa-solid fa-chevron-right\"></i> ";
		model.addAttribute("instruction", iconRight + "<a href=\"/admin/orders\">đơn hàng</a>");
		return "admin/orders/orders";
	}

	// cập nhật đơn hàng
	@PostMapping("/update-order")
	public ResponseEntity<String> updateOrder(Order order) {
		String body = "";
		Optional<Order> checkOrder = daoOrder.findById(order.getId());
		System.out.println(checkOrder.get().getAccount().getUsername());
		if (checkOrder.isPresent()) {
			order.setCreateDate(checkOrder.get().getCreateDate());
			order.setAccount(checkOrder.get().getAccount());
			daoOrder.saveAndFlush(order);
			body = "success";
		} else {
			body = "notUsername";
		}
		return ResponseEntity.ok(body);
	}

	// duyệt đơn hàng
	@GetMapping("/accept-order")
	public ResponseEntity<String> acceptOrder(@RequestParam("orderId") Long orderId,
			@RequestParam("status") String status) {
		String body = "";
		Optional<Order> order = daoOrder.findById(orderId);
		if (order.isPresent()) {
			if (status.equals("Chờ xác nhận")) {
				order.get().setStatus("Đã xác nhận");
			} else if (status.equals("Đã xác nhận")) {
				order.get().setStatus("Đang giao");
			} else if (status.equals("Đang giao")) {
				order.get().setStatus("Đã giao");
			}
			daoOrder.saveAndFlush(order.get());
			body = "success";
		} else {
			body = "notUsername";
		}
		return ResponseEntity.ok(body);
	}

	// xóa đơn hàng
	@GetMapping("/delete-order")
	public ResponseEntity<String> deleteOrder(@RequestParam("orderId") Long orderId) {
		String body = "";
		Optional<Order> order = daoOrder.findById(orderId);
		if (order.isPresent()) {
			try {
				daoOrder.delete(order.get());
				body = "success";
			} catch (Exception e) {
				body = "error";
			}
		}
		return ResponseEntity.ok(body);
	}

	// tìm kiếm sản phẩm theo id và name
	@GetMapping("/search-order")
	public ResponseEntity<String> searchOrder(@RequestParam("query") String query,
			@RequestParam("dataFilter") String filter) {
		String body = "";
		query = query.trim();
		NumberFormat numb = NumberFormat.getInstance(new Locale("Vi", "VN"));
		SimpleDateFormat simpDate = new SimpleDateFormat("dd-MM-yyyy mm:HH");
		List<Order> orders = new ArrayList<>();

		if (filter != null && !filter.isEmpty()) {
			try {
				Order order = null;
				Long orderId = Long.parseLong(query);
				if (filter.equals("wait")) {
					order = daoOrder.findStatusById(orderId, "Chờ xác nhận");
				} else if (filter.equals("confirmed")) {
					order = daoOrder.findStatusById(orderId, "Đã xác nhận");
				} else if (filter.equals("delivering")) {
					order = daoOrder.findStatusById(orderId, "Đang giao");
				} else if (filter.equals("delivered")) {
					order = daoOrder.findStatusById(orderId, "Đã giao");
				} else if (filter.equals("cancelled")) {
					order = daoOrder.findStatusById(orderId, "Đã hủy");
				}

				if (order != null) {
					orders.add(order);
				}
			} catch (NumberFormatException e) {
				if (filter.equals("wait")) {
					orders = daoOrder.findAllOrdersByStatus("%" + query + "%", "Chờ xác nhận");
				} else if (filter.equals("confirmed")) {
					orders = daoOrder.findAllOrdersByStatus("%" + query + "%", "Đã xác nhận");
				} else if (filter.equals("delivering")) {
					orders = daoOrder.findAllOrdersByStatus("%" + query + "%", "Đang giao");
				} else if (filter.equals("delivered")) {
					orders = daoOrder.findAllOrdersByStatus("%" + query + "%", "Đã giao");
				} else if (filter.equals("cancelled")) {
					orders = daoOrder.findAllOrdersByStatus("%" + query + "%", "Đã hủy");
				}
			}
		} else {
			try {
				Optional<Order> order = daoOrder.findById(Long.parseLong(query));
				if (order.isPresent()) {
					orders.add(order.get());
				}
			} catch (NumberFormatException e) {
				orders = daoOrder.findAllOrders("%" + query + "%");
			}
		}

		if (orders.size() != 0) {
			if (query.isEmpty()) {
				if (orders.size() > 10) {
					orders = orders.subList(0, 10);
				}
			}

			body += "<table class=\"table table-bordered mt-2 text-center\">\r\n" + "					<tr>\r\n"
					+ "						<th>Mã số</th>\r\n" + "						<th>Họ và tên</th>\r\n"
					+ "						<th>Địa chỉ</th>\r\n" + "						<th>Số điện thoại</th>\r\n"
					+ "						<th>Ghi chú</th>\r\n" + "						<th>Ngày tạo</th>\r\n"
					+ "						<th>Trạng thái</th>\r\n" + "						<th>Hành động</th>\r\n"
					+ "					</tr>";
			for (Order order : orders) {
				body += "<tr>\r\n" + "						<td class=\"orderId\">" + order.getId() + "</td>\r\n"
						+ "						<td class=\"fullName\">" + order.getFullName() + "</td>\r\n"
						+ "						<td class=\"address\">" + order.getAddress() + "</td>\r\n"
						+ "						<td class=\"phone\">" + order.getPhone() + "</td>\r\n"
						+ "						<td class=\"note\">" + order.getNote() + "</td>\r\n"
						+ "						<td class=\"createDate\">" + simpDate.format(order.getCreateDate())
						+ "h</td>\r\n" + "						<td class=\"status\">\r\n";
				if (order.getStatus().equals("Chờ xác nhận")) {
					body += "							<span class=\"wait\">Chờ xác nhận</span>\r\n";
				} else if (order.getStatus().equals("Đã xác nhận")) {
					body += "							<span class=\"confirmed\">Đã xác nhận</span>\r\n";
				} else if (order.getStatus().equals("Đang giao")) {

					body += "							<span class=\"delivering\">Đang giao</span>\r\n";
				} else if (order.getStatus().equals("Đã giao")) {
					body += "							<span class=\"delivered\">Đã giao</span>\r\n";
				} else if (order.getStatus().equals("Đã hủy")) {
					body += "							<span class=\"cancelled\">Đã hủy</span>\r\n";
				}
				body += "						</td>\r\n" + "						<td class=\"action\">\r\n"
						+ "							<span class=\"edit\" id=\"" + order.getId()
						+ "\" data-bs-toggle=\"modal\" data-bs-target=\"#editOrder_" + order.getId()
						+ "\"><i class=\"fa-solid fa-pen\"></i> Sửa</span>\r\n"
						+ "							<div class=\"modal fade\" id=\"editOrder_" + order.getId()
						+ "\" tabindex=\"-1\" aria-labelledby=\"exampleModalLabel\" aria-hidden=\"true\">\r\n"
						+ "							  <div class=\"modal-dialog\">\r\n"
						+ "							    <div class=\"modal-content\">\r\n"
						+ "							      <div class=\"modal-header\">\r\n"
						+ "							        <h1 class=\"modal-title fs-5\" id=\"exampleModalLabel\">Cập nhật đơn hàng</h1>\r\n"
						+ "							        <button type=\"button\" class=\"btn-close\" data-bs-dismiss=\"modal\" aria-label=\"Close\"></button>\r\n"
						+ "							      </div>\r\n"
						+ "							      <div class=\"data-save modal-body\">\r\n"
						+ "							        <form>\r\n"
						+ "									    <div class=\"form-group\">\r\n"
						+ "											<label>Họ và tên</label>\r\n"
						+ "											<input type=\"text\" class=\"inpFullName form-control\" value=\""
						+ order.getFullName() + "\">\r\n"
						+ "											<div class=\"message msFullname\"></div>\r\n"
						+ "										</div>\r\n"
						+ "										\r\n"
						+ "									    <div class=\"form-group\">\r\n"
						+ "											<label>Địa chỉ</label>\r\n"
						+ "											<input type=\"text\" class=\"inpAddress form-control\" value=\""
						+ order.getAddress() + "\">\r\n"
						+ "											<div class=\"message msAddress\"></div>\r\n"
						+ "										</div>\r\n"
						+ "										\r\n"
						+ "									    <div class=\"form-group\">\r\n"
						+ "											<label>Số điện thoại</label>\r\n"
						+ "											<input type=\"number\" class=\"inpPhone form-control\" value=\""
						+ order.getPhone() + "\">\r\n"
						+ "											<div class=\"message msPhone\"></div>\r\n"
						+ "										</div>\r\n"
						+ "										\r\n"
						+ "									    <div class=\"form-group\">\r\n"
						+ "											<label>Ghi chú</label>\r\n"
						+ "											<textarea rows=\"3\" type=\"text\" class=\"inpNote form-control\">"
						+ order.getNote() + "</textarea>\r\n" + "										</div>\r\n"
						+ "										\r\n"
						+ "										<div class=\"form-group\">\r\n"
						+ "											<label>Trạng thái</label>\r\n"
						+ "											<select class=\"inpStatus form-select\">\r\n";
				if (order.getStatus().equals("Chờ xác nhận")) {
					body += "												<option selected value=\"Chờ xác nhận\">Chờ xác nhận</option>\r\n";
				} else {
					body += "												<option value=\"Chờ xác nhận\">Chờ xác nhận</option>\r\n";
				}
				if (order.getStatus().equals("Đã xác nhận")) {
					body += "												<option selected value=\"Đã xác nhận\">Đã xác nhận</option>\r\n";
				} else {
					body += "												<option value=\"Đã xác nhận\">Đã xác nhận</option>\r\n";
				}
				if (order.getStatus().equals("Đang giao")) {
					body += "												<option selected value=\"Đang giao\">Đang giao</option>\r\n";
				} else {
					body += "												<option value=\"Đang giao\">Đang giao</option>\r\n";
				}
				if (order.getStatus().equals("Đã giao")) {
					body += "												<option selected value=\"Đã giao\">Đã giao</option>\r\n";
				} else {
					body += "												<option value=\"Đã giao\">Đã giao</option>\r\n";
				}
				if (order.getStatus().equals("Đã giao")) {
					body += "												<option selected value=\"Đã hủy\">Đã hủy</option>\r\n";
				} else {
					body += "												<option value=\"Đã hủy\">Đã hủy</option>\r\n";
				}
				body += "											</select>\r\n"
						+ "											<div class=\"message msStatus\"></div>\r\n"
						+ "										</div>\r\n"
						+ "									</form>\r\n"
						+ "							      </div>\r\n"
						+ "							      <div class=\"modal-footer\">\r\n"
						+ "							        <button type=\"button\" class=\"btn btn-secondary btnReset\" data-bs-dismiss=\"modal\">Hủy</button>\r\n"
						+ "							        <button type=\"button\" id=\"" + order.getId()
						+ "\" class=\"btn btn-primary btnSave\">Lưu thay đổi</button>\r\n"
						+ "							      </div>\r\n" + "							    </div>\r\n"
						+ "							  </div>\r\n" + "							</div>\r\n"
						+ "							\r\n" + "							<span class=\"remove\" id=\""
						+ order.getId() + "\"><i class=\"fa-solid fa-trash-can\"></i> Xóa</span>\r\n";
				if (!order.getStatus().equals("Đã giao") && !order.getStatus().equals("Đã hủy")) {
					body += "							<span class=\"accept\"><i class=\"fa-regular fa-circle-check\"></i> Duyệt</span>\r\n";
				}
				body += "							<span class=\"see\"><i class=\"fa-solid fa-eye\"></i> Xem</span>\r\n"
						+ "							\r\n"
						+ "							<!--Show chi tiết đơn hàng-->\r\n"
						+ "						    <div class=\"show-orderDetail\">\r\n"
						+ "								<i class=\"hidden-orderDetail fa-solid fa-x\"></i>\r\n"
						+ "							</div>\r\n"
						+ "						    <div class=\"show-orderDetails\">\r\n"
						+ "								<div class=\"row\">\r\n"
						+ "									<div class=\"table-responsive\">\r\n"
						+ "										<table class=\"table table-bordered\">\r\n"
						+ "											<tr>\r\n"
						+ "												<th>Mã số</th>\r\n"
						+ "												<th>Mã sản phẩm</th>\r\n"
						+ "												<th>Tên sản phẩm</th>\r\n"
						+ "												<th>Hình ảnh</th>\r\n"
						+ "												<th>Giá</th>\r\n"
						+ "												<th>Khuyến mãi</th>\r\n"
						+ "												<th>Số lượng</th>\r\n"
						+ "												<th>Thành tiền</th>\r\n"
						+ "											</tr>\r\n"
						+ "											\r\n";
				for (OrderDetail orderD : daoOrderDetail.findAll()) {
					if (orderD.getOrder().getId() == order.getId()) {
						body += "											<tr>\r\n"
								+ "												<td>" + orderD.getId() + "</td>\r\n"
								+ "												<td>" + orderD.getProduct().getId()
								+ "</td>\r\n" + "												<td>"
								+ orderD.getProduct().getName() + "</td>\r\n"
								+ "												<td>\r\n"
								+ "													<img src=\"/images/"
								+ orderD.getProduct().getImages().get(0) + "\" width=\"80\" height=\"80\">\r\n"
								+ "												</td>\r\n"
								+ "												<td>\r\n"
								+ "													<div class=\"price\">"
								+ numb.format(orderD.getPrice() * (100 - orderD.getProduct().getDiscount()) / 100)
								+ "đ</div>\r\n";
						if (orderD.getProduct().isPromotion()) {
							body += "													<del class=\"price-del\">"
									+ numb.format(orderD.getProduct().getPrice()) + "đ</del>\r\n";
						}
						body += "												</td>\r\n"
								+ "												<td>\r\n";
						if (orderD.getProduct().isPromotion()) {
							body += "													<span>"
									+ orderD.getProduct().getDiscount() + "%</span>\r\n";
						}
						body += "												</td>\r\n"
								+ "												<td>" + orderD.getQuantity()
								+ "</td>\r\n" + "												<td class=\"totalOD\">"
								+ numb.format(orderD.getQuantity()
										* (orderD.getPrice() * (100 - orderD.getProduct().getDiscount()) / 100))
								+ "đ</td>\r\n" + "											</tr>\r\n";
					}
				}
				body += "										</table>\r\n"
						+ "									</div>\r\n" + "<div class=\"totalPay\"></div>"
						+ "								</div>\r\n" + "							</div>\r\n"
						+ "						</td>\r\n" + "					</tr>";
			}
			body += "</table>";
		} else {
			body += "<div class=\"resultQ\">Kết quả tìm kiếm không có đơn hàng nào!</div>";
		}
		return ResponseEntity.ok(body);
	}

	// danh sách đánh giá
	@GetMapping("/reviews")
	public String reviews(Model model, @RequestParam("page") Optional<Integer> pageNumber,
			@RequestParam("filter") Optional<String> filter) {
		int sizePage = 10;
		Pageable page = PageRequest.of(pageNumber.orElse(1) - 1, sizePage, Sort.by("createDate").reverse());
		List<ReviewFeedback> reviewFBs = daoReviewFB.findAll(Sort.by("createDate"));
		model.addAttribute("reviewFBs", reviewFBs);
		if (!filter.isPresent()) {
			Page<Review> reviews = daoReview.findAll(page);
			long total = reviews.getTotalElements();
			int totalPage = (int) (total / sizePage);
			if (total % sizePage != 0) {
				totalPage++;
			}

			model.addAttribute("pageFilter", "");
			model.addAttribute("reviews", reviews);
			model.addAttribute("total", total);
			model.addAttribute("totalPage", totalPage);
			model.addAttribute("pageNumber", pageNumber.orElse(0));
		} else {
			Page<Review> reviews = null;
			String nameFilter = "";
			if (filter.get().equals("5star")) {
				reviews = daoReview.findAllByStar(5.0, page);
				nameFilter = "Lọc theo đánh giá 5 sao";
			}
			if (filter.get().equals("4star")) {
				reviews = daoReview.findAllByStar(4.0, page);
				nameFilter = "Lọc theo đánh giá 4 sao";
			}
			if (filter.get().equals("3star")) {
				reviews = daoReview.findAllByStar(3.0, page);
				nameFilter = "Lọc theo đánh giá 3 sao";
			}
			if (filter.get().equals("2star")) {
				reviews = daoReview.findAllByStar(2.0, page);
				nameFilter = "Lọc theo đánh giá 2 sao";
			}
			if (filter.get().equals("1star")) {
				reviews = daoReview.findAllByStar(1.0, page);
				nameFilter = "Lọc theo đánh giá 1 sao";
			}

			long total = reviews.getTotalElements();
			int totalPage = (int) (total / sizePage);
			if (total % sizePage != 0) {
				totalPage++;
			}

			model.addAttribute("filter", filter.get());
			model.addAttribute("nameFilter", nameFilter);
			model.addAttribute("pageFilter", "filter=" + filter.get() + "&");
			model.addAttribute("reviews", reviews);
			model.addAttribute("total", total);
			model.addAttribute("totalPage", totalPage);
			model.addAttribute("pageNumber", pageNumber.orElse(0));
		}
		String iconRight = " <i style=\"color: darkgray; font-size: 14px;\" class=\"fa-solid fa-chevron-right\"></i> ";
		model.addAttribute("instruction", iconRight + "<a href=\"/admin/reviews\">đánh giá</a>");
		return "admin/reviews/reviews";
	}

	// xóa đánh giá
	@GetMapping("/delete-review")
	public ResponseEntity<String> deleteReview(@RequestParam("reviewId") Long reviewId) {
		String body = "";
		Optional<Review> review = daoReview.findById(reviewId);
		if (review.isPresent()) {
			try {
				daoReview.delete(review.get());
				body = "success";
			} catch (Exception e) {
				body = "error";
			}
		}
		return ResponseEntity.ok(body);
	}

	// xóa phản hồi đánh giá
	@GetMapping("/delete-reviewFB")
	public ResponseEntity<String> deleteReviewFB(@RequestParam("reviewFBId") Long reviewFBId) {
		String body = "";
		Optional<ReviewFeedback> reviewFB = daoReviewFB.findById(reviewFBId);
		if (reviewFB.isPresent()) {
			try {
				daoReviewFB.delete(reviewFB.get());
				body = "success";
			} catch (Exception e) {
				body = "error";
			}
		}
		return ResponseEntity.ok(body);
	}

	// tìm kiếm đánh giá theo mã số, bình luận, mã sản phẩm
	@GetMapping("/search-review")
	public ResponseEntity<String> searchReview(@RequestParam("query") String query,
			@RequestParam("dataFilter") String filter) {
		String body = "";
		query = query.trim();
		SimpleDateFormat simpDate = new SimpleDateFormat("dd-MM-yyyy mm:HH");
		List<Review> reviews = new ArrayList<>();

		if (filter != null && !filter.isEmpty()) {
			try {
				Long reviewId = Long.parseLong(query);
				if (filter.equals("5star")) {
					reviews = daoReview.findReviewById(reviewId, 5.0);
				} else if (filter.equals("4star")) {
					reviews = daoReview.findReviewById(reviewId, 4.0);
				} else if (filter.equals("3star")) {
					reviews = daoReview.findReviewById(reviewId, 3.0);
				} else if (filter.equals("2star")) {
					reviews = daoReview.findReviewById(reviewId, 2.0);
				} else if (filter.equals("1star")) {
					reviews = daoReview.findReviewById(reviewId, 1.0);
				}
			} catch (NumberFormatException e) {
				if (filter.equals("5star")) {
					reviews = daoReview.findAllByComment("%" + query + "%", 5.0);
				} else if (filter.equals("4star")) {
					reviews = daoReview.findAllByComment("%" + query + "%", 4.0);
				} else if (filter.equals("3star")) {
					reviews = daoReview.findAllByComment("%" + query + "%", 3.0);
				} else if (filter.equals("2star")) {
					reviews = daoReview.findAllByComment("%" + query + "%", 2.0);
				} else if (filter.equals("1star")) {
					reviews = daoReview.findAllByComment("%" + query + "%", 1.0);
				}
			}
		} else {
			try {
				reviews = daoReview.findReviewById(Long.parseLong(query));
			} catch (NumberFormatException e) {
				reviews = daoReview.findAllByComment("%" + query + "%");
			}
		}

		if (reviews.size() != 0) {
			if (query.isEmpty()) {
				if (reviews.size() > 10) {
					reviews = reviews.subList(0, 10);
				}
			}

			body += "<table class=\"table table-bordered mt-2 text-center\">\r\n" + "					<tr>\r\n"
					+ "						<th>Mã số</th>\r\n" + "						<th>Mã sản phẩm</th>\r\n"
					+ "						<th>Người bình luận</th>\r\n"
					+ "						<th>Bình luận</th>\r\n" + "						<th>Hình ảnh</th>\r\n"
					+ "						<th>Điểm đánh giá</th>\r\n"
					+ "						<th>Ngày bình luận</th>\r\n"
					+ "						<th>Hành động</th>\r\n" + "					</tr>";
			for (Review review : reviews) {
				body += "<tr>\r\n" + "						<td class=\"reviewId\">" + review.getId() + "</td>\r\n"
						+ "						<td class=\"productId\">" + review.getProduct().getId() + "</td>\r\n"
						+ "						<td class=\"accountId\">" + review.getAccount().getFullName()
						+ "</td>\r\n" + "						<td class=\"comment\">" + review.getComment()
						+ "</td>\r\n" + "						<td class=\"imageCmt\">\r\n";
				if (review.getImages() != null) {
					body += "							<div class=\"viewDetailImage\">\r\n"
							+ "								<image src=\"/images/" + review.getImages().get(0)
							+ "\" width=\"100%\" height=\"100%\"></image>\r\n"
							+ "								<div class=\"viewImage\">Xem " + review.getImages().size()
							+ " hình</div>\r\n" + "							</div>\r\n";
				}
				body += "						</td>\r\n" + "						<td class=\"reviewPoint\">"
						+ (int) review.getRate() + " sao</td>\r\n" + "						<td class=\"createDate\">"
						+ simpDate.format(review.getCreateDate()) + "h</td>\r\n"
						+ "						<td class=\"action\">\r\n"
						+ "							<span class=\"remove\" id=\"" + review.getId()
						+ "\"><i class=\"fa-solid fa-trash-can\"></i> Xóa</span>\r\n"
						+ "							<span class=\"see\">\r\n"
						+ "								<i class=\"fa-solid fa-eye\"></i> Xem\r\n"
						+ "							</span>\r\n" + "							\r\n"
						+ "							<!--Show chi tiết đánh giá-->\r\n"
						+ "						    <div class=\"show-reviewDetail\">\r\n"
						+ "								<i class=\"hidden-reviewDetail fa-solid fa-x\"></i>\r\n"
						+ "							</div>\r\n"
						+ "						    <div class=\"show-reviewDetails\">\r\n"
						+ "								<div class=\"row\">\r\n"
						+ "									<div class=\"table-responsive\">\r\n"
						+ "										<table class=\"table table-bordered\">\r\n"
						+ "											<tr>\r\n"
						+ "												<th>Mã số</th>\r\n"
						+ "												<th>Người trả lời</th>\r\n"
						+ "												<th>Bình luận</th>\r\n"
						+ "												<th>Hình ảnh</th>\r\n"
						+ "												<th>Trả lời</th>\r\n"
						+ "												<th>Ngày bình luận</th>\r\n"
						+ "												<th>Hành động</th>\r\n"
						+ "											</tr>\r\n"
						+ "											\r\n";
				for (ReviewFeedback reviewFB : daoReviewFB.findAll()) {
					if (reviewFB.getReview().getId() == review.getId()) {
						body += "											<tr>\r\n"
								+ "												<td>" + reviewFB.getId() + "</td>\r\n"
								+ "												<td>"
								+ reviewFB.getAccount().getFullName() + "</td>\r\n"
								+ "												<td class=\"cmtFB\">"
								+ reviewFB.getComment() + "</td>\r\n"
								+ "												<td class=\"rvFBImages\">\r\n";
						if (reviewFB.getImages() != null) {
							for (String image : reviewFB.getImages()) {
								body += "													<img src=\"/images/" + image
										+ "\" width=\"80\" height=\"80\">\r\n";
							}
						}
						body += "												</td>\r\n"
								+ "												<td>" + reviewFB.getRespondent()
								+ "</td>\r\n"
								+ "												<td class=\"createDate\">"
								+ simpDate.format(reviewFB.getCreateDate()) + "h</td>\r\n"
								+ "												<td>\r\n"
								+ "													<span class=\"removeFB\" id=\""
								+ reviewFB.getId() + "\"><i class=\"fa-solid fa-trash-can\"></i> Xóa</span>\r\n"
								+ "												</td>\r\n"
								+ "											</tr>\r\n";
					}
				}
				body += "										</table>\r\n"
						+ "									</div>\r\n" + "								</div>\r\n"
						+ "							</div>\r\n" + "						</td>\r\n"
						+ "						\r\n" + "						<!--Show nhiều hình ảnh-->\r\n"
						+ "					    <div class=\"show-detailImage\">\r\n"
						+ "							<i class=\"hidden-detailImage fa-solid fa-x\"></i>\r\n"
						+ "						</div>\r\n";
				if (review.getImages() != null) {
					body += "					    <div class=\"show-images\">\r\n"
							+ "							<div class=\"row\">\r\n";
					for (String image : review.getImages()) {
						body += "								<div class=\"col-xl-7 col-lg-8 col-md-9 col-sm-10\">\r\n"
								+ "									<img class=\"imagesP\" src=\"/images/" + image
								+ "\" width=\"100%\" height=\"100%\">\r\n" + "								</div>\r\n";
					}
					body += "							</div>\r\n";
				}
				body += "						</div>\r\n" + "					</tr>";
			}
			body += "</table>";
		} else {
			body += "<div class=\"resultQ\">Kết quả tìm kiếm không có đánh giá nào!</div>";
		}
		return ResponseEntity.ok(body);
	}
}
