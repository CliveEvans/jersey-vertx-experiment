package com.github.cliveevans.vertx.resources;

import com.github.cliveevans.vertx.RemoteRequestHandler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.client.WebClient;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("/api/remote")
public class HelloRemote {

    private static final Logger logger = LoggerFactory.getLogger(HelloRemote.class);

    @Context
    private WebClient webClient;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/users")
    public void users(@Suspended AsyncResponse asyncResponse, @QueryParam("page") @DefaultValue("1") int pageNum) {
        webClient.get("reqres.in", "/api/users?page=" + pageNum)
                .send(new RemoteRequestHandler(asyncResponse));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/users/{userId}")
    public void oneUser(@Suspended AsyncResponse asyncResponse, @PathParam("userId") String userId) {
        webClient.get("reqres.in", "/api/users/" + userId)
                .send(new RemoteRequestHandler(asyncResponse));
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/users/{userId}")
    public void updateUser(@Suspended AsyncResponse asyncResponse, @PathParam("userId") String userId, JsonObject user) {
        logger.info("Attempting to put: {0}", user);
        webClient.putAbs("https://reqres.in/api/users/" + userId)
                .sendJsonObject(user, new RemoteRequestHandler(asyncResponse));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/users/")
    public void createUser(@Suspended AsyncResponse asyncResponse, JsonObject user) {
        logger.info("Attempting to post: {0}", user);
        webClient.postAbs("https://reqres.in/api/users/")
                .sendJsonObject(user, new RemoteRequestHandler(asyncResponse));
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
