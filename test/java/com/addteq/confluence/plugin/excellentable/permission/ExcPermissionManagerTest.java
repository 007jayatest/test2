package com.addteq.confluence.plugin.excellentable.permission;

import com.addteq.confluence.plugin.excellentable.ao.ExcellentableDB;
import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.internal.ContentEntityManagerInternal;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.templates.PageTemplate;
import com.atlassian.confluence.pages.templates.PageTemplateManager;
import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.ConfluenceUser;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Created by rober on 8/26/2016.
 */
public class ExcPermissionManagerTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    
    @Mock
    private ActiveObjects ao;
    @Mock
    private SpaceManager spaceManager;
    @Mock
    private PageTemplateManager pageTemplateManager;
    @Mock
    private PageTemplate pageTemplate;
    @Mock
    private PermissionManager permissionManager;
    @Mock
    private ContentEntityManagerInternal contentEntityManagerInternal;
    @Mock
    private ContentEntityObject contentEntityObject;
    @Mock
    private Space space;
    @Mock
    private ConfluenceUser confluenceUser;

    private Permission mockedPermission;

    private ExcPermissionManagerImpl epm;

    private ExcellentableDB[] excellentableDBs;
    private final long CONTENT_ENTITY_ID = 1;
    private final String CONTENT_TYPE = "TEMPLATE";
    private final String SPACE_KEY = "TEST";

    @Before
    public void PrepareMocks() {
        epm = new ExcPermissionManagerImpl(ao,spaceManager, pageTemplateManager,
                        permissionManager, contentEntityManagerInternal) {
            @Override
            protected ConfluenceUser getCurrentUserBeingUsed() {
                return confluenceUser;
            }

            @Override
            protected Permission getPermissionTypeOverridable(String permissionType) {
                return mockedPermission;
            }
        };

        ExcellentableDB excDB = mock(ExcellentableDB.class);
        Mockito.when(excDB.getContentEntityId()).thenReturn(CONTENT_ENTITY_ID);
        Mockito.when(excDB.getContentType()).thenReturn(CONTENT_TYPE);
        Mockito.when(excDB.getSpaceKey()).thenReturn(SPACE_KEY);
        excellentableDBs = new ExcellentableDB[] {excDB};
    }

    @Test
    public void AllowPermissionwWhenPageAlreadyExists() {
        setMockedPermission_UsingTestedClassMethod("VIEW");
        fromContentEntityManagerInternal_getById_return(contentEntityObject);
        fromPermissionManager_hasPermissionOnContentEntityObject_return(true);
        checkPermissionOnExcellentable_And_AssertTrue("VIEW");

        setMockedPermission_UsingTestedClassMethod("EDIT");
        fromContentEntityManagerInternal_getById_return(contentEntityObject);
        fromPermissionManager_hasPermissionOnContentEntityObject_return(true);
        checkPermissionOnExcellentable_And_AssertTrue("EDIT");
    }

    @Test
    public void AllowedToViewWhenPageIsNotFoundButSpaceIs() {
        setMockedPermission_UsingTestedClassMethod("VIEW");

        fromContentEntityManagerInternal_getById_return(null);
        fromSpaceManager_getSpace_return(space);
        fromPermissionManager_hasPermissionOnSpace_return(true);

        checkPermissionOnExcellentable_And_AssertTrue("VIEW");
    }

    @Test
    public void AllowedToEditWhenPageIsJustBeingCreated() {
        setMockedPermission_UsingTestedClassMethod("EDIT");

        fromContentEntityManagerInternal_getById_return(null);
        fromSpaceManager_getSpace_return(space);
        fromPermissionManager_hasCreatePermission_return(true);

        checkPermissionOnExcellentable_And_AssertTrue("EDIT");
    }

    @Test
    public void DenyAnyPermissionWhenExcellentableIsNull() {
        JsonObject permissionResult = epm.hasPermissionOnExcellentable((ExcellentableDB[])null, "VIEW");
        assertFalse(permissionResult.get("status").getAsBoolean());

        permissionResult = epm.hasPermissionOnExcellentable((ExcellentableDB[]) null, "EDIT");
        assertFalse(permissionResult.get("status").getAsBoolean());
    }

    @Test
    public void PermissionCheckResultsInFalse() {
        setMockedPermission_UsingTestedClassMethod("VIEW");

        fromContentEntityManagerInternal_getById_return(contentEntityObject);
        fromPermissionManager_hasPermissionOnContentEntityObject_return(false);

        JsonObject permissionResult = epm.hasPermissionOnExcellentable(excellentableDBs, "VIEW");
        assertFalse(permissionResult.get("status").getAsBoolean());
    }

    @Test
    public void AllowPermission_WhenContentEntityIdIsNotZero_And_ContentTypeIsTemplate() {
        setMockedPermission_UsingTestedClassMethod("VIEW");
        fromPageTemplateManager_getPageTemplate_return(pageTemplate);
        fromPermissionManager_hasPermissionOnPageTemplate_return(true);

        JsonObject permissionResult =
                epm.hasPermissionOnContentEntity(CONTENT_ENTITY_ID, CONTENT_TYPE, SPACE_KEY, "VIEW");
        assertTrue(permissionResult.get("status").getAsBoolean());

        setMockedPermission_UsingTestedClassMethod("EDIT");
        fromPageTemplateManager_getPageTemplate_return(pageTemplate);
        fromPermissionManager_hasPermissionOnPageTemplate_return(true);

        permissionResult =
                epm.hasPermissionOnContentEntity(CONTENT_ENTITY_ID, CONTENT_TYPE, SPACE_KEY, "EDIT");
        assertTrue(permissionResult.get("status").getAsBoolean());
    }

    @Test
    public void AllowEdit_WhenContentEntityIsJustBeingCreated() {
        setMockedPermission_UsingTestedClassMethod("EDIT");

        fromSpaceManager_getSpace_return(space);
        fromPermissionManager_hasCreatePermission_return(true);

        JsonObject permissionResult =
                epm.hasPermissionOnContentEntity(0L, CONTENT_TYPE, SPACE_KEY, "EDIT");
        assertTrue(permissionResult.get("status").getAsBoolean());
    }

    @Test
    public void AllowView_WhenContentEntityIsZero_ButSpaceIsNot() {
        setMockedPermission_UsingTestedClassMethod("VIEW");

        fromSpaceManager_getSpace_return(space);
        fromPermissionManager_hasPermissionOnSpace_return(true);

        JsonObject permissionResult =
                epm.hasPermissionOnContentEntity(0L, CONTENT_TYPE, SPACE_KEY, "VIEW");
        assertTrue(permissionResult.get("status").getAsBoolean());
    }

    @Test
    public void AllowPermission_WhenContentEntityIdIsNotZero_And_ContentTypeNotTemplate() {
        setMockedPermission_UsingTestedClassMethod("VIEW");

        fromContentEntityManagerInternal_getById_return(contentEntityObject);
        fromPermissionManager_hasPermissionOnContentEntityObject_return(true);

        JsonObject permissionResult =
                epm.hasPermissionOnContentEntity(CONTENT_ENTITY_ID, "", SPACE_KEY, "VIEW");
        assertTrue(permissionResult.get("status").getAsBoolean());

        setMockedPermission_UsingTestedClassMethod("EDIT");

        fromContentEntityManagerInternal_getById_return(contentEntityObject);
        fromPermissionManager_hasPermissionOnContentEntityObject_return(true);

        permissionResult =
                epm.hasPermissionOnContentEntity(CONTENT_ENTITY_ID, "", SPACE_KEY, "EDIT");
        assertTrue(permissionResult.get("status").getAsBoolean());
    }

    @Test
    public void DenyPermissionWhenContentEntityIdIsZero_And_SpaceKeyIsEmpty() {
        JsonObject permissionResult =
                epm.hasPermissionOnContentEntity(0L, CONTENT_TYPE, "", "VIEW");
        assertFalse(permissionResult.get("status").getAsBoolean());

        /* TODO: This test validates unreachable code (bug), that it never gets to write 400, goes to 401
         Correct behavior should be to show the correct error
         assertTrue(permissionResult.getString("status-code").equals("400"));*/
    }

    @Test
    public void DenyPermissionInAnyCaseOnContentEntity() {
        fromPermissionManager_hasPermissionOnPageTemplate_return(false);

        JsonObject permissionResult =
                epm.hasPermissionOnContentEntity(CONTENT_ENTITY_ID, CONTENT_TYPE, SPACE_KEY, "VIEW");
        assertFalse(permissionResult.get("status").getAsBoolean());

        permissionResult =
                epm.hasPermissionOnContentEntity(CONTENT_ENTITY_ID, CONTENT_TYPE, SPACE_KEY, "VIEW");
        assertFalse(permissionResult.get("status").getAsBoolean());
    }

    private void setMockedPermission_UsingTestedClassMethod(String permissionType) {
        // This expects that VIEW permission is returned
        // Using the tested class' function to include it in the coverage
        mockedPermission = ExcPermissionManagerImpl.getPermissionTypeStatic(permissionType);
    }

    private void fromContentEntityManagerInternal_getById_return(ContentEntityObject contentEntityObject) {
        Mockito.when(contentEntityManagerInternal.getById(CONTENT_ENTITY_ID))
                .thenReturn(contentEntityObject);
    }

    private void fromSpaceManager_getSpace_return(Space space) {
        Mockito.when(spaceManager.getSpace(SPACE_KEY))
                .thenReturn(space);
    }

    private void fromPageTemplateManager_getPageTemplate_return(PageTemplate pageTemplate) {
        Mockito.when(pageTemplateManager.getPageTemplate(CONTENT_ENTITY_ID))
                .thenReturn(pageTemplate);
    }

    private void fromPermissionManager_hasPermissionOnContentEntityObject_return(boolean hasPermission) {
        Mockito.when(permissionManager.hasPermission(confluenceUser, mockedPermission, contentEntityObject))
                .thenReturn(hasPermission);
    }

    private void fromPermissionManager_hasPermissionOnSpace_return(boolean hasPermission) {
        Mockito.when(permissionManager.hasPermission(confluenceUser, mockedPermission, space))
                .thenReturn(hasPermission);
    }

    private void fromPermissionManager_hasCreatePermission_return(boolean hasPermission) {
        //Class pageClass = Page.class;
        Mockito.when(permissionManager.hasCreatePermission(confluenceUser, space, Page.class))
                .thenReturn(hasPermission);
    }

    private void fromPermissionManager_hasPermissionOnPageTemplate_return(boolean hasPermission) {
        Mockito.when(permissionManager.hasPermission(confluenceUser, mockedPermission, pageTemplate))
                .thenReturn(hasPermission);
    }

    private void checkPermissionOnExcellentable_And_AssertTrue(String permissionType) {
        JsonObject permissionResult = epm.hasPermissionOnExcellentable(excellentableDBs, permissionType);
        assertTrue(permissionResult.get("status").getAsBoolean());
    }
}
