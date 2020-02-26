package com.demoshow.tool.controller;


import com.demoshow.tool.bean.MapKeyComparator;
import com.demoshow.tool.bean.entity.DownstreamConfigEntity;
import com.demoshow.tool.bean.entity.UploadFileEntity;
import com.demoshow.tool.bean.payload.DateOrderedPayload;
import com.demoshow.tool.bean.payload.InstrumentPayload;
import com.demoshow.tool.bean.payload.InstrumentPayloadPackage;
import com.demoshow.tool.bean.payload.MsgPayload;
import com.demoshow.tool.constant.Constant;
import com.demoshow.tool.enums.FileStatus;
import com.demoshow.tool.service.DownstreamConfigService;
import com.demoshow.tool.service.UploadFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.sql.rowset.serial.SerialClob;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Controller
public class GenerateMessageController {

    @Autowired
    private UploadFileService uploadFileService;
    @Autowired
    private DownstreamConfigService downstreamConfigService;


    @RequestMapping("/generate")
    public String groupMessages(@ModelAttribute(name = "msgpayload") MsgPayload msgPayload, Model model){

        setDownstreamConfig(msgPayload);

        List<InstrumentPayload> instrumentPayloadList = createInstrumentPayloadList();
        model.addAttribute("instrumentPayloadList", instrumentPayloadList);

        InstrumentPayloadPackage payloadPackage = new InstrumentPayloadPackage();
        payloadPackage.setInstrumentPayloadList(instrumentPayloadList);
        model.addAttribute("instrumentPayloadPackage", payloadPackage);


        return "setupamr";
    }




    @PostMapping("/calculateamr")
    public String calculateAmrMessage (@ModelAttribute(name = "instrumentPayloadPackage") InstrumentPayloadPackage payloadPackage, Model model) {
        List<InstrumentPayload> instrumentPayloadList = payloadPackage.getInstrumentPayloadList();
        for (InstrumentPayload payload:instrumentPayloadList) {
            String amrDateRange = payload.getAmrDateRange();
            List<String> dateRange = calculateAmrMessages(payload, amrDateRange);
            payload.setAmrDateList(dateRange);
            calculateSodMessages(payload);
            SetCusAndLiqMessageStatusToReadyToSend(payload.getInstrumentId());
        }
        model.addAttribute("instrumentPayloadList", instrumentPayloadList);

        return "allmsgs";
    }






    @PostMapping("/send")
    public String showProgressAndResult(Model model) {

        List<DownstreamConfigEntity> downstreamEnabled = downstreamConfigService.findByStatus(true);
        List<UploadFileEntity> msgEntityList = uploadFileService.findNeedSendMessagesAndOrderedByDate();

        List<String> downstreamNameList = getDownstreamNames(downstreamEnabled);
        Map<String, DateOrderedPayload> payloadMap = new HashMap<>(); // key: date
        Map<String, List<UploadFileEntity>> sendMsgMap = new HashMap<>(); // k: date

        for (UploadFileEntity entity:msgEntityList) {
            createDateOrderedPayloadMap(downstreamNameList, payloadMap, entity);
            createSendMsgMap(sendMsgMap, entity);
        }

        Map<String, DateOrderedPayload> sortedPayloadMap = sortPayloadMapByDate(payloadMap);
        model.addAttribute("dateOrderedList", sortedPayloadMap.values());

        Map<String, List<UploadFileEntity>> sortedSendMsgMap = sortSendMapByDate(sendMsgMap);
        for(Map.Entry<String, List<UploadFileEntity>> entry:sortedSendMsgMap.entrySet()) {
            List<UploadFileEntity> entities = entry.getValue();
            for (UploadFileEntity entity:entities) {
                String fileContent = convertClobToString(entity.getFileContent());
                for (DownstreamConfigEntity configEntity:downstreamEnabled) {
                    String url = configEntity.getUrl();
                    // todo: prepare payload and send to downstream
                }
                entity.setFileStatus(FileStatus.SENDING);
                uploadFileService.save(entity);
            }
        }

        return "result";
    }




    private String convertClobToString (Clob clob) {
        String reString = "";
        try {
            Reader characterStream = clob.getCharacterStream();
            BufferedReader br = new BufferedReader(characterStream);
            String s = br.readLine();
            StringBuffer sb = new StringBuffer();
            while (s != null) {
                sb.append(s);
                s = br.readLine();
            }
            reString = sb.toString();

        } catch (SQLException e) {

        } catch (IOException e) {

        }
        return reString;
    }




    private void setDownstreamConfig(MsgPayload msgPayload) {
        updateDownstreamConfigEntity(msgPayload.isCbCls(), "CLS");
        updateDownstreamConfigEntity(msgPayload.isCbFtp(), "FTP");
        updateDownstreamConfigEntity(msgPayload.isCbQrm(), "QRM");
        updateDownstreamConfigEntity(msgPayload.isCbRemind(), "REMIND");
    }



    private void updateDownstreamConfigEntity (boolean enabled, String downstreamName) {
        DownstreamConfigEntity entity = downstreamConfigService.findByDownstreamName(downstreamName);
        entity = entity == null?new DownstreamConfigEntity():entity;
        entity.setDownstreamName(downstreamName);
        entity.setEnabled(enabled);
        downstreamConfigService.save(entity);
    }



    private List<String> calculateAmrMessages(InstrumentPayload payload, String amrDateRange) {
        String instrumentId = payload.getInstrumentId();
        List<String> everydayList = new LinkedList<>();
        String[] dateRange = amrDateRange.split(" - ");

        // calculate amr messages
        LocalDate startDate = convertStringToLocalDate(dateRange[0]);
        LocalDate endDate = convertStringToLocalDate(dateRange[1]);

        int days = ((int) ChronoUnit.DAYS.between(startDate, endDate))+1;

        long dailyAmrAmount = getDailyAmrAmount (days, instrumentId, payload.getCusDate());

        Calendar startCalendar = convertStringToCalendar(dateRange[0]);
        for (int i=0; i<days; i++) {
            Date currAmrDate = startCalendar.getTime();
            String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(currAmrDate);
            everydayList.add(dateStr);
            createAmrMsgAndSaveToDb (currAmrDate, dateStr.replace("-",""), dailyAmrAmount, instrumentId);
            startCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return everydayList;
    }




    private void calculateSodMessages(InstrumentPayload payload) {
        String instrumentId = payload.getInstrumentId();
        String cusDate = payload.getCusDate();
        String liqDate = payload.getLiqDate();

        // calculate sod messages
        LocalDate startDate = convertStringToLocalDate(cusDate);
        LocalDate endDate = convertStringToLocalDate(liqDate);

        int days = ((int) ChronoUnit.DAYS.between(startDate, endDate))+1;

        Calendar startCalendar = convertStringToCalendar(cusDate);
        for (int i=0; i<days; i++) {
            Date currSodDate = startCalendar.getTime();
            String currSodDateDateStr = new SimpleDateFormat("yyyyMMdd").format(currSodDate);
            createSodMsgAndSaveToDb(currSodDate, currSodDateDateStr);
            startCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }






    private void createSendMsgMap (Map<String, List<UploadFileEntity>> sendMsgMap, UploadFileEntity entity) {
        Date messageDate = entity.getMessageDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String msgDateStr = sdf.format(messageDate);

        if (sendMsgMap.containsKey(msgDateStr)) {
            List<UploadFileEntity> list = sendMsgMap.get(msgDateStr);
            int insertPostion = -1;
            for (UploadFileEntity item:list) {
                if (item.getSendPriority() > entity.getSendPriority()) {
                    insertPostion = list.indexOf(item);
                    break;
                }
            }
            if (insertPostion < 0) {
                list.add(entity);
            } else {
                list.add(insertPostion, entity);
            }
        } else {
            ArrayList<UploadFileEntity> list = new ArrayList<>();
            list.add(entity);
            sendMsgMap.put(msgDateStr, list);
        }
    }



    private void createDateOrderedPayloadMap(List<String> downstreamNameList, Map<String, DateOrderedPayload> payloadMap, UploadFileEntity entity) {
        Date messageDate = entity.getMessageDate();
        String activityType = entity.getActivityType();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = sdf.format(messageDate);
        if (payloadMap.containsKey(dateStr)) {
            DateOrderedPayload payload = payloadMap.get(dateStr);
            Map<String, Integer> msgCountMap = payload.getMsgCountMap();
            updateMsgCountMap(activityType, msgCountMap);
        } else {
            DateOrderedPayload payload = new DateOrderedPayload();
            payload.setDate(dateStr);
            payload.setDownstreams(downstreamNameList);

            Map<String, Integer> msgCountMap = new HashMap<>();
            updateMsgCountMap(activityType, msgCountMap);
            payload.setMsgCountMap(msgCountMap);

            payloadMap.put(dateStr, payload);
        }
    }


    private void updateMsgCountMap(String activityType, Map<String, Integer> msgCountMap) {
        if (msgCountMap.containsKey(activityType)) {
            msgCountMap.replace(activityType, msgCountMap.get(activityType)+1);
        } else {
            msgCountMap.put(activityType, 1);
        }
    }


    private List<String> getDownstreamNames(List<DownstreamConfigEntity> downstreamEnabled) {
        List<String> downstreamNameList = new LinkedList<>();
        for (DownstreamConfigEntity entity:downstreamEnabled) {
            downstreamNameList.add(entity.getDownstreamName());
        }
        return downstreamNameList;
    }





    private List<InstrumentPayload> createInstrumentPayloadList() {

        Map<String, InstrumentPayload> instrumentPayloadMap = new HashMap<>();
        // read UPLOADED file from db, group by instrument id
        List<UploadFileEntity> entities = uploadFileService.findByFileStatus(FileStatus.UPLOADED);
        for(UploadFileEntity entity:entities) {
            String instrumentId = entity.getInstrumentId();
            if (!instrumentPayloadMap.containsKey(instrumentId)) {
                instrumentPayloadMap.put(instrumentId, createInstrumentPayload(instrumentId));
            }
        }

        List<InstrumentPayload> instrumentPayloadList = new LinkedList<>();
        instrumentPayloadList.addAll(instrumentPayloadMap.values());
        return instrumentPayloadList;
    }




    private InstrumentPayload createInstrumentPayload(String instrumentId) {
        InstrumentPayload payload = new InstrumentPayload();
        payload.setInstrumentId(instrumentId);
        payload.setCusDate(getActivityDateByInstrumentIdAndActivityType(instrumentId,"CUS"));
        payload.setLiqDate(getActivityDateByInstrumentIdAndActivityType(instrumentId, "LIQ"));
        return payload;
    }


    private String getActivityDateByInstrumentIdAndActivityType (String instrumentId, String activityType) {
        UploadFileEntity entity = uploadFileService.findFirstByInstrumentIdAndActivityType(instrumentId, activityType);
        if (entity == null) {
            return "";
        }
        Date messageDate = entity.getMessageDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(messageDate);
    }



    private LocalDate convertStringToLocalDate(String str) {
        try {
            return LocalDate.parse(str);
        } catch (Exception e) {

        }
        return LocalDate.now();
    }



    private Calendar convertStringToCalendar (String dateStr) {
        Calendar instance = Calendar.getInstance();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(dateStr);
            instance.setTime(date);
        } catch (Exception e) {

        }
        return instance;
    }



    private long getDailyAmrAmount(int days, String instrumentId, String cusDate) {
        // todo: get instrument amount
        return 10;
    }


    private void createAmrMsgAndSaveToDb(Date currAmrDate, String currAmrDateStr, long dailyAmount, String instrumentId) {
        UploadFileEntity entity = new UploadFileEntity();
        entity.setInstrumentId(instrumentId);
        entity.setActivityType("AMR");
        entity.setMessageDate(currAmrDate);
        entity.setMessageType("ACCTV21");
        entity.setFileStatus(FileStatus.READY_FOR_SEND);
        try {
            String amrMsgStr = String.format(Constant.amrTemplete, "ACCTV21", currAmrDateStr, instrumentId, dailyAmount);
            entity.setFileContent(new SerialClob(amrMsgStr.toCharArray()));
        } catch (SQLException e) {

        }
        uploadFileService.save(entity);
    }



    private void createSodMsgAndSaveToDb(Date currSodDate, String currSodDateStr) {
        UploadFileEntity entity = new UploadFileEntity();
        entity.setMessageDate(currSodDate);
        entity.setMessageType("SODMESS");
        entity.setActivityType("SOD");
        entity.setFileStatus(FileStatus.READY_FOR_SEND);
        try {
            Calendar instance = Calendar.getInstance();
            instance.setTime(currSodDate);
            instance.add(Calendar.DAY_OF_MONTH, -1);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String prevDateStr = sdf.format(instance.getTime());
            String sodMsgStr = String.format(Constant.sodTemplete, currSodDateStr, prevDateStr, currSodDateStr);
            entity.setFileContent(new SerialClob(sodMsgStr.toCharArray()));
        } catch (SQLException e) {

        }
        uploadFileService.save(entity);
    }



    private void SetCusAndLiqMessageStatusToReadyToSend (String instrumentId) {
        setReadyToSendStatus(instrumentId, "CUS");
        setReadyToSendStatus(instrumentId, "LIQ");
    }

    private void setReadyToSendStatus(String instrumentId, String activityType) {
        UploadFileEntity cusEntity = uploadFileService.findFirstByInstrumentIdAndActivityType(instrumentId, activityType);
        cusEntity.setFileStatus(FileStatus.READY_FOR_SEND);
        uploadFileService.save(cusEntity);
    }



    private Map<String, DateOrderedPayload> sortPayloadMapByDate (Map<String, DateOrderedPayload> oriMap) {
        Map<String, DateOrderedPayload> sortMap = new TreeMap<>(new MapKeyComparator());
        sortMap.putAll(oriMap);
        return sortMap;
    }


    private Map<String, List<UploadFileEntity>> sortSendMapByDate (Map<String, List<UploadFileEntity>> oriMap) {
        Map<String, List<UploadFileEntity>> sortMap = new TreeMap<>(new MapKeyComparator());
        sortMap.putAll(oriMap);
        return sortMap;
    }
}
