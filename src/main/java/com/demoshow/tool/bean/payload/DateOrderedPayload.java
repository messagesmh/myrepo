package com.demoshow.tool.bean.payload;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DateOrderedPayload {
    private String date;
    private List<String> downstreams;
    private Map<String, Integer> msgCountMap;
}
