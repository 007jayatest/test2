package com.addteq.confluence.plugin.excellentable.multiedit.rest;

import com.addteq.confluence.plugin.excellentable.multiedit.dao.LiveEditRegisterService;
import com.addteq.confluence.plugin.excellentable.multiedit.dao.MultieditSettingsDao;
import com.addteq.confluence.plugin.excellentable.multiedit.model.MultieditConnectionInfo;
import com.addteq.confluence.plugin.excellentable.multiedit.model.MultieditSettingsResponse;
import com.addteq.confluence.plugin.excellentable.multiedit.service.LambdaRestClient;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.nio.BufferUnderflowException;


/**
 * REST web services to provide multiple operation in order to enable disable Collaborative editing.
 * 
 * @author akanksha
 * @author Vikash Kumar <vikash.kumar@addteq.com>
 */
@Path("/multieditconfig")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)

@Component
public class MultieditSettingsResource {

    private final MultieditSettingsDao multieditSettingsDao;
    private final LambdaRestClient lambdaRestClient;
    private final LiveEditRegisterService liveEditRegisterService;
    private final int DISABLE = 0, ENABLE = 1;

    @Autowired
    public MultieditSettingsResource(
            MultieditSettingsDao multieditSettingsDao,
            LambdaRestClient lambdaRestClient,
            LiveEditRegisterService liveEditRegisterService) {
        
        this.multieditSettingsDao = multieditSettingsDao;
        this.lambdaRestClient = lambdaRestClient;
        this.liveEditRegisterService = liveEditRegisterService;
    }

    /**
     * Get multiedit configuration information
     * @return Firebase instance and connection related information and NO_CONTENT for null value.
     */
    @GET
    @Path("/settings")
    public Response multieditSettings() {
        MultieditConnectionInfo multieditConnectionInfo = multieditSettingsDao.getMultieditSettings();
        if( multieditConnectionInfo != null) {
            MultieditSettingsResponse multieditSettingsResponse = removeExtraParametersFromResponse(multieditConnectionInfo);
            return Response.ok(multieditSettingsResponse).build();
        } else {
            multieditConnectionInfo = new MultieditConnectionInfo();
            multieditConnectionInfo.setHasError(Boolean.TRUE);
            multieditConnectionInfo.setMessage("The Collaborative Editing is not enabled");
            return Response.status(Response.Status.NO_CONTENT).build();
        }
    }

    /**
     * Set multiedit configuration information
     * @param multieditConnectionInfo Collaborative edit permission status
     * @return : Ok on successful operation.
     */
    @POST
    @Path("/settings")
    public Response multieditSettings(final MultieditConnectionInfo multieditConnectionInfo) throws UnirestException {
        Status statusCode = Response.Status.OK;
        MultieditConnectionInfo multieditConnectionInfo1 = new MultieditConnectionInfo();
        switch (multieditConnectionInfo.getStatus()) {
            case DISABLE:
                multieditConnectionInfo1 = lambdaRestClient.disableMultiedit();
                break;
            case ENABLE:
                multieditConnectionInfo1 = lambdaRestClient.enableMultiedit();
                break;
            default:
                multieditConnectionInfo1.setHasError(Boolean.TRUE);
                multieditConnectionInfo1.setMessage("Failed to do the operation");
                multieditConnectionInfo1.setDeveloperMessage("Error occurred in DB operations while enabling Collaborative Editing."
                        + " Status code: "+multieditConnectionInfo.getStatus());
                break;             
        }
        if (multieditConnectionInfo1.getHasError()) {
            statusCode = Response.Status.INTERNAL_SERVER_ERROR;
        }
        MultieditSettingsResponse multieditSettingsResponse = removeExtraParametersFromResponse(multieditConnectionInfo1);
        return Response.status(statusCode).entity(multieditSettingsResponse).build();
    }
    
    /**
     * 
     * @return true or false based on the result of server is reachable or not.
     * @throws UnirestException 
     */
    @GET
    @Path("/settings/testConnection")
    public Response testConnection() throws UnirestException, BufferUnderflowException {
        // EXC-5010 Immediately register the attempt to enable collaborative editing, first thing, regardless of
        // whether the connection is successful or not
        liveEditRegisterService.registerMultiEditAttempt();


        Status statusCode = Response.Status.OK;
        MultieditConnectionInfo multieditConnectionInfo = lambdaRestClient.testLambdaServer();
        if(multieditConnectionInfo.getHasError()) {
            statusCode = Response.Status.INTERNAL_SERVER_ERROR;
        }
        MultieditSettingsResponse multieditSettingsResponse = removeExtraParametersFromResponse(multieditConnectionInfo);

        return Response.status(statusCode).entity(multieditSettingsResponse).build();
    }

    private MultieditSettingsResponse removeExtraParametersFromResponse(MultieditConnectionInfo multieditConnectionInfo){
        return new MultieditSettingsResponse(multieditConnectionInfo.getStatus(),
                multieditConnectionInfo.getHasError(), multieditConnectionInfo.getMessage(),
                multieditConnectionInfo.getDeveloperMessage());
    }
}

   
                

