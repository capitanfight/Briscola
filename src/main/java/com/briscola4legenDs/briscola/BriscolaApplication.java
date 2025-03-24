package com.briscola4legenDs.briscola;

import game.Card;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class BriscolaApplication {

	public static void main(String[] args) {
		SpringApplication.run(BriscolaApplication.class, args);
	}

}
