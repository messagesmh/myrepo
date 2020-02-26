package com.demoshow.tool.repository;

import com.demoshow.tool.bean.entity.DownstreamConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DownstreamConfigRepository extends JpaRepository<DownstreamConfigEntity,String> {
    DownstreamConfigEntity findByDownstreamName (String downstreamName);
    List<DownstreamConfigEntity> findByEnabled(boolean status);
}
