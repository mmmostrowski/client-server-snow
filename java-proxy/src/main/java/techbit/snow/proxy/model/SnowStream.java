package techbit.snow.proxy.model;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor(staticName = "of")
public class SnowStream {

    private final String sessionId;

    private boolean started = false;

    private PhpSnowApp phpSnow;

    public void startPhpApp() {
        started = true;
    }

    public void streamTo(OutputStream out) throws IOException {
        if (!started) {
            throw new IllegalStateException("You must startPhpApp() first!");
        }

        String text = "HELLO: " + sessionId;
        out.write(text.getBytes(StandardCharsets.UTF_8));
    }
}
