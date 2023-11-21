package com.shopkun.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {

	@GetMapping("/error404")
	public String error404() {
		return "errors/error404";
	}
}
