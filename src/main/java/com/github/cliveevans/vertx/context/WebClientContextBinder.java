package com.github.cliveevans.vertx.context;

import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Inject;

public class WebClientContextBinder extends AbstractBinder implements Factory<WebClient> {

    private final Vertx vertx;

    @Inject
    public WebClientContextBinder(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public WebClient provide() {
        return WebClient.create(vertx);
    }

    @Override
    public void dispose(WebClient webClient) {
        webClient.close();
    }

    @Override
    protected void configure() {
        bindFactory(WebClientContextBinder.class).to(WebClient.class);
    }
}
