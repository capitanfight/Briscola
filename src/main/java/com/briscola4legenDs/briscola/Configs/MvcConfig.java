package com.briscola4legenDs.briscola.Configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/index").setViewName("index");
		registry.addViewController("/").setViewName("index");
		registry.addViewController("/login").setViewName("login");
		registry.addViewController("/register").setViewName("register");
		registry.addViewController("/settings").setViewName("settings");
		registry.addViewController("/startGame").setViewName("startGame");
		registry.addViewController("/lobby").setViewName("lobby");
		registry.addViewController("/account").setViewName("account");
		registry.addViewController("/leaderboard").setViewName("leaderboard");
		registry.addViewController("/FAQ").setViewName("FAQ");
		registry.addViewController("/rules").setViewName("rules");
		registry.addViewController("/history").setViewName("history");
	}

}