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
@SuppressWarnings("unused")
public final class SnowProxyApplication {

	private final String developerMode;

	public SnowProxyApplication(
			@Value("${phpsnow.developer-mode:PRODUCTION}") String developerMode)
	{
		this.developerMode = developerMode;
	}

	public static void main(String[] args) {
		SpringApplication.run(SnowProxyApplication.class, args);
	}

	@Bean
	public String applicationPid() {
		return System.getProperty("PID");
	}

	@Bean
	public boolean isDeveloperMode() {
		return developerMode.equalsIgnoreCase("develop");
	}

}
