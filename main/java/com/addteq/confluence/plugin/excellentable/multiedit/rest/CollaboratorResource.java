package com.addteq.confluence.plugin.excellentable.multiedit.rest;

import com.addteq.confluence.plugin.excellentable.multiedit.ao.CollaboratorDB;
import com.addteq.confluence.plugin.excellentable.multiedit.dao.CollaboratorService;
import com.addteq.confluence.plugin.excellentable.multiedit.model.CollaboratorModel;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.confluence.user.actions.ProfilePictureInfo;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.user.User;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vikash Kumar
 * @author saurabh.gupta
 * A rest resource to provide all operation related to collaborators editing
 * an Excellentable.
 */
@AnonymousAllowed
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
@Path("/multiedit")

@Component
public class CollaboratorResource {

    private final CollaboratorService collaboratorService;
    private final UserAccessor userAccessor;
    private final I18nResolver i18nResolver;

    @Autowired
    public CollaboratorResource(
            @ComponentImport UserAccessor userAccessor,
            @ComponentImport I18nResolver i18nResolver,
            CollaboratorService collaboratorService) {

        this.collaboratorService = collaboratorService;
        this.userAccessor = userAccessor;
        this.i18nResolver = i18nResolver;
    }

    /**
     * Fetches a collaborator for given excId and userKey.
     * @param excId Id of the Excellentable.
     * @param userKey UserKey of the collaborator.
     * @return Fetches the collaborator by userKey.
     */
    @GET
    @Path("/table/{excId}/collaborators/{userKey}")
    public Response getCollaboratorByUserKey(@PathParam("excId") Integer excId,
                                              @PathParam("userKey") String userKey) {

        // Check authorization for given excellentable id and send response immediately if not OK
        JsonObject authorization = collaboratorService.authorizeLoggedInUser(excId);
        if (!authorization.get("status").getAsBoolean()) {
            int statusCode = authorization.get("status-code").getAsInt();
            String statusMsg = authorization.get("message").toString();
            return Response.status(Response.Status.fromStatusCode(statusCode)).entity(statusMsg).build();
        }
        // Authorization looks good. Go ahead and execute the business logic.
        CollaboratorDB collaboratorDB = collaboratorService.getCollaboratorByUserKey(excId, userKey);
        if (collaboratorDB == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.status(Response.Status.OK)
                .entity(makeModel(collaboratorDB))
                .build();
    }
    /**
     * Fetches collaborators for given excId
     * @param excId  Id of the Excellentable.
     * @return Fetches collaborators for given excId
     */
    @GET
    @Path("/table/{excId}/collaborators")
    public Response getCollaborators(@PathParam("excId") Integer excId) {

        // Check authorization for given excellentable id and send response immediately if not OK
        JsonObject authorization = collaboratorService.authorizeLoggedInUser(excId);
        if (!authorization.get("status").getAsBoolean()) {
            int statusCode = authorization.get("status-code").getAsInt();
            String statusMsg = authorization.get("message").toString();
            return Response.status(Response.Status.fromStatusCode(statusCode)).entity(statusMsg).build();
        }

        // Authorization looks good. Go ahead and execute the business logic.
        int getUserUpdatedWithin = Integer.parseInt(i18nResolver.getText("com.addteq.confluence.plugin.excellentable.multiedit.collaborators.retrieve.range"));
        CollaboratorDB collaboratorDB[] = collaboratorService.getCollaborators(excId, getUserUpdatedWithin);
        return Response.status(Response.Status.OK)
                .entity(makeModels(collaboratorDB))
                .build();
    }

    /**
     * Add a collaborator to an Excellentable.
     * @param excId Id of the Excellentable
     * @param userKey UserKey of the collaborator.
     * @param versionNumber Version number the user is trying to edit.
     * @return Returns 201 on successful addition of collaborator.
     */
    @POST
    @Path("/table/{excId}/collaborators/{userKey}/versions/{versionNumber}")
    public Response addCollaborator(@PathParam("excId") Integer excId,
                                    @PathParam("userKey") String userKey,
                                    @PathParam("versionNumber") Integer versionNumber) {

        // Check authorization for given excellentable id and send response immediately if not OK
        JsonObject authorization = collaboratorService.authorizeLoggedInUser(excId);
        if (!authorization.get("status").getAsBoolean()) {
            int statusCode = authorization.get("status-code").getAsInt();
            String statusMsg = authorization.get("message").toString();
            return Response.status(Response.Status.fromStatusCode(statusCode)).entity(statusMsg).build();
        }

        // Authorization looks good. Go ahead and execute the business logic.
        collaboratorService.addUniqueCollaborator(excId, userKey, versionNumber);
        return Response.status(Response.Status.CREATED) // 201
                .build();
    }

    /**
     * Update collaborator of an Excellentable with the last seen time. In this case version number is very much
     * important to pass into the updateCollaborator method so that if it does not exist then system will add a new
     * collaborator.
     *
     * @param excId Id of the Excellentable.
     * @param userKey UserKey of the collaborator.
     * @param versionNumber Version number.
     * @return NO_CONTENT (204) status code if the collaborator already exists else it returns CREATED (201) status
     * code.
     */
    @PUT
    @Path("/table/{excId}/collaborators/{userKey}/versions/{versionNumber}")
    public Response updateCollaborator(@PathParam("excId") Integer excId,
                                       @PathParam("userKey") String userKey,
                                       @PathParam("versionNumber") Integer versionNumber) {
        // Check authorization for given excellentable id and send response immediately if not OK
        JsonObject authorization = collaboratorService.authorizeLoggedInUser(excId);
        if (!authorization.get("status").getAsBoolean()) {
            int statusCode = authorization.get("status-code").getAsInt();
            String statusMsg = authorization.get("message").toString();
            return Response.status(Response.Status.fromStatusCode(statusCode)).entity(statusMsg).build();
        }
        // Authorization looks good. Go ahead and execute the business logic.

        int getUserUpdatedWithin = Integer.parseInt(i18nResolver.getText("com.addteq.confluence.plugin.excellentable.multiedit.collaborators.retrieve.range"));
        int id = collaboratorService.updateOrCreateCollaborator(excId, userKey, versionNumber);
        CollaboratorDB[] collaboratorDBS = collaboratorService.getCollaborators(excId, getUserUpdatedWithin);
        Status statusCode;
        switch (id) {
            case 0:
                statusCode = Status.CREATED; // 201 Created a resource
                break;
            case 1:
                statusCode = Status.CREATED; // 201 Accepted (Updated a resource)
                break;
            default:
                statusCode = Status.NOT_FOUND; // 404 Resource not found, in case of any runtime error
                break;
        }

        return Response.status(statusCode)
                .entity(makeModels(collaboratorDBS))
                .build();
    }
    /**
     * Delete the collaborator for the given excId.
     * @param excId Id of the Excellentable.
     * @param userKey UserKey of the collaborator.
     * @return returns different status code based on the number of records deleted.
     */
    @DELETE
    @Path("/table/{excId}/collaborators/{userKey}")
    public Response deleteCollaborator(@PathParam("excId") Integer excId,
                                       @PathParam("userKey") String userKey) {

        // Check authorization for given excellentable id and send response immediately if not OK
        JsonObject authorization = collaboratorService.authorizeLoggedInUser(excId);
        if (!authorization.get("status").getAsBoolean()) {
            int statusCode = authorization.get("status-code").getAsInt();
            String statusMsg = authorization.get("message").toString();
            return Response.status(Response.Status.fromStatusCode(statusCode)).entity(statusMsg).build();
        }
        // Authorization looks good. Go ahead and execute the business logic.

        Status statusCode;
        int deletedRecords = collaboratorService.deleteCollaborator(excId, userKey);
        if (deletedRecords == 0) {
            statusCode = Status.NO_CONTENT;
        } else {
            statusCode = Status.OK;
        }

        return Response.status(statusCode).build();
    }
    /**
     * Convert collaboratorDB into collaboratedModel.
     * @param collaboratorDB CollaboratorDB object to be transformed into CollaboratorModel
     * @return returns CollaboratorModel objects.
     */
    private CollaboratorModel makeModel(CollaboratorDB collaboratorDB) {

        CollaboratorModel collaboratorModel = new CollaboratorModel();
        collaboratorModel.setID(collaboratorDB.getID());
        collaboratorModel.setUserKey(collaboratorDB.getUserKey());
        collaboratorModel.setAvatar(getUserAvatar(collaboratorDB.getUserKey()));
        collaboratorModel.setVersionNumber(collaboratorDB.getVersion());
        collaboratorModel.setUserName(getUserName(collaboratorDB.getUserKey()));

        return collaboratorModel;
    }
    /**
     * Returns list of collaboratorModel bojects.
     * @param collaboratorDB Array of Objects to be transformed into collaboraorModel.
     * @return List of collaboratorModel.
     */
    private List<CollaboratorModel> makeModels(CollaboratorDB collaboratorDB[]) {

        List<CollaboratorModel> collaboratorList = new ArrayList<>();
        for (CollaboratorDB collaboratorDBTemp : collaboratorDB) {
            collaboratorList.add(makeModel(collaboratorDBTemp));
        }

        return collaboratorList;
    }
    /**
     * Returns URI of User picture URI reference.
     * @param userKey UserKey of the user
     * @return String URI path of user profile picture.
     */
    private String getUserAvatar(String userKey) {

        User user = userAccessor.getUserByKey(new UserKey(userKey));
        ProfilePictureInfo pic = userAccessor.getUserProfilePicture(user);
        return pic.getUriReference();
    }

    private String getUserName(String userKey) {

        User user = userAccessor.getUserByKey(new UserKey(userKey));
        return user.getName();
    }
}
