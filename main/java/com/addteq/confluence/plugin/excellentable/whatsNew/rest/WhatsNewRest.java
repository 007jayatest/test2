package com.addteq.confluence.plugin.excellentable.whatsNew.rest;

import com.addteq.confluence.plugin.excellentable.whatsNew.model.WhatsNewModel;
import com.addteq.confluence.plugin.excellentable.whatsNew.service.WhatsNewService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.message.I18nResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Rest API to get and set data related to whats new notification
 */
@Path("/whatsnew")

@Component
public class WhatsNewRest {


    private final I18nResolver i18nResolver;
    private final WhatsNewService whatsNewService;

    @Autowired
    public WhatsNewRest(
            @ComponentImport I18nResolver i18nResolver,
            WhatsNewService whatsNewService) {
        this.i18nResolver = i18nResolver;
        this.whatsNewService = whatsNewService;
    }

    /**
     * Return data of notification
     *
     * @return Response
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{location}")
    public Response returnData(@PathParam("location") String location) {
        return Response.ok(whatsNewService.getData(location)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveData(final WhatsNewModel whatsNewModel) {
        return Response.ok(whatsNewService.setData(whatsNewModel)).build();
    }
}
