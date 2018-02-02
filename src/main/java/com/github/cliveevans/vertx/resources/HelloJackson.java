package com.github.cliveevans.vertx.resources;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("/v1")
public class HelloJackson {

    @Path("/sync")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonObject doGet(@QueryParam("name") @DefaultValue("Jackson") String name) {
        return new JsonObject()
                .put("message", "Hello " + name);
    }

    @Path("/async")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public void doEventually(@Suspended AsyncResponse response, @Context Vertx vertx) {
        vertx.runOnContext(v -> response.resume(doGet("Async")));
    }
}
