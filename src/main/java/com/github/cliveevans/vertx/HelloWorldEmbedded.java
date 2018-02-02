package com.github.cliveevans.vertx;

import com.englishtown.vertx.hk2.HK2JerseyBinder;
import com.englishtown.vertx.hk2.HK2VertxBinder;
import com.englishtown.vertx.jersey.JerseyServer;
import com.github.cliveevans.vertx.context.WebClientContextBinder;
import com.github.cliveevans.vertx.resources.HelloJackson;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

public class HelloWorldEmbedded {

    public static void main(String[] args) {

        Vertx vertx = Vertx.vertx();

        vertx.runOnContext(aVoid -> {

            // Set up the jersey configuration
            // The minimum config required is a package to inspect for JAX-RS endpoints
            JsonArray packages = new JsonArray()
                    .add(HelloJackson.class.getPackage().getName());
            vertx.getOrCreateContext().config()
                    .put("jersey", new JsonObject()
                            .put("port", 8080)
                            .put("packages", packages));

            ServiceLocator locator = ServiceLocatorUtilities
                    .bind(new HK2JerseyBinder(), new HK2VertxBinder(vertx), new WebClientContextBinder(vertx));

            JerseyServer server = locator.getService(JerseyServer.class);

            server.start();

        });

    }

}
