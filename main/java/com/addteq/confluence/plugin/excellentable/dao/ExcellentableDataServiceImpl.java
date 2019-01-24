package com.addteq.confluence.plugin.excellentable.dao;

import com.addteq.confluence.plugin.excellentable.ao.EditHistoryDB;
import com.addteq.confluence.plugin.excellentable.ao.ExcellentableDB;
import com.addteq.confluence.plugin.excellentable.model.ExcellentableModel;
import com.addteq.service.excellentable.exc_io.utils.ETDateUtils;
import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import net.java.ao.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExcellentableDataServiceImpl implements ExcellentableDataService {
    private final ActiveObjects ao;

    @Autowired
    public ExcellentableDataServiceImpl(@ComponentImport ActiveObjects ao) {
        this.ao = ao;
    }

    @Override
    public ExcellentableDB getTable(final Integer excId) {
        ExcellentableDB[] excellentableDBS = ao.executeInTransaction(() -> ao.find(ExcellentableDB.class, "ID = ? ", excId));
        if ( excellentableDBS == null || excellentableDBS.length != 1)
            return null;
        return excellentableDBS[0];
    }

    @Override
    public ExcellentableModel getTableData(final Integer excId, final ExcellentableDB excellentableDB, final ExcellentableModel excellentableModel) {
        ExcellentableModel model = ao.executeInTransaction(() -> {
            EditHistoryDB[] editHistoryDBS = ao.find(EditHistoryDB.class,
                    Query.select().where("TABLE_DBID = ?", excId).order("ID DESC"));
            final String metaData = excellentableDB.getMetaData() == null? "": excellentableDB.getMetaData();
            excellentableModel.setMetaData(metaData);
            excellentableModel.setContentEntityId(excellentableDB.getContentEntityId());
            excellentableModel.setContentType(excellentableDB.getContentType());
            excellentableModel.setSpaceKey(excellentableDB.getSpaceKey());
            excellentableModel.setCreated(excellentableDB.getCreated());
            excellentableModel.setCreatedDate(ETDateUtils.getFormattedDate(excellentableDB.getCreated()));
            excellentableModel.setCreator(excellentableDB.getCreator());
            excellentableModel.setUpdated(excellentableDB.getUpdated());
            excellentableModel.setUpdatedDate(ETDateUtils.getFormattedDate(excellentableDB.getUpdated()));
            excellentableModel.setUpdater(excellentableDB.getUpdater());
            excellentableModel.setHistoryID(editHistoryDBS.length > 0 ? editHistoryDBS[0].getID() : -1);
            excellentableModel.setVersionNumber(editHistoryDBS.length);
            excellentableModel.setID(excellentableDB.getID());
            return excellentableModel;
        });
        return model;
    }

    @Override
    public ExcellentableModel getContentData(final ExcellentableDB excellentableDB, final ExcellentableModel excellentableModel) {
        ExcellentableModel model = ao.executeInTransaction(() -> {
            excellentableModel.setContentEntityId(excellentableDB.getContentEntityId());
            excellentableModel.setContentType(excellentableDB.getContentType());
            excellentableModel.setSpaceKey(excellentableDB.getSpaceKey());
            excellentableModel.setID(excellentableDB.getID());
            return excellentableModel;
        });
        return model;
    }
}
