package com.demoshow.tool.repository;

import com.demoshow.tool.bean.entity.UploadFileEntity;
import com.demoshow.tool.enums.FileStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UploadFileRepository extends JpaRepository<UploadFileEntity,String> {
    List<UploadFileEntity> findByFileStatus (FileStatus status);
    UploadFileEntity findFirstByInstrumentIdAndActivityType (String instrumentId, String activityType);
    List<UploadFileEntity> findByFileStatusOrderByMessageDate(FileStatus status);
}
