package com.bortmanco.postgrescdcstarter;

import com.bortmanco.postgrescdcstarter.config.PostgresCDCConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(PostgresCDCConfig.class)
public class PostgresCdcStarterConfiguration {

}
