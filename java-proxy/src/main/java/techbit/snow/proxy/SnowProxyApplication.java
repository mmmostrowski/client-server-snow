package techbit.snow.proxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

import java.nio.file.Path;

@EnableAsync
@SpringBootApplication
@SuppressWarnings("unused")
public class SnowProxyApplication {

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

	@Bean
	public Path pipesDir() {
		return Path.of(System.getProperty("user.dir") + "/../.pipes/");
	}

}
