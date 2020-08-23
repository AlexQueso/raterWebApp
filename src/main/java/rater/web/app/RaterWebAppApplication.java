package rater.web.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RaterWebAppApplication {

	public static final Logger LOGGER= LoggerFactory.getLogger(RaterWebAppApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(RaterWebAppApplication.class, args);

		LOGGER.info("Logger initialized!");
	}

}
