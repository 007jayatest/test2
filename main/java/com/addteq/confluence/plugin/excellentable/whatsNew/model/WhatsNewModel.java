package com.addteq.confluence.plugin.excellentable.whatsNew.model;

import com.addteq.confluence.plugin.excellentable.whatsNew.ao.WhatsNewDB;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Data structure -
 * location :       String
 * example :        "view"
 * data :           {version: ["attempts", "subscription"]}
 * example :        {1: ['1', 'true'], 2: ['0', 'true']}
 */
@XmlRootElement(name = "WhatsNewModel")
@XmlAccessorType(XmlAccessType.FIELD)
public class WhatsNewModel {

    private String location;
    private Map<Integer, String[]> data;

    public WhatsNewModel() {
    }

    public WhatsNewModel(String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setData(WhatsNewDB[] whatsNewDBS) {
        Map<Integer, String[]> data = new HashMap<>();
        for (WhatsNewDB whatsNewDB: whatsNewDBS) {
            String[] strings = new String[2];
            strings[0] = Integer.toString(whatsNewDB.getAttempts());
            strings[1] = Boolean.toString(whatsNewDB.getSubscription());
            data.put(whatsNewDB.getVersion(), strings);
        }
        this.data = data;
    }

    public Map<Integer, String[]> getData(){
        return data;
    }

}
