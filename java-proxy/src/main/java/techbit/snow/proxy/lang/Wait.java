package techbit.snow.proxy.lang;

import lombok.SneakyThrows;

public class Wait {

    @SneakyThrows
    public static void milliseconds(int milliseconds)  {
        Thread.sleep(milliseconds);
    }

}
