package com.demoshow.tool.bean.payload;

import lombok.Data;

@Data
public class MsgPayload {

    private boolean cbCls;
    private boolean cbQrm;
    private boolean cbFtp;
    private boolean cbRemind;

}
