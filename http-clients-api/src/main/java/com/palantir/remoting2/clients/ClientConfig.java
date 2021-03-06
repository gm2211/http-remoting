/*
 * Copyright 2016 Palantir Technologies, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.palantir.remoting2.clients;

import com.palantir.remoting2.config.service.Duration;
import com.palantir.remoting2.config.service.ProxyConfiguration;
import com.palantir.remoting2.config.service.ServiceConfiguration;
import com.palantir.remoting2.config.ssl.SslSocketFactories;
import com.palantir.remoting2.config.ssl.TrustContext;
import java.util.Optional;
import org.immutables.value.Value;

/** Implementation-independent configuration options for HTTP-based dynamic proxies. */
@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PACKAGE)
@SuppressWarnings("checkstyle:designforextension")
public abstract class ClientConfig {

    private static final Duration CONNECT_TIMEOUT = Duration.seconds(10);
    private static final Duration READ_TIMEOUT = Duration.minutes(10);
    private static final Duration WRITE_TIMEOUT = Duration.minutes(10);
    private static final int MAX_NUM_RETRIES = 0;

    @Value.Parameter
    public abstract Optional<TrustContext> trustContext();

    @Value.Default
    public Duration connectTimeout() {
        return CONNECT_TIMEOUT;
    }

    @Value.Default
    public Duration readTimeout() {
        return READ_TIMEOUT;
    }

    @Value.Default
    public Duration writeTimeout() {
        return WRITE_TIMEOUT;
    }

    @Value.Parameter
    public abstract Optional<ProxyConfiguration> proxy();

    @Value.Default
    public Integer maxNumRetries() {
        return MAX_NUM_RETRIES;
    }

    @Value.Default
    public boolean enableGcmCipherSuites() {
        return false;
    }

    public static ClientConfig fromServiceConfig(ServiceConfiguration serviceConfig) {
        ClientConfig.Builder clientConfig = builder();

        // ssl
        if (serviceConfig.security().isPresent()) {
            clientConfig.trustContext(SslSocketFactories.createTrustContext(serviceConfig.security().get()));
        }

        // timeouts & proxy
        clientConfig.connectTimeout(serviceConfig.connectTimeout().orElse(CONNECT_TIMEOUT));
        clientConfig.readTimeout(serviceConfig.readTimeout().orElse(READ_TIMEOUT));
        clientConfig.writeTimeout(serviceConfig.writeTimeout().orElse(WRITE_TIMEOUT));
        clientConfig.proxy(serviceConfig.proxyConfiguration());
        clientConfig.enableGcmCipherSuites(serviceConfig.enableGcmCipherSuites().orElse(false));

        return clientConfig.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder extends ImmutableClientConfig.Builder {}
}
