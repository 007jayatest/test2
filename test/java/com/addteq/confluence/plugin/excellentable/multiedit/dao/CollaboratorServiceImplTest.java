package com.addteq.confluence.plugin.excellentable.multiedit.dao;

import com.addteq.confluence.plugin.excellentable.multiedit.ao.CollaboratorDB;
import com.addteq.confluence.plugin.excellentable.permission.ExcPermissionManager;
import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.test.TestActiveObjects;
import net.java.ao.EntityManager;
import net.java.ao.Query;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.mockito.Mock;

@RunWith(ActiveObjectsJUnitRunner.class)
public class CollaboratorServiceImplTest {

    private EntityManager entityManager;

    private CollaboratorServiceImpl collaboratorService;
    
    @Mock
    private ExcPermissionManager excPermissionManager;

    private ActiveObjects ao;

    @Before
    public void setUp() throws Exception {
        assertNotNull(entityManager);
        ao = new TestActiveObjects(entityManager);
        collaboratorService = new CollaboratorServiceImpl(new TestActiveObjects(entityManager), excPermissionManager);
        //Pointed AO to CollaboratorDB.class
        ao.migrate(CollaboratorDB.class);
        collaboratorService.addUniqueCollaborator(2, "USER2KEY", 1);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void addUniqueCollaboratorTest() throws Exception {
        final int excId = 1;
        final String userKey = "USER1KEY";
        final int versionNumber = 1;
        //Empty DB
        assertEquals("Check if initialized DB by count",1, ao.find(CollaboratorDB.class).length);
        //New collaborator added
        final Integer i =  collaboratorService.addUniqueCollaborator(excId, userKey, versionNumber);
        ao.flushAll();

        assertEquals("Checking return variable when new user added.",new Integer(0), i);
        assertEquals("Check number of entries added", 1, ao.find(CollaboratorDB.class, Query.select().where("EXC_ID = ?",excId)).length);//1 Entry added

        //Trying to add same user again
        final Integer j =  collaboratorService.addUniqueCollaborator(excId, userKey, versionNumber);
        ao.flushAll();

        assertEquals("Checking return variable when user was already present.", new Integer(1), j); //new row updated instead of added
        CollaboratorDB[] collaboratorDB = ao.find(CollaboratorDB.class, Query.select().where("EXC_ID = ?", excId));
        assertEquals("Checking if calling method for existing user should only update", 1, collaboratorDB.length);
        assertEquals("Validating Id inserted", new Integer(1), collaboratorDB[0].getExcID());
        assertEquals("Validating UserKey Inserted", "USER1KEY", collaboratorDB[0].getUserKey());
        assertEquals("Validating versionNumber inserted", new Integer(1),collaboratorDB[0].getVersion());
    }

    @Test
    public void getCollaboratorByUserKeyTest() throws Exception {
        //Value already present 2, USER2KEY, 1
        final int excId = 2;
        final String userKey = "USER2KEY";

        //Check if 1 record is present
        assertEquals("Check DB initial state", 1, ao.count(CollaboratorDB.class));

        //Case 1: Valid Values
        CollaboratorDB collaboratorDB = collaboratorService.getCollaboratorByUserKey(excId, userKey);
        assertTrue("Did found userKey", collaboratorDB instanceof CollaboratorDB);
        CollaboratorDB collaboratorDB1 = collaboratorService.getCollaboratorByUserKey(excId, "USER3KEY");
        assertEquals("Did not found userKey", null, collaboratorDB1);
        CollaboratorDB collaboratorDB2 = collaboratorService.getCollaboratorByUserKey(10, "USER2KEY");
        assertEquals("Did not found id", null, collaboratorDB2);

        //Case 2:Border Conditions
        //Case 3:Incorrect parameters
    }

    @Test
    public void getCollaboratorsTest() {
        //Value already present 2, USER2KEY, 1

        //Check if 1 record is present
        assertEquals("Check DB initial state", 1, ao.count(CollaboratorDB.class));

        //Case 1: Valid Values
        collaboratorService.addUniqueCollaborator(2, "USER1KEY", 1);
        CollaboratorDB[] collaboratorDBS = collaboratorService.getCollaborators(2, 60);
        int actualValue = ao.count(CollaboratorDB.class, Query.select().where("EXC_ID = ?", 2));
        assertEquals("Check if it returns two entries from return", actualValue, collaboratorDBS.length);
        assertEquals("Check if it returns two entries from db", actualValue, collaboratorDBS.length);
        CollaboratorDB[] collaboratorDBS1 = collaboratorService.getCollaborators(11, 300);
        assertEquals("If no record is present", 0, collaboratorDBS1.length);
    }

    @Test
    public void updateOrCreateCollaboratorTest() throws Exception {
        addUniqueCollaboratorTest();
    }


    @Test
    public void deleteCollaboratorTest() throws Exception {
        //Value already present 2, USER2KEY, 1

        //Check if 1 record is present
        assertEquals("Check DB initial state", 1, ao.count(CollaboratorDB.class));

        //Case 1: Valid Values
        int recordsFound = collaboratorService.deleteCollaborator(2, "USER2KEY");
        assertEquals("Delete a record from return", 1, recordsFound);//Returned Value
        CollaboratorDB[] collaboratorDBS = ao.find(CollaboratorDB.class, Query.select().where("EXC_ID = ? AND USER_KEY = ?", 2, "USER2KEY"));
        int actualValue = collaboratorDBS==null?0:collaboratorDBS.length;
        assertEquals("Delete a record from db", 0, actualValue);

        int recordsFound1 = collaboratorService.deleteCollaborator(2, "USER2KEY");
        assertEquals("Delete an empty record from return", 0, recordsFound1);
        CollaboratorDB[] collaboratorDBS1 = ao.find(CollaboratorDB.class, Query.select().where("EXC_ID = ? AND USER_KEY = ?", 2, "USER2KEY"));
        int actualValue1 = collaboratorDBS1==null?0:collaboratorDBS1.length;
        assertEquals("Delete an empty record from DB", 0, actualValue1);
    }
    }
