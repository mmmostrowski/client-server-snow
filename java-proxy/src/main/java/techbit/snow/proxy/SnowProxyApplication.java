package techbit.snow.proxy;

import lombok.Generated;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@Generated
public class SnowProxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(SnowProxyApplication.class, args);
	}

	@Bean("application.pid")
	public String pid() {
		return System.getProperty("PID");
	}

}
