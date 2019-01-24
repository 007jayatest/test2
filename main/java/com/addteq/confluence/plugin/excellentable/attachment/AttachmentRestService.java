package com.addteq.confluence.plugin.excellentable.attachment;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.google.gson.JsonObject;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Vikash Kumar
 * @Description This service is used to parse a xls/xlsx attachment into json response supported by SpreadJS.
 */
@AnonymousAllowed
@Produces({MediaType.APPLICATION_JSON})
@Consumes({MediaType.APPLICATION_JSON})
@Path("/attachment")

@Component
public class AttachmentRestService {

    private final AttachmentService attachmentService;
    static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AttachmentRestService.class);

    @Autowired
    public AttachmentRestService(AttachmentService attachmentService) {

        this.attachmentService = attachmentService;
    }
    /**
     * This method returns Spread JSON data for the given attachment id of a Confluence page.
     *
     * @param attachmentId The id of the attachment to a page.
     * @return Return error object or Spread JSON response.
     */
    @GET
    @Path("/render/{attachmentId}")
    public Response renderAttachment(@PathParam("attachmentId") Long attachmentId) {

        JsonObject attachmentAsJson = attachmentService.parseAttachment(attachmentId);

        // Permission check and return response if false else it indicates no problem in
        // authorization and return json response needed.
        if (attachmentAsJson.has("status") && attachmentAsJson.get("status").equals(false)) {
            int statusCode = attachmentAsJson.get("status-code").getAsInt();
            String statusMsg = attachmentAsJson.get("message").getAsString();
            return Response.status(Response.Status.fromStatusCode(statusCode)).entity(statusMsg).build();
        }

        return Response.status(Response.Status.OK).entity(attachmentAsJson.toString())
                .build();
    }
}
