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
import java.util.function.Function;

public class WebResponse {

    private static Handler<AsyncResult<HttpResponse<Buffer>>> defaultError(AsyncResponse async) {
        return result -> async.resume(Response.serverError().entity(result.cause()).build());
    }

    private static <T> Handler<HttpResponse<Buffer>> passThrough(AsyncResponse async, Function<HttpResponse<Buffer>, T> transform) {
        return result -> async.resume(Response.status(result.statusCode()).entity(transform.apply(result)).build());
    }

    private static Handler<HttpResponse<Buffer>> defaultBadResponseHandler(AsyncResponse async) {
        return result -> {
            JsonObject responseObject = remoteError(result);
            async.resume(Response.status(Response.Status.BAD_GATEWAY).entity(responseObject).build());
        };
    }

    private static JsonObject remoteError(HttpResponse<Buffer> result) {
        return new JsonObject()
                .put("remote_error", result.bodyAsString())
                .put("remote_status", result.statusCode());
    }

    public WebResponseHandler handler(Handler<HttpResponse<Buffer>> success, Handler<HttpResponse<Buffer>> badResponse, Handler<AsyncResult<HttpResponse<Buffer>>> error) {
        return new WebResponseHandler(success, badResponse, error);
    }

    public WebResponseHandler handler(Handler<HttpResponse<Buffer>> success, AsyncResponse async) {
        return handler(success, defaultBadResponseHandler(async), defaultError(async));
    }

    public WebResponseHandler handler(AsyncResponse async) {
        return handler(passThrough(async, HttpResponse::bodyAsJsonObject), defaultBadResponseHandler(async), defaultError(async));
    }

    public WebResponseHandler handler(AsyncResponse async, Function<HttpResponse<Buffer>, Object> entityTransform) {
        return handler(passThrough(async, entityTransform), defaultBadResponseHandler(async), defaultError(async));
    }

    public static class WebResponseHandler implements Handler<AsyncResult<HttpResponse<Buffer>>> {
    
        private static final Logger logger = LoggerFactory.getLogger(WebResponseHandler.class);
    
    
        private final Handler<HttpResponse<Buffer>> success;
    
        private final Handler<HttpResponse<Buffer>> badResponse;
    
        private final Handler<AsyncResult<HttpResponse<Buffer>>> error;
    
    
        private WebResponseHandler(Handler<HttpResponse<Buffer>> success, Handler<HttpResponse<Buffer>> badResponse, Handler<AsyncResult<HttpResponse<Buffer>>> error) {
            this.success = success;
            this.badResponse = badResponse;
            this.error = error;
        }
    
    
        @Override
        public void handle(AsyncResult<HttpResponse<Buffer>> event) {
            if (event.succeeded()) {
                HttpResponse<Buffer> result = event.result();
                if (result.statusCode() < 300) {
                    logger.info("It worked");
                    logger.info("body: {0}", result.bodyAsString());
                    success.handle(result);
                } else {
                    logger.warn("Remote request problem. Status received: {0}", result.statusCode());
                    badResponse.handle(result);
                }
            } else {
                logger.warn("Something bad happened: {}", event.cause());
                error.handle(event);
            }
        }
    
    }
}
