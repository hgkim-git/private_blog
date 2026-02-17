package io.github.hgkimer.privateblog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
// JPA Auditing 활성화를 통해 엔티티의 생성/수정 시간 정보를 자동으로 관리
@EnableJpaAuditing
public class JpaConfig {

}
