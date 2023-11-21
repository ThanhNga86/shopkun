package com.shopkun.configuration;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import com.shopkun.oauth.OAuth2LoginSuccessHandler;


@Configuration
@EnableWebSecurity
public class MyConfiguration {
	@Autowired
	private OAuth2LoginSuccessHandler auth2LoginSuccessHandler;
	
	@Bean
	BCryptPasswordEncoder bCryptPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	JdbcUserDetailsManager jdbcUserDetailsManager(DataSource dataSource) {
		JdbcUserDetailsManager userDetailsManager = new JdbcUserDetailsManager(dataSource);
		userDetailsManager.setUsersByUsernameQuery("select username, password, activated from accounts where username = ?");
		userDetailsManager.setAuthoritiesByUsernameQuery("select username,role from accounts where username = ?");
		return userDetailsManager;
	}
	
	@SuppressWarnings("removal")
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(
			configurer->configurer
			.requestMatchers("/admin/**").hasRole("ADMIN")
			.requestMatchers("/account/update").authenticated()
			.requestMatchers("/account/change-password").authenticated()
			.requestMatchers("/shopping-cart").authenticated()
			.requestMatchers("/order-history").authenticated()
			.anyRequest().permitAll()
		).formLogin(
			form->form
			.loginPage("/account/login").loginProcessingUrl("/authenticateTheUser")
			// .defaultSuccessUrl("/", true)
			.permitAll()
		).oauth2Login(
			login->login
			.loginPage("/account/login")
			.userInfoEndpoint()
			.and().successHandler(auth2LoginSuccessHandler)
		).logout(
			logout->logout
			.logoutSuccessUrl("/account/login")
			.permitAll()
		).exceptionHandling(
			configurer->configurer
			.accessDeniedPage("/error404")
		);
		http.csrf(scrf->scrf.disable());
		return http.build();
	}
}
