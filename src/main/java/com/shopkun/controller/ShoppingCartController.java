package com.shopkun.controller;

import java.util.Date;
import java.util.List;

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
import com.shopkun.dao.ProductDao;
import com.shopkun.entity.Account;
import com.shopkun.entity.Order;
import com.shopkun.entity.OrderDetail;
import com.shopkun.entity.Product;
import com.shopkun.service.ShoppingCartService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ShoppingCartController {
	@Autowired
	private ShoppingCartService cart;
	@Autowired
	private AccountDao daoAccount;
	@Autowired
	private ProductDao daoProduct;
	@Autowired
	private OrderDao daoOrder;
	@Autowired
	private OrderDetailDao daoOrderDetail;
	@Autowired
	private HttpSession session;

	@GetMapping("/shopping-cart")
	public String shoppingCart(Model model) {
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

			model.addAttribute("vlFullName", account.getFullName());
			model.addAttribute("vlAddress", account.getAddress());
			model.addAttribute("vlPhone", account.getPhone());
		}

		List<Product> cartProducts = cart.findAll();
		model.addAttribute("cartProducts", cartProducts);

		String right = "<i class=\"fa-solid fa-angle-right\"></i>";
		String cart = "<a href=\"/shopping-cart\">giỏ hàng</a>";
		String instruction = "<a href=\"/\"><i class=\"fa-solid fa-house\"></i></a>" + right + cart;
		model.addAttribute("instruction", instruction);
		return "shopping-cart/shopping-cart";
	}

	@PostMapping("/shopping-cart")
	public String shoppingCart(Model model, Order order) {
		String messageCart = "";
		if (cart.getTotal() != 0) {
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

			boolean check = true;

			if (order.getFullName().isEmpty()) {
				model.addAttribute("msFullName", "Vui lòng nhập họ và tên");
				check = false;
			}
			if (order.getAddress().isEmpty()) {
				model.addAttribute("msAddress", "Vui lòng nhập địa chỉ");
				check = false;
			}

			if (order.getPhone().isEmpty()) {
				model.addAttribute("msPhone", "Vui lòng nhập số điện thoại");
				check = false;
			}

			// kiểm tra nếu số lượng mua lớn hơn số lượng kho
			for (Product cart : cart.findAll()) {
				Product product = daoProduct.findById(cart.getId()).get();
				if (cart.getQuantity() > product.getQuantity()) {
					messageCart += cart.getId() + "|Hiện số lượng sản phẩm còn " + product.getQuantity()
							+ ". Vui lòng thay đổi số lượng hoặc xóa để tiếp tục" + ",";
					check = false;
				}
			}

			if (check) {
				// Cập nhật lại thông tin người dùng
				account.setFullName(order.getFullName());
				account.setAddress(order.getAddress());
				account.setPhone(order.getPhone());
				daoAccount.saveAndFlush(account);

				// lưu đơn hàng và chi tiết đơn hàng
				order.setCreateDate(new Date());
				order.setStatus("Chờ xác nhận");
				order.setAccount(account);

				daoOrder.save(order);
				for (Product cart : cart.findAll()) {
					Product product = daoProduct.findById(cart.getId()).get();
					product.setQuantity(product.getQuantity() - cart.getQuantity());
					daoProduct.saveAndFlush(product);
					daoOrderDetail.save(new OrderDetail(cart.getPrice(), cart.getQuantity(), cart, order));
				}

				cart.clear();
				session.removeAttribute("totalCart");
				model.addAttribute("orderSC", "");
			} else {
				model.addAttribute("vlNote", order.getNote());
				String messageCarts[] = messageCart.split(",");
				if (messageCarts.length > 0) {
					model.addAttribute("messageCarts", messageCarts);
				}
			}
			model.addAttribute("vlFullName", order.getFullName());
			model.addAttribute("vlAddress", order.getAddress());
			model.addAttribute("vlPhone", order.getPhone());

			List<Product> cartProducts = cart.findAll();
			model.addAttribute("cartProducts", cartProducts);
		} else {
			model.addAttribute("vlFullName", order.getFullName());
			model.addAttribute("vlAddress", order.getAddress());
			model.addAttribute("vlPhone", order.getPhone());
		}

		String right = "<i class=\"fa-solid fa-angle-right\"></i>";
		String cart = "<a href=\"/shopping-cart\">giỏ hàng</a>";
		String instruction = "<a href=\"/\"><i class=\"fa-solid fa-house\"></i></a>" + right + cart;
		model.addAttribute("instruction", instruction);
		return "shopping-cart/shopping-cart";
	}

	@GetMapping("/plus-shopping-cart")
	public ResponseEntity<String> plusShoppingCart(@RequestParam("productId") Long productId,
			@RequestParam("quantity") int quantity) {
		String body = "";
		Product product = daoProduct.findById(productId).get();
		if (quantity <= product.getQuantity()) {
			cart.updateInShoppingCart(product, quantity);
		} else {
			body = "overload|" + product.getQuantity();
		}
		return ResponseEntity.ok(body);
	}

	@GetMapping("/minus-shopping-cart")
	public ResponseEntity<String> minusShoppingCart(@RequestParam("productId") Long productId,
			@RequestParam("quantity") int quantity) {
		String body = "";
		Product product = daoProduct.findById(productId).get();
		if (quantity <= product.getQuantity()) {
			cart.updateInShoppingCart(product, quantity);
		} else {
			body = "overload";
		}
		return ResponseEntity.ok(body);
	}

	@GetMapping("/add-cart")
	public ResponseEntity<String> addCart(@RequestParam("productId") Long productId) {
		String body = "";
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

		if (account != null) {
			Product product = daoProduct.findById(productId).get();
			cart.add(product);
			session.setAttribute("totalCart", cart.getTotal());
			if (cart.getTotal() > 99) {
				body = "99+";
			} else {
				body = cart.getTotal() + "";
			}
			return ResponseEntity.ok(body);
		} else {
			return ResponseEntity.ok("not login");
		}
	}

	@GetMapping("/add-cartP")
	public ResponseEntity<String> addCartP(@RequestParam("productId") Long productId,
			@RequestParam("quantity") int quantity) {
		String body = "";
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

		if (account != null) {
			Product product = daoProduct.findById(productId).get();
			int lengthP = product.getQuantity();
			cart.update(product, quantity);
			session.setAttribute("totalCart", cart.getTotal());
			if (cart.getTotal() > 99) {
				body = "99+";
			} else {
				body = cart.getTotal() + "";
			}

			Product cartP = cart.findById(productId);
			if (cartP != null) {
				if (cartP.getQuantity() <= lengthP) {
					return ResponseEntity.ok(body);
				} else {
					return ResponseEntity.ok("overload");
				}
			} else {
				return ResponseEntity.ok("overload");
			}
		} else {
			return ResponseEntity.ok("not login");
		}
	}

	@GetMapping("/plus-cart")
	public ResponseEntity<String> plusCart(@RequestParam("productId") Long productId,
			@RequestParam("quantity") int quantity) {
		String body = "";
		Product product = daoProduct.findById(productId).get();
		if (quantity > product.getQuantity()) {
			body = "overload";
		}
		return ResponseEntity.ok(body);
	}

	@PostMapping("/delete-cartP")
	public ResponseEntity<String> deleteCartP(@RequestParam("productId") Long productId) {
		String body = "";
		cart.remove(productId);
		if (cart.getTotal() == 0) {
			session.removeAttribute("totalCart");
		} else {
			session.setAttribute("totalCart", cart.getTotal());
		}
		body = cart.getTotal() + "";
		return ResponseEntity.ok(body);
	}
}
