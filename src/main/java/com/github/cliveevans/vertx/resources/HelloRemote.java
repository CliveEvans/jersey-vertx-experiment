package com.github.cliveevans.vertx.resources;

import com.github.cliveevans.vertx.RemoteRequestHandler;
import io.vertx.ext.web.client.WebClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("/api/remote")
public class HelloRemote {

    @Context
    private WebClient webClient;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/working")
    public void goodCall(@Suspended AsyncResponse asyncResponse) {

        webClient.get("reqres.in", "/api/users")
                .send(new RemoteRequestHandler(asyncResponse));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/broken")
    public void badCall(@Suspended AsyncResponse asyncResponse) {

        webClient.get("reqres.in", "/apias/users")
                .send(new RemoteRequestHandler(asyncResponse));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/fucked")
    public void badRemote(@Suspended AsyncResponse asyncResponse) {

        webClient.get("not.valid", "/apias/users")
                .send(new RemoteRequestHandler(asyncResponse));
    }


}
