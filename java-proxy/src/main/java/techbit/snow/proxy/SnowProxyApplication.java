package techbit.snow.proxy;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import techbit.snow.proxy.model.NamedPipe;

import java.io.IOException;

@EnableAsync
@SpringBootApplication
public class SnowProxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(SnowProxyApplication.class, args);
	}

	public static String pid() {
		return System.getProperty("PID");
	}

	@PostConstruct
	public void startup() throws IOException {
		NamedPipe.destroyAll();
	}

}
