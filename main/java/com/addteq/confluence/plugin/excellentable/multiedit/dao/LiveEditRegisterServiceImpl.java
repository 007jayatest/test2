package com.addteq.confluence.plugin.excellentable.multiedit.dao;

import com.addteq.confluence.plugin.excellentable.multiedit.ao.LiveEditRegisterDB;
import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by yagnesh.bhat on 12/19/18.
 */
@Component
public class LiveEditRegisterServiceImpl implements LiveEditRegisterService {

    private final ActiveObjects ao;

    @Autowired
    public LiveEditRegisterServiceImpl(@ComponentImport ActiveObjects ao) {
        this.ao = ao;
    }

    @Override
    public void registerMultiEditAttempt() {
        LiveEditRegisterDB[] liveEditRegisterDB = ao.find(LiveEditRegisterDB.class);
        if (liveEditRegisterDB.length > 0) {
            //If there is a row (there would be 1 MAX at all times), make sure its set to true
            if (!liveEditRegisterDB[0].isTried()) {
                liveEditRegisterDB[0].setTried(true);
                liveEditRegisterDB[0].save();
            }
        } else {
            //If the table is empty (this would be the case when registering multi-edit enable for the VERY first time
            LiveEditRegisterDB liveEditRegisterDBNew = ao.create(LiveEditRegisterDB.class);
            liveEditRegisterDBNew.setTried(true);
            liveEditRegisterDBNew.save();
        }
    }

    @Override
    public boolean getRegisterMultiEditAttempt() {
        LiveEditRegisterDB[] liveEditRegisterDB = ao.find(LiveEditRegisterDB.class);
        if (liveEditRegisterDB.length > 0) {
            return liveEditRegisterDB[0].isTried();
        }
        return false;
    }
}
