package com.shopkun.controller;

import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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

import com.shopkun.dao.AccountDao;
import com.shopkun.entity.Account;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/account")
public class AccountController {
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private BCryptPasswordEncoder encoder;
	@Autowired
	private JavaMailSender sender;
	@Autowired
	private HttpSession session;
	@Autowired
	private AccountDao daoAccount;

	// Login
	@GetMapping("/login")
	public String login() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
			return "redirect:/";
		} else {
			return "account/login";
		}
	}

	// Register
	@GetMapping("/register")
	public String register() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
			return "redirect:/";
		} else {
			return "account/register";
		}
	}

	@PostMapping("/register")
	public String register(Account account, Model model) {
		boolean check = true;

		if (account.getUsername().isEmpty()) {
			model.addAttribute("msUsername", "Vui lòng nhập tên đăng nhập");
			check = false;
		} else {
			Optional<Account> checkAccount = daoAccount.findById(account.getUsername());
			if(checkAccount.isPresent()) {
				model.addAttribute("msUsername", "Tên đăng nhập đã tồn tại");
				check = false;
			}
		}

		if (account.getPassword().isEmpty()) {
			model.addAttribute("msPassword", "Vui lòng nhập mật khẩu");
			check = false;
		}

		if (account.getFullName().isEmpty()) {
			model.addAttribute("msFullName", "Vui lòng nhập họ và tên");
			check = false;
		}
		if (account.getAddress().isEmpty()) {
			model.addAttribute("msAddress", "Vui lòng nhập địa chỉ");
			check = false;
		}

		if (account.getEmail().isEmpty()) {
			model.addAttribute("msEmail", "Vui lòng nhập địa chỉ email");
			check = false;
		} else {
			Pattern p = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
			if (!p.matcher(account.getEmail()).find()) {
				model.addAttribute("msEmail", "Vui lòng nhập đúng định dạng email");
				check = false;
			}
		}

		if (account.getPhone().isEmpty()) {
			model.addAttribute("msPhone", "Vui lòng nhập số điện thoại");
			check = false;
		}

		if (check) {
			account.setPassword(encoder.encode(account.getPassword()));
			account.setRole("ROLE_USER");
			daoAccount.save(account);
			model.addAttribute("registerSC", "");
		} else {
			model.addAttribute("vlUsername", account.getUsername());
			model.addAttribute("vlPassword", account.getPassword());
			model.addAttribute("vlFullName", account.getFullName());
			model.addAttribute("vlAddress", account.getAddress());
			model.addAttribute("vlEmail", account.getEmail());
			model.addAttribute("vlPhone", account.getPhone());
		}
		return "account/register";
	}

	// Forgot-password
	@GetMapping("/forgot-password")
	public String forgot_password() {
		Object strTimeOut = session.getAttribute("timeOutFG");
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
			return "redirect:/";
		} else {
			if (strTimeOut != null) {
				if (Long.parseLong(strTimeOut + "") <= System.currentTimeMillis()) {
					session.removeAttribute("timeOutFG");
				}
				return "redirect:/account/forgot-password/change-password";
			} else {
				return "account/forgot-password";
			}
		}
	}

	@PostMapping("/forgot-password")
	public String forgot_password(Model model, @RequestParam("username") String username) throws MessagingException {
		boolean check = true;
		if (username.isEmpty()) {
			model.addAttribute("msUsername", "Vui lòng nhập tên đăng nhập");
			check = false;
		} else {
			Optional<Account> account = daoAccount.findById(username);
			if (account.isPresent()) {
				// Set timeout theo từng phiên
				long currentTime = 1 * 60 * 1000; // 1'
				session.setAttribute("timeOutFG", System.currentTimeMillis() + currentTime);

				int index = account.get().getEmail().indexOf("@");
				String emailXN = "";
				int length = account.get().getEmail().length();
				if (length > 6) {
					for (int i = 0; i < length; i++) {
						if (i >= 3 && i < index - 2) {
							emailXN += "*";
						} else {
							emailXN += account.get().getEmail().charAt(i);
						}
					}
				}
				String maXN = sendEmail(account.get());
				session.setAttribute("maXN", maXN);
				session.setAttribute("username", account.get().getUsername());
				session.setAttribute("emailXN", emailXN);
				session.setMaxInactiveInterval(30 * 60);
			} else {
				model.addAttribute("vlUsername", username);
				model.addAttribute("msUsername", "Tên đăng nhập không tồn tại");
				check = false;
			}
		}

		if (check) {
			return "redirect:/account/forgot-password/change-password";
		} else {
			return "account/forgot-password";
		}
	}

	public String sendEmail(Account account) throws MessagingException {
		String maXN = "";
		MimeMessage message = sender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
		helper.setFrom("testtest86n@gmail.com");
		helper.setTo(account.getEmail());
		helper.setSubject("Thông tin mã xác thực đặt lại mật khẩu");
		Random rd = new Random();
		for (int i = 0; i < 6; i++) {
			maXN += String.valueOf(rd.nextInt(9) + 0);
		}
		String noidung = "Chào " + account.getFullName() + ".<br>"
				+ "Chúng tôi nhận được yêu cầu lấy lại mật khẩu của bạn qua email. Để hoàn tất quá trình lấy lại mật khẩu, vui lòng sử dụng mã xác nhận sau đây để đặt lại mật khẩu của bạn:<br>"
				+ "Mã xác nhận: <span style=\"color: skyblue; font-size: 16px; font-weight: bold;\">" + maXN
				+ "</span><br>"
				+ "Vui lòng nhập mã xác nhận này vào trang đặt lại mật khẩu của chúng tôi để tiếp tục quá trình đặt lại mật khẩu của bạn.<br>"
				+ "Đây là email gửi tự động, vui lòng không phản hồi.<br>"
				+ "Nếu bạn không gửi yêu cầu này, vui lòng bỏ qua email này.<br>" + "Xin chân thành cảm ơn !";
		helper.setText(noidung, true);
		helper.setReplyTo("testtest86n@gmail.com");
		sender.send(message);
		return maXN;
	}

	@GetMapping("/forgot-password/change-password")
	public String fg_changePassword() throws MessagingException {
		String resend = request.getParameter("resend");
		if (resend != null) {
			if (resend.isEmpty()) {
				// Set timeout theo từng phiên
				long currentTime = 1 * 60 * 1000; // 1'
				session.setAttribute("timeOutFG", System.currentTimeMillis() + currentTime);

				String maXN = sendEmail(daoAccount.findById(session.getAttribute("username") + "").get());
				session.setAttribute("maXN", maXN);
				session.setMaxInactiveInterval(30 * 60);
			}
			return "redirect:/account/forgot-password/change-password";
		} else {
			Object strTimeOut = session.getAttribute("timeOutFG");
			if (strTimeOut != null) {
				if (Long.parseLong(strTimeOut + "") <= System.currentTimeMillis()) {
					session.removeAttribute("timeOutFG");
					session.removeAttribute("maXN");
				}
				return "account/fg-changePassword";
			} else {
				return "redirect:/account/forgot-password";
			}
		}
	}

	@PostMapping("/forgot-password/change-password")
	public String fg_changePassword(Model model, @RequestParam("verification") String maXN,
			@RequestParam("newPassword") String newPassword, @RequestParam("cfNewPassword") String cfNewPassword) {
		boolean check = true;
		Object strTimeOut = session.getAttribute("timeOutFG");
		if (strTimeOut != null) {
			if (Long.parseLong(strTimeOut + "") <= System.currentTimeMillis()) {
				session.removeAttribute("timeOutFG");
				session.removeAttribute("maXN");
			}
		}

		String maXNEmail = (String) session.getAttribute("maXN");
		if (maXN.isEmpty()) {
			model.addAttribute("msVerification", "Vui lòng nhập mã xác nhận");
			check = false;
		} else {
			if (!maXN.equals(maXNEmail)) {
				model.addAttribute("msVerification", "Mã xác nhận không đúng");
				check = false;
			}
		}

		if (newPassword.isEmpty()) {
			model.addAttribute("msNewPassword", "Vui lòng nhập mật khẩu mới");
			check = false;
		} else {
			if (!newPassword.equals(cfNewPassword)) {
				model.addAttribute("msNewPassword", "Mật khẩu chưa khớp");
				check = false;
			}
		}

		if (cfNewPassword.isEmpty()) {
			model.addAttribute("msCfNewPassword", "Vui lòng nhập mật khẩu mới");
			check = false;
		} else {
			if (!newPassword.equals(cfNewPassword)) {
				model.addAttribute("msCfNewPassword", "Mật khẩu chưa khớp");
				check = false;
			}
		}

		if (check) {
			Account account = daoAccount.findById(session.getAttribute("username") + "").get();
			account.setPassword(encoder.encode(newPassword));
			daoAccount.saveAndFlush(account);
			model.addAttribute("fgSC", "");

			session.removeAttribute("maXN");
			session.removeAttribute("emailXN");
			session.removeAttribute("username");
			session.removeAttribute("timeOutFG");
		} else {
			model.addAttribute("vlVerification", maXN);
			model.addAttribute("vlNewPassword", newPassword);
			model.addAttribute("vlCfNewPassword", cfNewPassword);
		}
		return "account/fg-changePassword";
	}

	@PostMapping("/forgot-password/cancel")
	public String cancelFG() {
		session.removeAttribute("maXN");
		session.removeAttribute("emailXN");
		session.removeAttribute("username");
		session.removeAttribute("timeOutFG");
		return "redirect:/account/forgot-password";
	}

	// Update
	@GetMapping("/update")
	public String update(Model model) {
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
			model.addAttribute("vlEmail", account.getEmail());
			model.addAttribute("vlPhone", account.getPhone());
		}
		return "account/update";
	}

	@PostMapping("/update")
	public String update(Account account, Model model) {
		boolean check = true;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (account.getFullName().isEmpty()) {
			model.addAttribute("msFullName", "Vui lòng nhập họ và tên");
			check = false;
		}

		if (account.getAddress().isEmpty()) {
			model.addAttribute("msAddress", "Vui lòng nhập địa chỉ");
			check = false;
		}

		if (account.getEmail().isEmpty()) {
			model.addAttribute("msEmail", "Vui lòng nhập địa chỉ email");
			check = false;
		} else {
			Pattern p = Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
			if (!p.matcher(account.getEmail()).find()) {
				model.addAttribute("msEmail", "Vui lòng nhập đúng định dạng email");
				check = false;
			}
		}

		if (account.getPhone().isEmpty()) {
			model.addAttribute("msPhone", "Vui lòng nhập số điện thoại");
			check = false;
		}

		if (check) {
			Account accountId = null;
			if (auth.getPrincipal() instanceof OAuth2User) {
				OAuth2User user = (OAuth2User) auth.getPrincipal();
				String id = user.getAttribute("id");
				if (id != null) {
					accountId = daoAccount.findById(id).get();
				} else {
					String email = user.getAttribute("email");
					accountId = daoAccount.findById(email).get();
				}
			} else {
				accountId = daoAccount.findById(auth.getName()).get();
			}

			accountId.setFullName(account.getFullName());
			accountId.setAddress(account.getAddress());
			accountId.setEmail(account.getEmail());
			accountId.setPhone(account.getPhone());

			daoAccount.saveAndFlush(accountId);
			model.addAttribute("updateSC", "");
			model.addAttribute("vlFullName", accountId.getFullName());
			model.addAttribute("vlAddress", accountId.getAddress());
			model.addAttribute("vlEmail", accountId.getEmail());
			model.addAttribute("vlPhone", accountId.getPhone());
		} else {
			model.addAttribute("vlFullName", account.getFullName());
			model.addAttribute("vlAddress", account.getAddress());
			model.addAttribute("vlEmail", account.getEmail());
			model.addAttribute("vlPhone", account.getPhone());
		}
		return "account/update";
	}

	// Change-password
	@GetMapping("/change-password")
	public String change_password() {
		return "account/change-password";
	}

	@PostMapping("/change-password")
	public String change_password(Model model, @RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword, @RequestParam("cfNewPassword") String cfNewPassword) {
		boolean check = true;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		Account account = daoAccount.findById(auth.getName()).get();
		if (oldPassword.isEmpty()) {
			model.addAttribute("msOldPassword", "Vui lòng nhập mật khẩu cũ");
			check = false;
		} else {
			if (!encoder.matches(oldPassword, account.getPassword())) {
				model.addAttribute("msOldPassword", "Mật khẩu không chính xác");
				check = false;
			}
		}

		if (newPassword.isEmpty()) {
			model.addAttribute("msNewPassword", "Vui lòng nhập mật khẩu mới");
			check = false;
		} else {
			if (!newPassword.equals(cfNewPassword)) {
				model.addAttribute("msNewPassword", "Mật khẩu chưa khớp");
				check = false;
			}
		}

		if (cfNewPassword.isEmpty()) {
			model.addAttribute("msCfNewPassword", "Vui lòng nhập nhập lại mật khẩu mới");
			check = false;
		} else {
			if (!newPassword.equals(cfNewPassword)) {
				model.addAttribute("msCfNewPassword", "Mật khẩu chưa khớp");
				check = false;
			}
		}

		if (check) {
			account.setPassword(encoder.encode(newPassword));
			daoAccount.saveAndFlush(account);
			model.addAttribute("changeSC", "");
		} else {
			model.addAttribute("vlOldPassword", oldPassword);
			model.addAttribute("vlNewPassword", newPassword);
			model.addAttribute("vlCfNewPassword", cfNewPassword);
		}
		return "account/change-password";
	}
}
