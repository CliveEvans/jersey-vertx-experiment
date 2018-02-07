package com.github.cliveevans.vertx.resources;

import com.englishtown.promises.Deferred;
import com.englishtown.promises.When;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("/v1/local")
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

    @Path("/when")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public void doEventuallyWithPromises(@Suspended AsyncResponse response, @Context When when) {
        Deferred<Object> defer = when.defer();

        defer.getPromise()
                .then(r -> {
                    response.resume(r);
                    return null;
                })
                .otherwise(e -> {
                    response.resume(new WebApplicationException(e));
                    return null;
                });

        defer.resolve("Eventual answer");
    }
}
