package com.app.AutomationSales;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling              // turns on @Scheduled (the daily renewal check)
@ConfigurationPropertiesScan   // picks up RenewalProperties (app.renewal.* in application.yaml)
public class AutomationSalesApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutomationSalesApplication.class, args);
	}

}
