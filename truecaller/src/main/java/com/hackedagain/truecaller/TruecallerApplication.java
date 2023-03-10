package com.hackedagain.truecaller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import com.hackedagain.truecaller.services.MostFrequentNameSelectionStrategy;
import com.hackedagain.truecaller.services.TrueCallerProfileService;


/*
 * Return name against a number
 * Save a name against a number
 * Show if a number is spam
 * Mark a number as spam
 * Show if a number is business, telemarketer, etc.
 * Search for number by name
 * Import contacts to global directory
 * Block/Unblock number
 */

@ComponentScan(basePackages={"com.hackedagain.truecaller"})
@SpringBootApplication
public class TruecallerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TruecallerApplication.class, args);
	}

	@Bean
	public TrueCallerProfileService trueCallerProfileService() {
		return new TrueCallerProfileService(new MostFrequentNameSelectionStrategy());
	}
}

