package com.shopkun.error;

import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.boot.web.server.ErrorPageRegistry;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@EnableWebSecurity
public class ErrorConfiguration implements ErrorPageRegistrar {

	@Override
	public void registerErrorPages(ErrorPageRegistry registry) {
		ErrorPage error404 = new ErrorPage(HttpStatus.NOT_FOUND, "/error404");
		ErrorPage error405 = new ErrorPage(HttpStatus.METHOD_NOT_ALLOWED, "/error404");
		ErrorPage error400 = new ErrorPage(HttpStatus.BAD_REQUEST, "/error404");
		ErrorPage error500 = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/error404");
		registry.addErrorPages(error404, error405, error400, error500);
	}

}
