package com.briscola4legenDs.briscola.Security;

import com.briscola4legenDs.briscola.User.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
// TODO: implementare JWT
	private final UserService userService;

	@Autowired
	public WebSecurityConfig(UserService userService) {
		this.userService = userService;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
            .csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(requests -> requests
				.requestMatchers(
					// register api
					"/api/user/register"
				).permitAll()
				.requestMatchers(
					"api/room",
					"api/room/**"
				).hasRole("USER")
				.requestMatchers(
					// home page (html)
					"/", "/index",
					// resources
					"/css/**", "/js/**", "/img/**",
					// register page (html)
					"/register"
				).permitAll()
				.anyRequest().authenticated()
			)
			.formLogin(form -> form
				.defaultSuccessUrl("/startGame")
				.loginProcessingUrl("/login")
				.loginPage("/login")
				.permitAll()
			)
			.logout(logout -> logout
				.logoutSuccessUrl("/login?logout")
				.permitAll()
			);

		return http.build();
	}

//	@Bean
//	public AuthenticationProvider authenticationProvider() {
//		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
//		daoAuthenticationProvider.setUserDetailsService(userService);
//		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
//		return daoAuthenticationProvider;
//	}

	@Bean
	public UserDetailsService userDetailService() {
		return userService;
	}
}