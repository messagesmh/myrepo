package com.demoshow.tool.bean.entity;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Data
@Entity
@Table(name = "DOWNSTREAM_CONFIG")
public class DownstreamConfigEntity {

    @Id
    @GenericGenerator(name = "system-uuid",strategy = "uuid")
    @GeneratedValue(generator = "system-uuid")
    private String id;

    @Column(name = "DOWNSTREAM_NAME")
    private String downstreamName;

    @Column(name = "ENABLED")
    private boolean enabled;

    @Column(name = "URL")
    private String url;

    public void setDownstreamName(String downstreamName) {
        this.downstreamName = downstreamName;
        switch (downstreamName) {
            case "CLS":
                url = "http://localhost:8080/send";
                break;
            case "QRM":
                url = "http://localhost:8080/send";
                break;
            case "FTP":
                url = "http://localhost:8080/send";
                break;
            case "REMIND":
                url = "http://localhost:8080/send";
                break;
            case "ADE":
                url = "http://localhost:8080/send";
                break;
            default:
                url = "http://localhost:8080/send";
                break;
        }
    }
}
