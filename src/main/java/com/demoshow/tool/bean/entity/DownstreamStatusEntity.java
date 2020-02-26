package com.demoshow.tool.bean.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Data
@Entity
@Table(name = "DOWNSTREAM_STATUS")
public class DownstreamStatusEntity {

    @Id
    @GenericGenerator(name = "system-uuid",strategy = "uuid")
    @GeneratedValue(generator = "system-uuid")
    private String id;

    @Column(name = "DOWNSTREAM_NAME")
    private String downstreamName;

    @Column(name = "FILE_GENERATED")
    private Integer fileGenerated;

    @Column(name = "DOWNSTREAM_STATUS")
    private String downstreamStatus;

    @Column(name = "TASK_ID")
    private String taskId;

}
