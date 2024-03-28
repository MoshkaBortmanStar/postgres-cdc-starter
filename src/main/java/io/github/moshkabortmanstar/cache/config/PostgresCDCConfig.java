package io.github.moshkabortmanstar.cache.config;


import io.github.moshkabortmanstar.cache.decode.PgoutHendler;
import io.github.moshkabortmanstar.cache.decode.PgoutMsgDecoder;
import io.github.moshkabortmanstar.cache.decode.impl.PgoutHendlerImpl;
import io.github.moshkabortmanstar.cache.decode.impl.PgoutMsgDecoderImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "postgres-cdc.decoder.enabled", havingValue = "true")
public class PostgresCDCConfig {

    @Bean
    public PgoutMsgDecoder pgoutMsgDecoder() {
        return new PgoutMsgDecoderImpl();
    }

    @Bean
    public PgoutHendler pgoutHendler(PgoutMsgDecoder pgoutMsgDecoder) {
        return new PgoutHendlerImpl(pgoutMsgDecoder);
    }

}
