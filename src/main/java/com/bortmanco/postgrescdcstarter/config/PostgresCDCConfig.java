package com.bortmanco.postgrescdcstarter.config;


import com.bortmanco.postgrescdcstarter.decode.PgoutHendler;
import com.bortmanco.postgrescdcstarter.decode.PgoutMsgDecoder;
import com.bortmanco.postgrescdcstarter.decode.impl.PgoutHendlerImpl;
import com.bortmanco.postgrescdcstarter.decode.impl.PgoutMsgDecoderImpl;
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
