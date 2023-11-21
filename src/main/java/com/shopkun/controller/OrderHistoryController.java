package com.shopkun.controller;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.shopkun.dao.AccountDao;
import com.shopkun.dao.OrderDao;
import com.shopkun.dao.OrderDetailDao;
import com.shopkun.entity.Account;
import com.shopkun.entity.Order;
import com.shopkun.entity.OrderDetail;
import com.shopkun.service.ShoppingCartService;

import jakarta.servlet.http.HttpSession;

@Controller
public class OrderHistoryController {
	@Autowired
	private OrderDao daoOrder;
	@Autowired
	private OrderDetailDao daoOrderDetail;
	@Autowired
	private ShoppingCartService cart;
	@Autowired
	private AccountDao daoAccount;

	@GetMapping("/order-history")
	public String orderHistory(Model model) {
		Account account = null;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
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

		List<Order> ordersHistory = daoOrder.findAllByStatus(account, "Chờ xác nhận");
		model.addAttribute("ordersHistory", ordersHistory);
		List<OrderDetail> orderDetailsHistory = daoOrderDetail.findAll();
		model.addAttribute("orderDetailsHistory", orderDetailsHistory);

		List<Order> ordersHistoryByQtity = daoOrder.findAllByAccount(account);
		int totalWait = 0, totalConfirmed = 0, totalDelivering = 0;
		for (Order order : ordersHistoryByQtity) {
			if (order.getStatus().equals("Chờ xác nhận")) {
				totalWait++;
			}
			if (order.getStatus().equals("Đã xác nhận")) {
				totalConfirmed++;
			}
			if (order.getStatus().equals("Đang giao")) {
				totalDelivering++;
			}
		}
		model.addAttribute("totalWait", totalWait);
		model.addAttribute("totalConfirmed", totalConfirmed);
		model.addAttribute("totalDelivering", totalDelivering);

		String right = "<i class=\"fa-solid fa-angle-right\"></i>";
		String orderH = "<a href=\"/order-history\">lịch sử mua hàng</a>";
		String instruction = "<a href=\"/\"><i class=\"fa-solid fa-house\"></i></a>" + right + orderH;
		model.addAttribute("instruction", instruction);
		return "order-history/order-history";
	}

	@PostMapping("/cancel-order")
	public ResponseEntity<String> cancelOrder(@RequestParam("orderId") Long orderId) {
		Account account = null;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
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

		Optional<Order> order = daoOrder.findOrderById(orderId, account);

		if (order.isPresent()) {
			if (order.get().getStatus().equals("Chờ xác nhận")) {
				order.get().setStatus("Đã hủy");
				daoOrder.saveAndFlush(order.get());

			}
		}

		List<Order> ordersHistoryByQtity = daoOrder.findAllByAccount(account);
		int totalWait = 0;
		for (Order o : ordersHistoryByQtity) {
			if (o.getStatus().equals("Chờ xác nhận")) {
				totalWait++;
			}
		}
		return ResponseEntity.ok(totalWait + "");
	}

	@GetMapping("/order-status")
	public ResponseEntity<String> orderByStatus(@RequestParam("status") String status) {
		String body = "";
		SimpleDateFormat simpDate = new SimpleDateFormat("dd-MM-yyyy hh:mm");
		NumberFormat numb = NumberFormat.getInstance(new Locale("Vi", "VN"));

		Account account = null;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
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

		List<Order> orderStatus = daoOrder.findAllByStatus(account, status);
		int totalOrder = orderStatus.size();
		int sizePage = 10;
		int start = 0;

		if (orderStatus.size() != 0) {
			if (status.equals("Đã giao") || status.equals("Đã hủy")) { // paging
				if (orderStatus.size() > sizePage) {
					int end = Math.min(start + sizePage, orderStatus.size());
					orderStatus = orderStatus.subList(start, end);
				}
			}

			for (Order order : orderStatus) {
				body += "<div class=\"col-xl-9 col-lg-10 col-md-11 col-sm-12\">\r\n"
						+ "							<div class=\"order\">\r\n"
						+ "								<div class=\"heading\">\r\n"
						+ "									<span class=\"id\">Mã đơn: " + order.getId() + "</span>\r\n"
						+ "									<span class=\"createDate\">"
						+ simpDate.format(order.getCreateDate()) + "</span>\r\n"
						+ "								</div>\r\n";
				for (OrderDetail orderDetail : daoOrderDetail.findByOrder(order)) {
					body += "								<a href=\"product?id=" + orderDetail.getProduct().getId()
							+ "&name=" + orderDetail.getProduct().getName() + "\" class=\"orderDetail\">\r\n"
							+ "									<div class=\"image\">\r\n"
							+ "										<img src=\"/images/"
							+ orderDetail.getProduct().getImages().get(0) + "\" width=\"100\" height=\"100\">\r\n"
							+ "									</div>\r\n"
							+ "									<div class=\"info\">\r\n"
							+ "										<div class=\"name\">"
							+ orderDetail.getProduct().getName() + "</div>\r\n"
							+ "										<span class=\"price\">"
							+ numb.format(orderDetail.getProduct().getPrice()
									* (100 - orderDetail.getProduct().getDiscount()) / 100)
							+ "đ</span>\r\n";
					if (orderDetail.getProduct().isPromotion()) {
						body += "										<del class=\"price-del\">"
								+ numb.format(orderDetail.getProduct().getPrice()) + "đ</del>\r\n";
					}
					body += "										<div class=\"quantity\">Số lượng: "
							+ orderDetail.getQuantity() + "</div>\r\n"
							+ "										<div class=\"total\">"
							+ numb.format(orderDetail.getProduct().getPrice()
									* (100 - orderDetail.getProduct().getDiscount()) / 100 * orderDetail.getQuantity())
							+ "đ</div>\r\n" + "									</div>\r\n"
							+ "								</a>\r\n";
				}
				body += "								<div class=\"row\">\r\n"
						+ "									<div class=\"totalPay col-xl-6 col-lg-6 col-md-6 col-sm-6\">Tổng tiền: <span></span></div>\r\n"
						+ "									<div class=\"action col-xl-6 col-lg-6 col-md-6 col-sm-6\">\r\n";
				if (order.getStatus().equals("Chờ xác nhận")) {
					body += "										<span class=\"cancel\" id=\"" + order.getId()
							+ "\">Hủy đơn hàng</span>\r\n";
				}
				if (order.getStatus().equals("Đã giao") || order.getStatus().equals("Đã hủy")) {
					body += "										<span class=\"repurchase\" id=\"" + order.getId()
							+ "\">Mua lại</span>\r\n";
				}
				body += "										<span class=\"seeInfo\" data-bs-toggle=\"collapse\" data-bs-target=\"#seeInfo-"
						+ order.getId() + "\" aria-expanded=\"false\" aria-controls=\"seeInfo\">\r\n"
						+ "											Thông tin đơn hàng\r\n"
						+ "										</span>\r\n"
						+ "									</div>\r\n"
						+ "									<div class=\"collapse table-responsive\" id=\"seeInfo-"
						+ order.getId() + "\">\r\n" + "									   <table class=\"table\">\r\n"
						+ "										   <tr class=\"head\">\r\n"
						+ "											   <td>Họ và tên</td>\r\n"
						+ "											   <td>Địa chỉ</td>\r\n"
						+ "											   <td>Số điện thoại</td>\r\n"
						+ "											   <td>Ghi chú</td>\r\n"
						+ "										   </tr>\r\n"
						+ "										   <tr>\r\n"
						+ "											   <td>" + order.getFullName() + "</td>\r\n"
						+ "											   <td>" + order.getAddress() + "</td>\r\n"
						+ "											   <td>" + order.getPhone() + "</td>\r\n"
						+ "											   <td>" + order.getNote() + "</td>\r\n"
						+ "										   </tr>\r\n"
						+ "									   </table>\r\n"
						+ "									</div>\r\n" + "								</div>\r\n"
						+ "							</div>\r\n" + "						</div>";
			}

			if ((start + sizePage) < totalOrder) {
				body += "|<span class=\"btnViewMore\" id=\"" + status
						+ "\">Xem thêm đơn hàng <i class=\"fa-solid fa-angle-down\"></i></span>\r\n";
			}
		} else {
			body += "<div class=\"not-order\">Bạn chưa có đơn hàng nào !</div>";
		}

		List<Order> ordersHistoryByQtity = daoOrder.findAllByAccount(account);
		int totalWait = 0, totalConfirmed = 0, totalDelivering = 0;
		for (Order order : ordersHistoryByQtity) {
			if (order.getStatus().equals("Chờ xác nhận")) {
				totalWait++;
			}
			if (order.getStatus().equals("Đã xác nhận")) {
				totalConfirmed++;
			}
			if (order.getStatus().equals("Đang giao")) {
				totalDelivering++;
			}
		}
		body += "|qtityP" + totalWait + ":" + totalConfirmed + ":" + totalDelivering;
		return ResponseEntity.ok(body);
	}

	@GetMapping("/paging-order")
	public ResponseEntity<String> paging(@RequestParam("pageNumberDelivered") Optional<Integer> pageNumberDelivered,
			@RequestParam("pageNumberCancelled") Optional<Integer> pageNumberCancelled,
			@RequestParam("status") String status) {
		String body = "";
		SimpleDateFormat simpDate = new SimpleDateFormat("dd-MM-yyyy hh:mm");
		NumberFormat numb = NumberFormat.getInstance(new Locale("Vi", "VN"));

		Account account = null;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
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

		List<Order> orderStatus = daoOrder.findAllByStatus(account, status);
		int totalOrder = orderStatus.size();
		int sizePage = 10;

		if (status.equals("Đã giao")) {
			int start = (pageNumberDelivered.orElse(1) - 1) * sizePage;
			int end = Math.min(start + sizePage, orderStatus.size());
			orderStatus = orderStatus.subList(start, end);

			for (Order order : orderStatus) {
				body += "<div class=\"col-xl-9 col-lg-10 col-md-11 col-sm-12\">\r\n"
						+ "							<div class=\"order\">\r\n"
						+ "								<div class=\"heading\">\r\n"
						+ "									<span class=\"id\">Mã đơn: " + order.getId() + "</span>\r\n"
						+ "									<span class=\"createDate\">"
						+ simpDate.format(order.getCreateDate()) + "</span>\r\n"
						+ "								</div>\r\n";
				for (OrderDetail orderDetail : daoOrderDetail.findByOrder(order)) {
					body += "								<a href=\"product?id=" + orderDetail.getProduct().getId()
							+ "&name=" + orderDetail.getProduct().getName() + "\" class=\"orderDetail\">\r\n"
							+ "									<div class=\"image\">\r\n"
							+ "										<img src=\"/images/"
							+ orderDetail.getProduct().getImages().get(0) + "\" width=\"100\" height=\"100\">\r\n"
							+ "									</div>\r\n"
							+ "									<div class=\"info\">\r\n"
							+ "										<div class=\"name\">"
							+ orderDetail.getProduct().getName() + "</div>\r\n"
							+ "										<span class=\"price\">"
							+ numb.format(orderDetail.getProduct().getPrice()
									* (100 - orderDetail.getProduct().getDiscount()) / 100)
							+ "đ</span>\r\n";
					if (orderDetail.getProduct().isPromotion()) {
						body += "										<del class=\"price-del\">"
								+ numb.format(orderDetail.getProduct().getPrice()) + "đ</del>\r\n";
					}
					body += "										<div class=\"quantity\">Số lượng: "
							+ orderDetail.getQuantity() + "</div>\r\n"
							+ "										<div class=\"total\">"
							+ numb.format(orderDetail.getProduct().getPrice()
									* (100 - orderDetail.getProduct().getDiscount()) / 100 * orderDetail.getQuantity())
							+ "đ</div>\r\n" + "									</div>\r\n"
							+ "								</a>\r\n";
				}
				body += "								<div class=\"row\">\r\n"
						+ "									<div class=\"totalPay col-xl-6 col-lg-6 col-md-6 col-sm-6\">Tổng tiền: <span></span></div>\r\n"
						+ "									<div class=\"action col-xl-6 col-lg-6 col-md-6 col-sm-6\">\r\n";
				if (status.equals("Chờ xác nhận")) {
					body += "										<span class=\"cancel\" id=\"" + order.getId()
							+ "\">Hủy đơn hàng</span>\r\n";
				}
				if (status.equals("Đã giao") || status.equals("Đã hủy")) {
					body += "										<span class=\"repurchase\" id=\"" + order.getId()
							+ "\">Mua lại</span>\r\n";
				}
				body += "										<span class=\"seeInfo\" data-bs-toggle=\"collapse\" data-bs-target=\"#seeInfo-"
						+ order.getId() + "\" aria-expanded=\"false\" aria-controls=\"seeInfo\">\r\n"
						+ "											Thông tin đơn hàng\r\n"
						+ "										</span>\r\n"
						+ "									</div>\r\n"
						+ "									<div class=\"collapse table-responsive\" id=\"seeInfo-"
						+ order.getId() + "\">\r\n" + "									   <table class=\"table\">\r\n"
						+ "										   <tr class=\"head\">\r\n"
						+ "											   <td>Họ và tên</td>\r\n"
						+ "											   <td>Địa chỉ</td>\r\n"
						+ "											   <td>Số điện thoại</td>\r\n"
						+ "											   <td>Ghi chú</td>\r\n"
						+ "										   </tr>\r\n"
						+ "										   <tr>\r\n"
						+ "											   <td>" + order.getFullName() + "</td>\r\n"
						+ "											   <td>" + order.getAddress() + "</td>\r\n"
						+ "											   <td>" + order.getPhone() + "</td>\r\n"
						+ "											   <td>" + order.getNote() + "</td>\r\n"
						+ "										   </tr>\r\n"
						+ "									   </table>\r\n"
						+ "									</div>\r\n" + "								</div>\r\n"
						+ "							</div>\r\n" + "						</div>";
			}

			if ((start + sizePage) < totalOrder) {
				body += "|<span class=\"btnViewMore\" id=\"" + status
						+ "\">Xem thêm đơn hàng <i class=\"fa-solid fa-angle-down\"></i></span>\r\n";
			}
		} else if (status.equals("Đã hủy")) {
			int start = (pageNumberCancelled.orElse(1) - 1) * sizePage;
			int end = Math.min(start + sizePage, orderStatus.size());
			orderStatus = orderStatus.subList(start, end);

			for (Order order : orderStatus) {
				body += "<div class=\"col-xl-9 col-lg-10 col-md-11 col-sm-12\">\r\n"
						+ "							<div class=\"order\">\r\n"
						+ "								<div class=\"heading\">\r\n"
						+ "									<span class=\"id\">Mã đơn: " + order.getId() + "</span>\r\n"
						+ "									<span class=\"createDate\">"
						+ simpDate.format(order.getCreateDate()) + "</span>\r\n"
						+ "								</div>\r\n";
				for (OrderDetail orderDetail : daoOrderDetail.findByOrder(order)) {
					body += "								<a href=\"product?id=" + orderDetail.getProduct().getId()
							+ "&name=" + orderDetail.getProduct().getName() + "\" class=\"orderDetail\">\r\n"
							+ "									<div class=\"image\">\r\n"
							+ "										<img src=\"/images/"
							+ orderDetail.getProduct().getImages().get(0) + "\" width=\"100\" height=\"100\">\r\n"
							+ "									</div>\r\n"
							+ "									<div class=\"info\">\r\n"
							+ "										<div class=\"name\">"
							+ orderDetail.getProduct().getName() + "</div>\r\n"
							+ "										<span class=\"price\">"
							+ numb.format(orderDetail.getProduct().getPrice()
									* (100 - orderDetail.getProduct().getDiscount()) / 100)
							+ "đ</span>\r\n";
					if (orderDetail.getProduct().isPromotion()) {
						body += "										<del class=\"price-del\">"
								+ numb.format(orderDetail.getProduct().getPrice()) + "đ</del>\r\n";
					}
					body += "										<div class=\"quantity\">Số lượng: "
							+ orderDetail.getQuantity() + "</div>\r\n"
							+ "										<div class=\"total\">"
							+ numb.format(orderDetail.getProduct().getPrice()
									* (100 - orderDetail.getProduct().getDiscount()) / 100 * orderDetail.getQuantity())
							+ "đ</div>\r\n" + "									</div>\r\n"
							+ "								</a>\r\n";
				}
				body += "								<div class=\"row\">\r\n"
						+ "									<div class=\"totalPay col-xl-6 col-lg-6 col-md-6 col-sm-6\">Tổng tiền: <span></span></div>\r\n"
						+ "									<div class=\"action col-xl-6 col-lg-6 col-md-6 col-sm-6\">\r\n";
				if (order.getStatus().equals("Chờ xác nhận")) {
					body += "										<span class=\"cancel\" id=\"" + order.getId()
							+ "\">Hủy đơn hàng</span>\r\n";
				}
				if (order.getStatus().equals("Đã giao") || order.getStatus().equals("Đã hủy")) {
					body += "										<span class=\"repurchase\" id=\"" + order.getId()
							+ "\">Mua lại</span>\r\n";
				}
				body += "										<span class=\"seeInfo\" data-bs-toggle=\"collapse\" data-bs-target=\"#seeInfo-"
						+ order.getId() + "\" aria-expanded=\"false\" aria-controls=\"seeInfo\">\r\n"
						+ "											Thông tin đơn hàng\r\n"
						+ "										</span>\r\n"
						+ "									</div>\r\n"
						+ "									<div class=\"collapse table-responsive\" id=\"seeInfo-"
						+ order.getId() + "\">\r\n" + "									   <table class=\"table\">\r\n"
						+ "										   <tr class=\"head\">\r\n"
						+ "											   <td>Họ và tên</td>\r\n"
						+ "											   <td>Địa chỉ</td>\r\n"
						+ "											   <td>Số điện thoại</td>\r\n"
						+ "											   <td>Ghi chú</td>\r\n"
						+ "										   </tr>\r\n"
						+ "										   <tr>\r\n"
						+ "											   <td>" + order.getFullName() + "</td>\r\n"
						+ "											   <td>" + order.getAddress() + "</td>\r\n"
						+ "											   <td>" + order.getPhone() + "</td>\r\n"
						+ "											   <td>" + order.getNote() + "</td>\r\n"
						+ "										   </tr>\r\n"
						+ "									   </table>\r\n"
						+ "									</div>\r\n" + "								</div>\r\n"
						+ "							</div>\r\n" + "						</div>";
			}

			if ((start + sizePage) < totalOrder) {
				body += "|<span class=\"btnViewMore\" id=\"" + status
						+ "\">Xem thêm đơn hàng <i class=\"fa-solid fa-angle-down\"></i></span>\r\n";
			}
		}

		return ResponseEntity.ok(body);
	}

	@GetMapping("/search-order")
	public ResponseEntity<String> searchOrder(@RequestParam("query") String query,
			@RequestParam("status") String status) {
		String body = "";
		SimpleDateFormat simpDate = new SimpleDateFormat("dd-MM-yyyy hh:mm");
		NumberFormat numb = NumberFormat.getInstance(new Locale("Vi", "VN"));

		Account account = null;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
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

		List<Order> orderStatus = null;
		if (query.isEmpty()) {
			orderStatus = daoOrder.findAllByStatus(account, status);
		} else {
			try {
				int id = Integer.parseInt(query);
				orderStatus = daoOrderDetail.findAllById(account, id);
			} catch (NumberFormatException e) {
				orderStatus = daoOrderDetail.findAllByName(account, "%" + query + "%");
			}
		}

		if (orderStatus.size() != 0) {
			int totalOrder = orderStatus.size();
			int sizePage = 10;
			int start = 0;
			if (status.equals("Đã giao") || status.equals("Đã hủy")) { // paging
				if (totalOrder > sizePage) {
					int end = Math.min(start + sizePage, orderStatus.size());
					orderStatus = orderStatus.subList(start, end);
				}
			}

			for (Order order : orderStatus) {
				body += "<div class=\"col-xl-9 col-lg-10 col-md-11 col-sm-12\">\r\n"
						+ "							<div class=\"order\">\r\n"
						+ "								<div class=\"heading\">\r\n"
						+ "									<span class=\"id\">Mã đơn: " + order.getId() + "</span>\r\n"
						+ "									<span class=\"createDate\">"
						+ simpDate.format(order.getCreateDate()) + "</span>\r\n"
						+ "								</div>\r\n";
				for (OrderDetail orderDetail : daoOrderDetail.findByOrder(order)) {
					body += "								<a href=\"product?id=" + orderDetail.getProduct().getId()
							+ "&name=" + orderDetail.getProduct().getName() + "\" class=\"orderDetail\">\r\n"
							+ "									<div class=\"image\">\r\n"
							+ "										<img src=\"/images/"
							+ orderDetail.getProduct().getImages().get(0) + "\" width=\"100\" height=\"100\">\r\n"
							+ "									</div>\r\n"
							+ "									<div class=\"info\">\r\n"
							+ "										<div class=\"name\">"
							+ orderDetail.getProduct().getName() + "</div>\r\n"
							+ "										<span class=\"price\">"
							+ numb.format(orderDetail.getProduct().getPrice()
									* (100 - orderDetail.getProduct().getDiscount()) / 100)
							+ "đ</span>\r\n";
					if (orderDetail.getProduct().isPromotion()) {
						body += "										<del class=\"price-del\">"
								+ numb.format(orderDetail.getProduct().getPrice()) + "đ</del>\r\n";
					}
					body += "										<div class=\"quantity\">Số lượng: "
							+ orderDetail.getQuantity() + "</div>\r\n"
							+ "										<div class=\"total\">"
							+ numb.format(orderDetail.getProduct().getPrice()
									* (100 - orderDetail.getProduct().getDiscount()) / 100 * orderDetail.getQuantity())
							+ "đ</div>\r\n" + "									</div>\r\n"
							+ "								</a>\r\n";
				}
				body += "								<div class=\"row\">\r\n"
						+ "									<div class=\"totalPay col-xl-6 col-lg-6 col-md-6 col-sm-6\">Tổng tiền: <span></span></div>\r\n"
						+ "									<div class=\"action col-xl-6 col-lg-6 col-md-6 col-sm-6\">\r\n";
				if (order.getStatus().equals("Chờ xác nhận")) {
					body += "										<span class=\"cancel\" id=\"" + order.getId()
							+ "\">Hủy đơn hàng</span>\r\n";
				}
				if (order.getStatus().equals("Đã giao") || order.getStatus().equals("Đã hủy")) {
					body += "										<span class=\"repurchase\" id=\"" + order.getId()
							+ "\">Mua lại</span>\r\n";
				}
				body += "										<span class=\"seeInfo\" data-bs-toggle=\"collapse\" data-bs-target=\"#seeInfo-"
						+ order.getId() + "\" aria-expanded=\"false\" aria-controls=\"seeInfo\">\r\n"
						+ "											Thông tin đơn hàng\r\n"
						+ "										</span>\r\n"
						+ "									</div>\r\n"
						+ "									<div class=\"collapse table-responsive\" id=\"seeInfo-"
						+ order.getId() + "\">\r\n" + "									   <table class=\"table\">\r\n"
						+ "										   <tr class=\"head\">\r\n"
						+ "											   <td>Họ và tên</td>\r\n"
						+ "											   <td>Địa chỉ</td>\r\n"
						+ "											   <td>Số điện thoại</td>\r\n"
						+ "											   <td>Ghi chú</td>\r\n"
						+ "										   </tr>\r\n"
						+ "										   <tr>\r\n"
						+ "											   <td>" + order.getFullName() + "</td>\r\n"
						+ "											   <td>" + order.getAddress() + "</td>\r\n"
						+ "											   <td>" + order.getPhone() + "</td>\r\n"
						+ "											   <td>" + order.getNote() + "</td>\r\n"
						+ "										   </tr>\r\n"
						+ "									   </table>\r\n"
						+ "									</div>\r\n" + "								</div>\r\n"
						+ "							</div>\r\n" + "						</div>";
			}

			if ((start + sizePage) < totalOrder) {
				body += "|<span class=\"btnViewMore\" id=\"" + status
						+ "\">Xem thêm đơn hàng <i class=\"fa-solid fa-angle-down\"></i></span>\r\n";
			}
		} else {
			// body += "<div class=\"not-order\">Kết quả tìm kiếm không có đơn hàng
			// nào!</div>";
		}
		return ResponseEntity.ok(body);
	}

	@GetMapping("/repurchase-order")
	public ResponseEntity<String> repurchaseOrder(@RequestParam("orderId") Long orderId, HttpSession session) {
		String body = "";
		Order order = daoOrder.findById(orderId).get();
		for (OrderDetail orderDetail : daoOrderDetail.findByOrder(order)) {
			cart.update(orderDetail.getProduct(), orderDetail.getQuantity());
		}
		session.setAttribute("totalCart", cart.getTotal());
		return ResponseEntity.ok(body);
	}
}
