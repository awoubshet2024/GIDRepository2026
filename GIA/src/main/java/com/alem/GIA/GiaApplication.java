package com.alem.GIA;

import com.alem.GIA.entity.FeeConfig;
import com.alem.GIA.enumes.MaritalStatus;
import com.alem.GIA.repository.FeeConfigRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.math.BigDecimal;

@SpringBootApplication
@EnableScheduling
public class GiaApplication {

	public static void main(String[] args) {

		SpringApplication.run(GiaApplication.class, args);
	}
	@Bean
	CommandLineRunner seedFeeConfig(FeeConfigRepository repo) {
		return args -> {

			if(repo.count() == 0){

				repo.save(createConfig(MaritalStatus.SINGLE, 20));
				repo.save(createConfig(MaritalStatus.SINGLE_WITH_CHILD, 25));
				repo.save(createConfig(MaritalStatus.MARRIED_WITHOUT_CHILD, 25));
				repo.save(createConfig(MaritalStatus.MARRIED_WITH_CHILD, 30));
			}

		};
	}

	private FeeConfig createConfig(MaritalStatus status, int fee){
		FeeConfig config = new FeeConfig();
		config.setMaritalStatus(status);
		config.setMonthlyFee(BigDecimal.valueOf(fee));
		return config;
	}

}
