package com.demoshow.tool.bean.entity;

import com.demoshow.tool.enums.FileStatus;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.sql.Clob;
import java.util.Date;

@Entity
@Table(name = "UPLOAD_FILE")
@Data
public class UploadFileEntity {
    @Id
    @GenericGenerator(name = "system-uuid",strategy = "uuid")
    @GeneratedValue(generator = "system-uuid")
    private String id;

    @Column(name = "TASK_ID")
    private String taskId;

    @Column(name = "FILE_NAME")
    private String fileName;

    @Column(name = "FILE_PATH")
    private String filePath;

    @Column(name = "INSTRUMENT_ID")
    private String instrumentId;

    @Column(name = "MESSAGE_TYPE")
    private String messageType;

    @Column(name = "ACTIVITY_TYPE")
    private String activityType;

    @Column(name = "MESSAGE_DATE")
    private Date messageDate;

    @Column(name = "FILE_CONTENT")
    private Clob fileContent;

    @Column(name = "FILE_STATUS")
    @Enumerated(EnumType.STRING)
    private FileStatus fileStatus;

    @Column(name = "SEND_PRIORITY")
    private int sendPriority;


    public void setActivityType(String activityType) {
        this.activityType = activityType;
        switch (activityType) {
            case "CUS":
                sendPriority = 1;
                break;
            case "AMR":
                sendPriority = 2;
                break;
            case "LIQ":
                sendPriority = 3;
                break;
            case "SOD":
                sendPriority = 4;
                break;
            default:
                sendPriority = 9;
                break;
        }
    }
}
