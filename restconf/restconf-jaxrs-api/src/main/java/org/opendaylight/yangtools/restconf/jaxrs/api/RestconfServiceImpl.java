/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.jaxrs.api;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import java.net.URI;
import com.sun.jersey.api.client.Client;
import java.net.URL;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import org.opendaylight.controller.sal.restconf.impl.StructuredData;
import org.opendaylight.controller.sal.rest.api.RestconfService;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;

public class RestconfServiceImpl implements RestconfService {

    private final WebResource service;
    private final URL url;

    public RestconfServiceImpl(URL url){
        ClientConfig config = new DefaultClientConfig();
        Client client  = Client.create(config);
        this.url = url;
        this.service = client.resource(url.toString());
    }

    @Override
    public Object getRoot() {
        return null;
    }

    @Override
    public StructuredData getModules() {
        return null;
    }

    @Override
    public StructuredData invokeRpc(@PathParam("identifier") String identifier, CompositeNode payload) {
        return null;
    }

    @Override
    public StructuredData invokeRpc(@PathParam("identifier") String identifier) {
        return null;
    }

    @Override
    public StructuredData readConfigurationData(@PathParam("identifier") String identifier) {
        return null;
    }

    @Override
    public StructuredData readOperationalData(@PathParam("identifier") String identifier) {
        return null;
    }

    @Override
    public Response updateConfigurationData(@PathParam("identifier") String identifier, CompositeNode payload) {
        return null;
    }

    @Override
    public Response createConfigurationData(@PathParam("identifier") String identifier, CompositeNode payload) {
        return null;
    }

    @Override
    public Response createConfigurationData(CompositeNode payload) {
        return null;
    }

    @Override
    public Response deleteConfigurationData(@PathParam("identifier") String identifier) {
        return null;
    }

    @Deprecated
    @Override
    public StructuredData readAllData() {
        return null;
    }

    @Deprecated
    @Override
    public StructuredData readData(@PathParam("identifier") String identifier) {
        return null;
    }

    @Deprecated
    @Override
    public Response createConfigurationDataLegacy(@PathParam("identifier") String identifier, CompositeNode payload) {
        return null;
    }

    @Deprecated
    @Override
    public Response updateConfigurationDataLegacy(@PathParam("identifier") String identifier, CompositeNode payload) {
        return null;
    }

    \

}
