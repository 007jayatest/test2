/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.addteq.confluence.plugin.excellentable.permission;

import com.addteq.confluence.plugin.excellentable.ao.ExcellentableDB;
import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.internal.ContentEntityManagerInternal;
import com.atlassian.confluence.json.json.Json;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.templates.PageTemplate;
import com.atlassian.confluence.pages.templates.PageTemplateManager;
import com.atlassian.confluence.security.Permission;
import com.atlassian.confluence.security.PermissionManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.gson.JsonObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class ExcPermissionManagerImpl implements ExcPermissionManager {

    private ActiveObjects ao;
    private SpaceManager spaceManager;
    private PageTemplateManager pageTemplateManager;
    private PermissionManager permissionManager;
    private ContentEntityManagerInternal contentEntityManagerInternal;

    @Autowired
    public ExcPermissionManagerImpl(
            @ComponentImport ActiveObjects ao,
            @ComponentImport SpaceManager spaceManager,
            @ComponentImport PageTemplateManager pageTemplateManager,
            @ComponentImport PermissionManager permissionManager,
            @ComponentImport ContentEntityManagerInternal contentEntityManagerInternal) {

        this.ao = ao;
        this.spaceManager = spaceManager;
        this.pageTemplateManager = pageTemplateManager;
        this.permissionManager = permissionManager;
        this.contentEntityManagerInternal = contentEntityManagerInternal;
    }

    /**
     * Checks whether loggedInUser has permission on specified Excellentable By ID
     *
     * @param excId
     * @param permissionType i.e Edit/View
     * @return JSONObject
     */
    @Override
    public JsonObject hasPermissionOnExcellentableById(int excId, String permissionType) {
        ExcellentableDB excellentableDB = ao.get(ExcellentableDB.class, excId);
        if (excellentableDB == null) {
            return createNew404JSONResponse();
        }
        return checkPermission(excellentableDB, permissionType);
    }

    /**
     * To be removed in favor of hasPermissionOnExcellentable(ExcellentableDB excellentableDB, String permissionType)
     * @param excellentableDBs result of - select * from db where id = excId
     * @param permissionType 'View' or 'Edit'
     * @return Json response based on permissions
     */
    @Override
    public JsonObject hasPermissionOnExcellentable(ExcellentableDB[] excellentableDBs, String permissionType) {

        // TODO: To remove the length part in this validation once we change the excellentableDB argument to don't be an array
        // Added method below without the length part will remove when all methods are replaced using below method
        if (excellentableDBs == null || excellentableDBs.length != 1) {
            return createNew404JSONResponse();
        }

        // Using the correct item from the argument array - This will be fixed later for better clarification
        ExcellentableDB excellentableDB = excellentableDBs[0];
        return checkPermission(excellentableDB, permissionType);

    }

    /**
     * Checks whether loggedInUser has permission on Excellentable
     *
     * @param excellentableDB
     * @param permissionType  i.e Edit/View
     * @return JSONObject
     */
    @Override
    public JsonObject hasPermissionOnExcellentable(ExcellentableDB excellentableDB, String permissionType) {
        if (excellentableDB == null) {
            return createNew404JSONResponse();
        }
        return checkPermission(excellentableDB, permissionType);

    }

    /**
     * Returns permission on Excellentable of logged in user
     *
     * @param excellentableDB Excellentable Data Tuple
     * @return JSONObject
     */
    @Override
    public JsonObject getPermissionOnExcellentable(ExcellentableDB excellentableDB) {
        if (excellentableDB == null) {
            return createNew404JSONResponse();
        }
        JsonObject response = checkPermission(excellentableDB, "Edit");
        if (response.get("status").getAsBoolean()) {
            response.addProperty("permissionType", 2);
            return response;
        }
        response = checkPermission(excellentableDB, "View");
        if (response.get("status").getAsBoolean())
            response.addProperty("permissionType", 1);
        else
            response.addProperty("permissionType", 0);
        return response;
    }

    public JsonObject checkPermission(ExcellentableDB excellentableDB, String permissionType) {
        Permission requestedPermission = getPermissionTypeOverridable(permissionType);
        ConfluenceUser user = getCurrentUserBeingUsed();

        ContentEntityObject page = getPage_WhereExcellentableInstanceLivesIn(excellentableDB);
        Space space = getSpace_WhereExcellentableInstanceLivesIn(excellentableDB);

        boolean hasPermission = false;
        if (page != null) {
            hasPermission = userHasRequestedPermissionOnPage(user, requestedPermission, page);
        } else {
            hasPermission = userHasRequestedPermissionOnSpace(user, requestedPermission, space);
        }

        return hasPermission ? createNewSuccessJSONResponse() : createNew401JSONResponse();
    }

    /**
     * Checks whether loggedInUser has permission on specified contentEntity
     *
     * @param contentEntityId i.e PageId/TemplateId
     * @param contentType     i.e Page/Template
     * @param spaceKey        i.e space Key
     * @param permissionType  i.e Edit/View
     * @return JSONObject
     */
    @Override
    public JsonObject hasPermissionOnContentEntity(Long contentEntityId, String contentType, String spaceKey, String permissionType) {

        boolean hasPermission = false;

        Permission requestedPermission = getPermissionTypeOverridable(permissionType);
        ConfluenceUser user = getCurrentUserBeingUsed();

        if (idExists(contentEntityId) && ("template").equalsIgnoreCase(contentType)) {
            PageTemplate page = pageTemplateManager.getPageTemplate(contentEntityId);
            hasPermission = permissionManager.hasPermission(user, requestedPermission, page);
        } else if (idExists(contentEntityId)) {
            ContentEntityObject ceo = contentEntityManagerInternal.getById(contentEntityId);
            hasPermission = permissionManager.hasPermission(user, requestedPermission, ceo);
        } else if (StringUtils.isNotEmpty(spaceKey)) {
            Space space = spaceManager.getSpace(spaceKey);
            hasPermission = userHasRequestedPermissionOnSpace(user, requestedPermission, space);
        } else
            return createNew400JSONResponse();

        return hasPermission ? createNewSuccessJSONResponse() : createNew401JSONResponse();
    }


    private ContentEntityObject getPage_WhereExcellentableInstanceLivesIn(ExcellentableDB excellentableDB) {
        return contentEntityManagerInternal.getById(excellentableDB.getContentEntityId());
    }

    private Space getSpace_WhereExcellentableInstanceLivesIn(ExcellentableDB excellentableDB) {
        return spaceManager.getSpace(excellentableDB.getSpaceKey());
    }

    private boolean userHasRequestedPermissionOnPage(ConfluenceUser user, Permission requestedPermission, ContentEntityObject page) {
        return permissionManager.hasPermission(user, requestedPermission, page);
    }

    private boolean userHasRequestedPermissionOnSpace(ConfluenceUser user, Permission requestedPermission, Space space) {
        if (requestedPermission == Permission.VIEW)
            return permissionManager.hasPermission(user, requestedPermission, space);
        else if (requestedPermission == Permission.EDIT)
            return permissionManager.hasCreatePermission(user, space, Page.class);
        else
            return false;
    }

    private boolean idExists(Long id) {
        return (id > 0L);
    }

    private JsonObject createNew404JSONResponse() {
        JsonObject responseJSON = new JsonObject();
        responseJSON.addProperty("status", false);
        responseJSON.addProperty("status-code", "404");
        JsonObject errorMessageJSON = new JsonObject();
        errorMessageJSON.addProperty("errorMessage", "Specified Excellentable does not exist.");
        responseJSON.add("message", errorMessageJSON);
        return responseJSON;
    }

    private JsonObject createNew401JSONResponse() {
        JsonObject responseJSON = new JsonObject();
        responseJSON.addProperty("status", false);
        responseJSON.addProperty("status-code", "401");
        JsonObject errorMessageJSON = new JsonObject();
        errorMessageJSON.addProperty("errorMessage", "User is not Authorized to access this resource");
        responseJSON.add("message", errorMessageJSON);
        return responseJSON;
    }

    private JsonObject createNew400JSONResponse() {
        JsonObject responseJSON = new JsonObject();
        responseJSON.addProperty("status", false);
        responseJSON.addProperty("status-code", "400");
        JsonObject errorMessageJSON = new JsonObject();
        errorMessageJSON.addProperty("errorMessage", "Please provide all mandatory fields.  1.ContentEntityId  2.SpaceKey");
        responseJSON.add("message", errorMessageJSON);
        return responseJSON;
    }

    private JsonObject createNewSuccessJSONResponse() {
        JsonObject temp = new JsonObject();
        temp.addProperty("status", true);
        return temp;
    }


    /* Temp functions for test overrides */
    protected ConfluenceUser getCurrentUserBeingUsed() {
        return AuthenticatedUserThreadLocal.get();
    }

    protected Permission getPermissionTypeOverridable(String permissionType) {
        if (("EDIT").equalsIgnoreCase(permissionType)) {
            return Permission.EDIT;
        } else if (("VIEW").equalsIgnoreCase(permissionType)) {
            return Permission.VIEW;
        } else
            return null;
    }

    public static Permission getPermissionTypeStatic(String permissionType) {
        if (("EDIT").equalsIgnoreCase(permissionType)) {
            return Permission.EDIT;
        } else if (("VIEW").equalsIgnoreCase(permissionType)) {
            return Permission.VIEW;
        } else
            return null;
    }
}
