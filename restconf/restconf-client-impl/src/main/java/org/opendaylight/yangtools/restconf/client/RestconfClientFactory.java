/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
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
import org.opendaylight.yangtools.yang.data.impl.codec.BindingIndependentMappingService;
import org.opendaylight.yangtools.yang.model.api.SchemaContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestconfClientFactory implements RestconfClientContextFactory {

    private static final Logger logger = LoggerFactory.getLogger(RestconfClientFactory.class);

    @Override
    public RestconfClientContext getRestconfClientContext(URL baseUrl, BindingIndependentMappingService mappingService, SchemaContextHolder schemaContextHolder, AuthenticationHolder authHolder) throws UnsupportedProtocolException {
        if (!baseUrl.getProtocol().equals("http")){
            throw new UnsupportedProtocolException("Unsupported protocol "+baseUrl.getProtocol());
        }
        return new RestconfClientImpl(baseUrl,mappingService,schemaContextHolder,authHolder);
    }
}
