package com.addteq.confluence.plugin.excellentable.multiedit.dao;

import com.addteq.confluence.plugin.excellentable.multiedit.ao.LiveEditConfigDB;
import com.addteq.confluence.plugin.excellentable.multiedit.model.MultieditConnectionInfo;
import com.addteq.confluence.plugin.excellentable.permission.ExcPermissionManager;
import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.test.TestActiveObjects;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.atlassian.upm.api.license.PluginLicenseManager;
import net.java.ao.EntityManager;
import net.java.ao.Query;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(ActiveObjectsJUnitRunner.class)
public class MultieditSettingsDaoImplTest {
    private EntityManager entityManager;

    private CollaboratorServiceImpl collaboratorService;

    @Mock
    private ExcPermissionManager excPermissionManager;
    @Mock
    private SettingsManager settingsManager;
    @Mock
    private PluginLicenseManager pluginLicenseManager;
    @Mock
    private SoyTemplateRenderer soyTemplateRenderer;

    private ActiveObjects ao;

    private  MultieditSettingsDaoImpl multieditSettingsDao;

    private MultieditConnectionInfo multieditConnectionInfo;

    private LiveEditConfigDB[] liveEditConfigDBS;

    @Before
    public void setUp() throws Exception {
        assertNotNull(entityManager);
        ao = new TestActiveObjects(entityManager);
        collaboratorService = new CollaboratorServiceImpl(new TestActiveObjects(entityManager), excPermissionManager);
        //Pointed AO to CollaboratorDB.class
        ao.migrate(LiveEditConfigDB.class);

        //Initalizing class
        multieditSettingsDao = new MultieditSettingsDaoImpl(ao);
        //Sample Object and population of values
       multieditConnectionInfo = new MultieditConnectionInfo();
        multieditConnectionInfo.setStatus(1);
        multieditConnectionInfo.setConfluenceId("1234567890");
        multieditConnectionInfo.setPublicKey("-----BEGIN PUBLIC KEY-----\n" +
                "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCEGVFR/sl7uXv9EANL8PBAeQPV\n" +
                "peTKqzQDAfv6R6JhDoTmAizcazHGYWMNVRzW1O3/O7DFHgEDqdoEhITBCjc1xwCE\n" +
                "OxedysUIq71OQo4UCpPRKWn/86QdJiQlXqTnPoYASfFIpywpqBArw0ZRFID9CcNk\n" +
                "Lc+7arDBUiY6dKaeqQIDAQAB\n" +
                "-----END PUBLIC KEY-----\n");
        multieditConnectionInfo.setFirebaseUrl("https://collabeditin-microservice-prod.firebaseio.com/");
        multieditConnectionInfo.setApiKey("AIzaSyD90DhAbOWP-4-hzsigefj4sP-Uoy9Jlok");
        multieditConnectionInfo.setFirebaseContext("/instances/-Kw5_L4B3mu0ayKPkwBj");
        //Saving some settings
        multieditSettingsDao.saveMultieditSettings(multieditConnectionInfo);
        liveEditConfigDBS = ao.find(LiveEditConfigDB.class, Query.select());

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void saveMultieditSettings() throws Exception {
        assertEquals("Number of records", 1, liveEditConfigDBS.length);
        assertEquals("Status",multieditConnectionInfo.getStatus() ,liveEditConfigDBS[0].getStatus());
        assertEquals("ConfluenceId",multieditConnectionInfo.getConfluenceId(),liveEditConfigDBS[0].getConfluenceId());
        assertEquals("Public Key",multieditConnectionInfo.getPublicKey(),liveEditConfigDBS[0].getPublicKey());
        assertEquals("Firebase Url",multieditConnectionInfo.getFirebaseUrl(),liveEditConfigDBS[0].getFirebaseUrl());
        assertEquals("Api Key",multieditConnectionInfo.getApiKey(),liveEditConfigDBS[0].getApiKey());
        assertEquals("Api Key",multieditConnectionInfo.getFirebaseContext(),liveEditConfigDBS[0].getFirebaseContext());
    }

    @Test
    public void nullShouldNotbeAllowedForConfluenceIDInAOInMultieditSettings() throws Exception {
        //Set confluenceID to null in the multiediconnectionInfo and try saving it in the active objects
        multieditConnectionInfo.setConfluenceId(null);
        multieditSettingsDao.saveMultieditSettings(multieditConnectionInfo);
        //Confluence Id should still remain 1234567890 as done in the setUp()
        assertEquals("ConfluenceId",liveEditConfigDBS[0].getConfluenceId(),"1234567890");


    }
}
