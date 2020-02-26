package com.demoshow.tool.controller;

import com.demoshow.tool.bean.entity.DownstreamStatusEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.LinkedList;
import java.util.List;

@Controller
public class UpdateStatusController {

    @RequestMapping("/updatestatus")
    public String updateStatus (Model model) {

        // todo: update downstreams status
        List<DownstreamStatusEntity> downstreamStatusEntities = new LinkedList<>();
        DownstreamStatusEntity downstreamStatusEntity = new DownstreamStatusEntity();
        downstreamStatusEntity.setDownstreamName("QRM");
        downstreamStatusEntity.setFileGenerated(1);
        downstreamStatusEntities.add(downstreamStatusEntity);
        model.addAttribute("downstreams", downstreamStatusEntities);
        return "result::update_status";
    }
}
