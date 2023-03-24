package techbit.snow.proxy.service.phpsnow;

import java.util.Map;

public interface PhpSnowConfigFactory {

    PhpSnowConfig create(Map<String, String> config);

}
