package com.demoshow.tool.constant;

public class Constant {

    public static String amrTemplete = "<?xml version=\"1.0\"?><Proponix><Header><MessageType>%s</MessageType><DateSent>%s</DateSent></Header><SubHeader><SplitActivities><InstrumentID>%s</InstrumentID><ActivityType>AMR</ActivityType><Amount>%s</Amount></SplitActivities></SubHeader><Body></Body></Proponix>";  // todo: to be completed
    public static String sodTemplete = "<?xml version=\"1.0\"?><Proponix><Header><MessageType>SODMESS</MessageType><DateSent>%s</DateSent></Header><Body><PerviousBusinessDate>%s</PerviousBusinessDate><CurrentBusinessDate>%s</CurrentBusinessDate></Body></Proponix>";  // todo: to be completed
}
