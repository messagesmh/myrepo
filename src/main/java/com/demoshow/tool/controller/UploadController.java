package com.demoshow.tool.controller;

import com.demoshow.tool.bean.entity.UploadFileEntity;
import com.demoshow.tool.enums.FileStatus;
import com.demoshow.tool.service.UploadFileService;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialClob;
import javax.sql.rowset.serial.SerialException;
import java.io.*;
import java.sql.Clob;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class UploadController {

    @Autowired
    private UploadFileService service;

    @RequestMapping("/upload")
    public Map<String, Object> fileUpload(MultipartFile file, Model model) throws IOException {

        Document xmlDocument = readXmlFileDocument(file.getInputStream());
        String xmlFileStr = readXmlFileString(file.getInputStream());
        saveFileToDb(xmlDocument, xmlFileStr);

        Map map = new HashMap<String,Object>();
        map.put("code",0);

        return map;
    }




    /**
     *
     * @param fileInputStream
     * @return
     */
    private Document readXmlFileDocument (InputStream fileInputStream) {
        Document xmlDocument = null;
        try {
            SAXReader saxReader = new SAXReader();
            xmlDocument = saxReader.read(fileInputStream);
        } catch (DocumentException docException) {

        }
        return xmlDocument;
    }



    /**
     *
     * @param fileInputStream
     * @return
     */
    private String readXmlFileString(InputStream fileInputStream) {
        String xmlFileStr = "";
        try {
            String line = "";
            StringBuffer sb = new StringBuffer();
            BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream, "UTF-8"));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            xmlFileStr = sb.toString();
        } catch (IOException ioException) {

        } catch (Exception e) {

        }
        return xmlFileStr;
    }




    private void saveFileToDb (Document xmlDocument, String xmlString) {
        UploadFileEntity uploadFileEntity = new UploadFileEntity();
        SerialClob serialClob = null;
        try {
            serialClob = new SerialClob(xmlString.toCharArray());
            uploadFileEntity.setFileContent(serialClob);

            uploadFileEntity.setFileStatus(FileStatus.UPLOADED);

            Element rootElement = xmlDocument.getRootElement();

            String messageType = rootElement.element("Header").element("MessageType").getStringValue();
            uploadFileEntity.setMessageType(messageType);

            String dateSent = rootElement.element("Header").element("DateSent").getStringValue();
            uploadFileEntity.setMessageDate(new SimpleDateFormat("yyyyMMdd").parse(dateSent));

            uploadFileEntity.setActivityType(getActivityTypeByMessageType(messageType, rootElement));

            uploadFileEntity.setInstrumentId(getInstrumentIdByMessageType(messageType, rootElement));

            service.save(uploadFileEntity);

        } catch (SerialException serialException) {

        } catch (SQLException sqlException) {

        } catch (Exception e) {

        }
    }



    private String getActivityTypeByMessageType (String messageType, Element rootElement) {
        String activityType = "";
        switch (messageType) {
            case "ACCTV21":
                activityType = rootElement.element("SubHeader").element("SplitActivities").element("ActivityType").getStringValue();
                break;
            case "BALMIS":
                activityType = rootElement.element("SubHeader").element("ActivityType").getStringValue();
                break;
            default:
                break;
        }
        return activityType;
    }




    private String getInstrumentIdByMessageType (String messageType, Element rootElement) {
        String instrumentId = "";
        switch (messageType) {
            case "ACCTV21":
                instrumentId = rootElement.element("SubHeader").element("SplitActivities").element("InstrumentID").getStringValue();
                break;
            case "BALMIS":
                instrumentId = rootElement.element("SubHeader").element("InstrumentID").getStringValue();
                break;
            default:
                break;
        }
        return instrumentId;
    }
}
