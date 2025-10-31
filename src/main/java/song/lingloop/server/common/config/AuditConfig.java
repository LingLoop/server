package song.lingloop.server.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
public class AuditConfig {

    @Bean
    public AuditorAware<Long> auditorAware() {
        return () -> Optional.of(SecurityContextHolder.getContext()
                        .getAuthentication().getName())
                .map(Long::valueOf);
    }
}
