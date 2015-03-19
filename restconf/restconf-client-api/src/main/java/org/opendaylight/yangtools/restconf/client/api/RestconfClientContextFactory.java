/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client.api;

import java.net.URL;
import org.opendaylight.yangtools.restconf.client.api.auth.AuthenticationHolder;
import org.opendaylight.yangtools.yang.data.impl.codec.BindingIndependentMappingService;
import org.opendaylight.yangtools.yang.model.api.SchemaContextHolder;

/**
 * An interface for acquiring #{@link RestconfClientContext} instances.
 */
public interface RestconfClientContextFactory {
	/**
	 * Get a #{@link RestconfClientContext} attached to the server pointed to by an URL.
	 * @param baseUrl URL pointer to the backend server
	 * @return RestconfClientContext attached to the backend server.
	 * @throws UnsupportedProtocolException if the factory cannot handle the protocol specified in the URL.
	 */
    @Deprecated
RestconfClientContext getRestconfClientContext(URL baseUrl,BindingIndependentMappingService mappingService,SchemaContextHolder holder) throws UnsupportedProtocolException;
    void setAuthentication(AuthenticationHolder authenticationHolder);

    RestconfClientContext getRestconfClientContext(URL baseUrl, SchemaContextHolder schemaContextHolder)
            throws UnsupportedProtocolException;

}
