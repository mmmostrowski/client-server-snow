package techbit.snow.proxy;

import lombok.Generated;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@Generated
@EnableAsync
@SpringBootApplication
public class SnowProxyApplication {

	@Value("${phpsnow.developer-mode:PRODUCTION}")
	private String developerMode;

	public static void main(String[] args) {
		SpringApplication.run(SnowProxyApplication.class, args);
	}

	@Bean("application.pid")
	public String pid() {
		return System.getProperty("PID");
	}

	@Bean("is.developer.mode")
	public boolean isDeveloperMode() {
		return developerMode.equals("DEVELOP");
	}

}
