package techbit.snow.proxy.service.phpsnow;

import lombok.Builder;
import lombok.Generated;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
@Value
@RequiredArgsConstructor
@Jacksonized
@Builder
@Generated
public class PhpSnowConfig {

    String presetName;
    int width;
    int height;
    Duration animationDuration;
    int fps;

}
