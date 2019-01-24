package com.addteq.confluence.plugin.excellentable.multiedit.dao;

import com.addteq.confluence.plugin.excellentable.multiedit.ao.LiveEditRegisterDB;
import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.activeobjects.test.TestActiveObjects;
import net.java.ao.EntityManager;
import net.java.ao.Query;
import net.java.ao.test.junit.ActiveObjectsJUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit Tests for LiveEditRegisterDB AO Entity
 *
 * Created by yagnesh.bhat on 12/20/18.
 */

@RunWith(ActiveObjectsJUnitRunner.class)
public class LiveEditRegisterServiceImplTest {

    private EntityManager entityManager;
    private ActiveObjects ao;
    private LiveEditRegisterServiceImpl liveEditRegisterService;

    private LiveEditRegisterDB[] liveEditRegisterDBs;

    @Before
    public void setUp() throws Exception {
        assertNotNull(entityManager);
        ao = new TestActiveObjects(entityManager);

        ao.migrate(LiveEditRegisterDB.class);

        //Initalizing class
        liveEditRegisterService = new LiveEditRegisterServiceImpl(ao);

        //Sample Object and population of values
        liveEditRegisterService.registerMultiEditAttempt();

        liveEditRegisterDBs = ao.find(LiveEditRegisterDB.class, Query.select());
    }

    @Test
    public void multiEditAttemptMustBePersistedInAO() {
        assertEquals("There should be only one record of multieditattempt", 1, liveEditRegisterDBs.length);
        assertTrue("Tried column value should be true", liveEditRegisterDBs[0].isTried());
    }

    @Test
    public void thereShouldBeOnlyOneRowPopulatedInAOAtAllTime() {
        //Try attempting to call this method again, it shouldnt add an additional row!
        liveEditRegisterService.registerMultiEditAttempt();
        liveEditRegisterDBs = ao.find(LiveEditRegisterDB.class, Query.select());
        assertEquals("There should be only one record of multieditattempt", 1, liveEditRegisterDBs.length);
        assertTrue("Tried column value should be true", liveEditRegisterDBs[0].isTried());
    }

}
