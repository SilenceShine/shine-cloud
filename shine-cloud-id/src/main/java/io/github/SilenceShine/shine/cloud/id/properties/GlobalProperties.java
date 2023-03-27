package io.github.SilenceShine.shine.cloud.id.properties;

import io.github.SilenceShine.shine.spring.core.properties.ShineProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author SilenceShine
 * @since 1.0
 */
@Getter
@Setter
@ConfigurationProperties(prefix = ShineProperties.PREFIX + ".cloud.id")
public class GlobalProperties {

    private SnowflakeProperties snowflake;

}
