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
import org.opendaylight.yangtools.yang.model.api.SchemaContextHolder;

/**
 * An interface for acquiring #{@link RestconfClientContext} instances.
 */
public interface RestconfClientContextFactory {

    RestconfClientContext getRestconfClientContext(URL baseUrl, SchemaContextHolder schemaContextHolder)
            throws UnsupportedProtocolException;

    void setAuthentication(AuthenticationHolder authenticationHolder);

}
