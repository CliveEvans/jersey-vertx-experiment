package com.github.cliveevans.vertx.context;

import com.beust.jcommander.Parameter;
import io.vertx.core.net.ProxyOptions;

public class ProxyParameters {

    private static final String DEFAULT_PROXY_HOST = "localhost";
    private static final int DEFAULT_PROXY_PORT = 3128;

    @Parameter(names = {"--proxy-user"})
    private String proxyUser;

    @Parameter(names = {"--proxy-pass"}, description = "Proxy password", password = true)
    private String proxyPass;

    @Parameter(names = {"--proxy-host"})
    private String proxyHost = DEFAULT_PROXY_HOST;

    @Parameter(names = {"--proxy-port"})
    private int proxyPort = DEFAULT_PROXY_PORT;

    @Parameter(names = {"--proxy"}, description = "Use a proxy")
    private boolean useProxy = false;

    public ProxyOptions proxyOptions() {
        if(useProxy) {
            return new ProxyOptions()
                    .setHost(proxyHost)
                    .setPort(proxyPort)
                    .setUsername(proxyUser)
                    .setPassword(proxyPass);
        } else {
            return null;
        }
    }

}
