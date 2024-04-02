package io.github.moshkabortmanstar.config;


import io.github.moshkabortmanstar.decode.PgoutHendler;
import io.github.moshkabortmanstar.decode.PgoutMsgDecoder;
import io.github.moshkabortmanstar.decode.impl.PgoutHendlerImpl;
import io.github.moshkabortmanstar.decode.impl.PgoutMsgDecoderImpl;
import io.github.moshkabortmanstar.service.ReplicationSlotPublicationService;
import io.github.moshkabortmanstar.service.impl.ReplicationSlotPublicationServiceImpl;
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

    @Bean
    ReplicationSlotPublicationService replicationSlotPublicationService() {
        return new ReplicationSlotPublicationServiceImpl();
    }

}
