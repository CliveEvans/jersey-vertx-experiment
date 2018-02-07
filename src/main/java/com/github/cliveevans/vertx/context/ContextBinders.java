package com.github.cliveevans.vertx.context;

import com.github.cliveevans.vertx.WebResponse;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class ContextBinders extends AbstractBinder {
    @Override
    protected void configure() {
        bind(WebResponse.class).to(WebResponse.class);
    }
}
