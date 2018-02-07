package com.github.cliveevans.vertx.resources;

import com.englishtown.promises.Deferred;
import com.englishtown.promises.When;
import com.github.cliveevans.vertx.WebResponse;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import java.util.List;
import java.util.stream.Collectors;


@Path("/api/remote")
public class HelloRemote {

    private static final Logger logger = LoggerFactory.getLogger(HelloRemote.class);

    @Context
    private WebClient webClient;
    @Context
    private WebResponse webResponse;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/users")
    public void users(@Suspended AsyncResponse asyncResponse, @QueryParam("page") @DefaultValue("1") int pageNum) {
        webClient.get("reqres.in", "/api/users?page=" + pageNum)
                .send(webResponse.handler(asyncResponse));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/users/{userId}")
    public void oneUser(@Suspended AsyncResponse asyncResponse, @PathParam("userId") String userId) {
        webClient.get("reqres.in", "/api/users/" + userId)
                .send(webResponse.handler(asyncResponse));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path(("/users/query"))
    public void users(@Suspended AsyncResponse asyncResponse, @QueryParam("id") List<String> userIds, @Context When when) {

        when.all(userIds.parallelStream().map(id -> {
            Deferred<JsonObject> d = when.defer();
            webClient.get("reqres.in", "/api/users/" + id)
                    .send(webResponse.handler(r -> d.resolve(r.bodyAsJsonObject()), r -> d.reject(new WebApplicationException("Remote service rejected request", r.statusCode())), e -> d.reject(e.cause())));
            return d.getPromise();
        }).collect(Collectors.toList()))
                .then(str -> {
                    logger.info("Datas: {0}", str);
                    asyncResponse.resume(str);
                    return null;
                })
                .otherwise(t -> {
                    asyncResponse.resume(new WebApplicationException(t));
                    return null;
                });
    }

    @GET
    @Produces("image/jpg")
    @Path("/users/{userId}/avatar.jpg")
    public void getAvatar(@Suspended AsyncResponse asyncResponse, @PathParam("userId") String userId) {
        // chain requests
        webClient.get("reqres.in", "/api/users/" + userId)
                .send(webResponse.handler(thenFetchAvatar(asyncResponse), asyncResponse));
    }

    private Handler<HttpResponse<Buffer>> thenFetchAvatar(AsyncResponse asyncResponse) {
        return resp -> {
            JsonObject entity = resp.bodyAsJsonObject();
            webClient.getAbs(entity.getJsonObject("data").getString("avatar"))
                    .send(webResponse.handler(asyncResponse, r -> r.bodyAsBuffer().getBytes()));
        };
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/users/{userId}")
    public void updateUser(@Suspended AsyncResponse asyncResponse, @PathParam("userId") String userId, JsonObject user) {
        logger.info("Attempting to put: {0}", user);
        webClient.putAbs("https://reqres.in/api/users/" + userId)
                .sendJsonObject(user, webResponse.handler(asyncResponse));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/users/")
    public void createUser(@Suspended AsyncResponse asyncResponse, JsonObject user) {
        logger.info("Attempting to post: {0}", user);
        webClient.postAbs("https://reqres.in/api/users/")
                .sendJsonObject(user, webResponse.handler(asyncResponse));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/broken")
    public void badCall(@Suspended AsyncResponse asyncResponse) {
        webClient.get("reqres.in", "/apias/users")
                .send(webResponse.handler(asyncResponse));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/fucked")
    public void badRemote(@Suspended AsyncResponse asyncResponse) {

        webClient.get("127.0.0.0", "/apias/users")
                .send(webResponse.handler(asyncResponse));
    }

}
