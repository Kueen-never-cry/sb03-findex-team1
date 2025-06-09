package com.kueennevercry.findex.repository;

import com.kueennevercry.findex.entity.IntegrationTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// JpaRepository 와 SyncJobCustomRepository(QueryDSL을 위한 레포) 상속받음
@Repository
public interface SyncJobRepository extends JpaRepository<IntegrationTask, Long>,
    SyncJobCustomRepository {
  /*  JpaRepository를 상속받아 기본적인 CRUD 연산을 제공합니다.*/
}
