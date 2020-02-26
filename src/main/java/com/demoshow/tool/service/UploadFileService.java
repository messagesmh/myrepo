package com.demoshow.tool.service;

import com.demoshow.tool.bean.entity.UploadFileEntity;
import com.demoshow.tool.enums.FileStatus;
import com.demoshow.tool.repository.UploadFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class UploadFileService {

    @Autowired
    private UploadFileRepository repository;


    public void save (UploadFileEntity entity) {
        try {
            repository.save(entity);
        } catch (Exception e) {

        }
    }


    public List<UploadFileEntity> findByFileStatus (FileStatus fileStatus) {
        try {
            return repository.findByFileStatus(fileStatus);
        } catch (Exception e) {

        }
        return new LinkedList<>();
    }


    public UploadFileEntity findFirstByInstrumentIdAndActivityType (String instrumentId, String activityType) {
        try {
            return repository.findFirstByInstrumentIdAndActivityType(instrumentId, activityType);
        } catch (Exception e) {

        }
        return null;
    }



    public List<UploadFileEntity> findNeedSendMessagesAndOrderedByDate() {
        try {
            return repository.findByFileStatusOrderByMessageDate(FileStatus.READY_FOR_SEND);
        } catch (Exception e) {

        }
        return new LinkedList<>();
    }
}
