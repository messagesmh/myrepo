package com.demoshow.tool.service;

import com.demoshow.tool.bean.entity.DownstreamConfigEntity;
import com.demoshow.tool.repository.DownstreamConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class DownstreamConfigService {
    @Autowired
    DownstreamConfigRepository repository;

    public DownstreamConfigEntity findByDownstreamName (String name) {
        try {
            return repository.findByDownstreamName(name);
        } catch (Exception e) {

        }
        return null;
    }



    public void save (DownstreamConfigEntity entity) {
        try {
            repository.save(entity);
        } catch (Exception e) {

        }
    }




    public List<DownstreamConfigEntity> findByStatus (boolean status) {
        try {
            return repository.findByEnabled(status);
        } catch (Exception e) {

        }
        return new LinkedList<>();
    }
}
