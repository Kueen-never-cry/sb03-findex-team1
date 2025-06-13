package com.kueennevercry.findex.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueryDSLConfig {

  // @Autowired 안됨.
  // 이유 : JPQL을 타입 세이프하게 만들어주는 빌더 도구일뿐 실제 쿼리를 실행하는 주체는 EntityManager임.
  @PersistenceContext
  private EntityManager em;

  @Bean
  public JPAQueryFactory jpaQueryFactory() {
    return new JPAQueryFactory(em);
  }

}
