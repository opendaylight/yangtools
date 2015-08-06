/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.restconf.client;

import java.net.URL;
import org.opendaylight.yangtools.restconf.client.api.RestconfClientContext;
import org.opendaylight.yangtools.restconf.client.api.RestconfClientContextFactory;
import org.opendaylight.yangtools.restconf.client.api.UnsupportedProtocolException;
import org.opendaylight.yangtools.restconf.client.api.auth.AuthenticationHolder;
import org.opendaylight.yangtools.yang.model.api.SchemaContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestconfClientFactory implements RestconfClientContextFactory {

    private static final Logger logger = LoggerFactory.getLogger(RestconfClientFactory.class);
    private AuthenticationHolder authenticationHolder;



    @Override
    public RestconfClientContext getRestconfClientContext(URL baseUrl, SchemaContextHolder schemaContextHolder) throws UnsupportedProtocolException {
        if (!baseUrl.getProtocol().equals("http")){
            throw new UnsupportedProtocolException("Unsupported protocol "+baseUrl.getProtocol());
        }
        RestconfClientImpl restconfClient = new RestconfClientImpl(baseUrl,schemaContextHolder);
        if (null!=authenticationHolder){
            restconfClient.setAuthenticationHolder(authenticationHolder);
        }
        return restconfClient;
    }

    @Override
    public void setAuthentication(AuthenticationHolder authenticationHolder) {
        this.authenticationHolder = authenticationHolder;
    }
}
