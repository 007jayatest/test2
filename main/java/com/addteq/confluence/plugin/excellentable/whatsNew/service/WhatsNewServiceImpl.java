package com.addteq.confluence.plugin.excellentable.whatsNew.service;

import com.addteq.confluence.plugin.excellentable.whatsNew.ao.WhatsNewDB;
import com.addteq.confluence.plugin.excellentable.whatsNew.model.WhatsNewModel;
import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import net.java.ao.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
public class WhatsNewServiceImpl implements WhatsNewService {

    private final ActiveObjects ao;
    private static final Logger LOGGER = LoggerFactory.getLogger(WhatsNewServiceImpl.class);

    @Autowired
    public WhatsNewServiceImpl(@ComponentImport ActiveObjects ao) {
        this.ao = ao;
    }

    /**
     * Get data based on user and location
     * @param location
     * @return
     */
    @Override
    public WhatsNewModel getData(String location) {
        ConfluenceUser confluenceUser = AuthenticatedUserThreadLocal.get();
        WhatsNewDB[] whatsNewDBS = ao.find(WhatsNewDB.class, Query.select().where("USER = ? AND LOCATION = ? ", confluenceUser.getKey().toString(), location));
        WhatsNewModel whatsNewModel = new WhatsNewModel();
        whatsNewModel.setData(whatsNewDBS);
        return whatsNewModel;
    }

    /**
     * Set data based on user, location and version
     * @param whatsNewModel
     * @return
     */
    @Override
    public Boolean setData(WhatsNewModel whatsNewModel) {
        ConfluenceUser confluenceUser = AuthenticatedUserThreadLocal.get();
        for (Map.Entry<Integer, String[]> entry : whatsNewModel.getData().entrySet()) {
            WhatsNewDB[] whatsNewDB1 = ao.find(WhatsNewDB.class,Query.select().where("USER = ? AND LOCATION = ? AND VERSION = ?",
                    confluenceUser.getKey().toString(), whatsNewModel.getLocation(), entry.getKey()));
            if (whatsNewDB1.length == 0){
                WhatsNewDB whatsNewDB = ao.create(WhatsNewDB.class);
                whatsNewDB.setLocation(whatsNewModel.getLocation());
                whatsNewDB.setUser(confluenceUser.getKey().toString());
                whatsNewDB.setVersion(entry.getKey());
                whatsNewDB.setAttempts(Integer.parseInt(entry.getValue()[0]));
                whatsNewDB.setSubscription(Boolean.parseBoolean(entry.getValue()[1]));
                whatsNewDB.save();
            } else {
                WhatsNewDB whatsNewDB = whatsNewDB1[0];
                whatsNewDB.setLocation(whatsNewModel.getLocation());
                whatsNewDB.setUser(confluenceUser.getKey().toString());
                whatsNewDB.setVersion(entry.getKey());
                whatsNewDB.setAttempts(Integer.parseInt(entry.getValue()[0]));
                whatsNewDB.setSubscription(Boolean.parseBoolean(entry.getValue()[1]));
                whatsNewDB.save();
            }
        }
        return true;
    }
}
