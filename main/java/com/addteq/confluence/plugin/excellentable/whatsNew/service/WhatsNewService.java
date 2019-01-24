package com.addteq.confluence.plugin.excellentable.whatsNew.service;

import com.addteq.confluence.plugin.excellentable.whatsNew.model.WhatsNewModel;
import com.atlassian.activeobjects.tx.Transactional;

@Transactional
public interface WhatsNewService {

    WhatsNewModel getData(String location);

    Boolean setData(WhatsNewModel whatsNewModel);
}
