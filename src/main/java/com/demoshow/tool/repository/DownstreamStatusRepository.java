package com.demoshow.tool.repository;

import com.demoshow.tool.bean.entity.DownstreamStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DownstreamStatusRepository extends JpaRepository<DownstreamStatusEntity, String> {

    DownstreamStatusEntity findByDownstreamName (String name);

}
