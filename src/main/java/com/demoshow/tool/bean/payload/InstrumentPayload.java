package com.demoshow.tool.bean.payload;

import lombok.Data;

import java.util.List;

@Data
public class InstrumentPayload {
    private String instrumentId;
    private String cusDate;  // early payment day
    private String liqDate;  // settlement day
    private String amrDateRange;
    private List<String> amrDateList;
}
