package com.github.cliveevans.vertx;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.client.HttpResponse;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;

public class RemoteRequestHandler implements Handler<AsyncResult<HttpResponse<Buffer>>> {

    private static final Logger logger = LoggerFactory.getLogger(RemoteRequestHandler.class);
    private final Handler<Response> handler;

    public RemoteRequestHandler(Handler<Response> handler) {
        this.handler = handler;
    }

    public RemoteRequestHandler(AsyncResponse async) {
        this(response -> async.resume(response));
    }

    @Override
    public void handle(AsyncResult<HttpResponse<Buffer>> event) {
        if (event.succeeded()) {
            logger.info("It worked, maybe");
            int statusCode = event.result().statusCode();
            if (statusCode < 300) {
                handler.handle(Response.ok(event.result().bodyAsJsonObject()).build());
            } else {
                logger.warn("I lied: status was {}", statusCode);
                JsonObject responseObject = new JsonObject()
                        .put("remote_error", event.result().bodyAsString())
                        .put("remote_status", statusCode);
                handler.handle(Response.status(Response.Status.BAD_GATEWAY).entity(responseObject).build());
            }
        } else {
            logger.warn("Something bad happened: {}", event.cause());
            handler.handle(Response.serverError().entity(event.cause()).build());
        }
    }


}
