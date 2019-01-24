package com.addteq.confluence.plugin.excellentable.blueprint;

import com.addteq.confluence.plugin.excellentable.ao.ExcellentableDB;
import com.addteq.service.excellentable.exc_io.utils.ETDateUtils;
import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.confluence.plugins.createcontent.api.contextproviders.AbstractBlueprintContextProvider;
import com.atlassian.confluence.plugins.createcontent.api.contextproviders.BlueprintContext;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.transaction.TransactionCallback;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public class ExcellentableBlueprintContextProvider extends AbstractBlueprintContextProvider {

    ActiveObjects ao;
    private final String ANONYMOUS = "Anonymous";
    static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ExcellentableBlueprintContextProvider.class);
    
    @Autowired
    public ExcellentableBlueprintContextProvider(@ComponentImport ActiveObjects ao) {
        this.ao = ao;
    }
    
    @Override
    protected BlueprintContext updateBlueprintContext(BlueprintContext blueprintContext) {
        
        String templateName = blueprintContext.get("template").toString();
        final String metaData;
        if(!templateName.isEmpty()){
            metaData = getTemplateDataFromJson(templateName);
        }else{
            metaData = "";
        }

        //Insert a new record in excellentable to get its its unique ID.
        ExcellentableDB excellentableDb = ao.executeInTransaction(new TransactionCallback<ExcellentableDB>() // (1)
        {
            @Override
            public ExcellentableDB doInTransaction() {
                ExcellentableDB excellentableDbInsert = ao.create(ExcellentableDB.class);
                excellentableDbInsert.setMetaData(metaData);
                excellentableDbInsert.setContentType("draft");
                excellentableDbInsert.setSpaceKey(blueprintContext.getSpaceKey());
                excellentableDbInsert.setCreated(ETDateUtils.currentTime());
                excellentableDbInsert.setCreator(getCurrentUsername());
                excellentableDbInsert.save();
                return excellentableDbInsert;
            }
        });

        blueprintContext.put("excellentable-id", excellentableDb.getID());
        return blueprintContext;
    }
    
    private String getTemplateDataFromJson(String templateName) {
        InputStream mInputStream = null;
        try {
            mInputStream = com.atlassian.core.util.ClassLoaderUtils.getResourceAsStream("/blueprint/templates/" + templateName + ".json", getClass());
            if (mInputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(mInputStream,"UTF-8"));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                reader.close();
                return builder.toString();
            }
        } catch (Exception ex) {
            LOGGER.error("Exception occured while getting data from template"+ex);
        }
        return "";
    }
    
    private String getCurrentUsername() {
        if (AuthenticatedUserThreadLocal.isAnonymousUser()) {
            return ANONYMOUS;
        } else {
            return AuthenticatedUserThreadLocal.get().getName();
        }
    }
}
