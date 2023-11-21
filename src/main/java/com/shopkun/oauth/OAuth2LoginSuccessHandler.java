package com.shopkun.oauth;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.shopkun.dao.AccountDao;
import com.shopkun.entity.Account;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
	@Autowired
	private HttpSession session;
	@Autowired
	private AccountDao dao;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		OAuth2User user = (OAuth2User) authentication.getPrincipal();
		String id = user.getAttribute("id");
		Account account = new Account();

		if (id != null) { // facebook
			Optional<Account> accountFB = dao.findById(user.getAttribute("id"));
			if (!accountFB.isPresent()) {
				account.setUsername(id);
				account.setFullName(user.getAttribute("name"));
				account.setEmail(user.getAttribute("email"));
				account.setRole("ROLE_USER");
				dao.save(account);
			}
		} else { // google
			Optional<Account> accountGG = dao.findById(user.getAttribute("email"));
			if (!accountGG.isPresent()) {
				account.setUsername(user.getAttribute("email"));
				account.setFullName(user.getAttribute("name"));
				account.setEmail(user.getAttribute("email"));
				account.setRole("ROLE_USER");
				dao.save(account);
			}
		}

		session.setAttribute("auth", "auth");
		session.setMaxInactiveInterval(1 * 24 * 60 * 60 * 1000);
		super.onAuthenticationSuccess(request, response, authentication);
	}
}
