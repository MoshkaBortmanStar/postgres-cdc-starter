package io.github.moshkabortmanstar;

import io.github.moshkabortmanstar.config.PostgresCDCConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(PostgresCDCConfig.class)
public class PostgresCdcStarterConfiguration {

}
