package com.github.cliveevans.vertx.context;

import io.vertx.core.Vertx;
import io.vertx.core.net.ProxyOptions;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Inject;

public class WebClientContextBinder extends AbstractBinder {

    private final Vertx vertx;
    private final WebClientOptions options;

    public WebClientContextBinder(Vertx vertx, ProxyParameters proxyParameters) {
        this.vertx = vertx;
        this.options = new WebClientOptions().setProxyOptions(proxyParameters.proxyOptions());
    }

    @Override
    protected void configure() {
        bindFactory(new WebClientFactory()).to(WebClient.class);
    }

    private class WebClientFactory implements Factory<WebClient> {
        @Override
        public WebClient provide() {
            return WebClient.create(vertx, options);
        }

        @Override
        public void dispose(WebClient webClient) {
            webClient.close();
        }
    }
}
