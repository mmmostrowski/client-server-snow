package techbit.snow.proxy.service.stream;

import jakarta.annotation.PostConstruct;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

@Component
public class NamedPipes {

    @PostConstruct
    public void destroyAll() throws IOException {
        FileUtils.cleanDirectory(pipesDir().toFile());
    }

    @Bean
    @Qualifier("namedPipes.dir")
    public Path pipesDir() {
        return Path.of(System.getProperty("user.dir") + "/../.pipes/");
    }

}
