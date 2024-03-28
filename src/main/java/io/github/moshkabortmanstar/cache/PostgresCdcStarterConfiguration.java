package io.github.moshkabortmanstar.cache;

import io.github.moshkabortmanstar.cache.config.PostgresCDCConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(PostgresCDCConfig.class)
public class PostgresCdcStarterConfiguration {

}
