package com.tweet.scanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Pankaj
 *
 *This class is main entry point of this application.
 */
@SpringBootApplication
//@EnableScheduling
public class TweetScannerApp {

	public static void main(String[] args) {

		SpringApplication.run(TweetScannerApp.class);
	}

}
