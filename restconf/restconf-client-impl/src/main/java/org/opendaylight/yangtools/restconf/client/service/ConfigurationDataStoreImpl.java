/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client.service;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.net.URI;
import org.opendaylight.yangtools.restconf.client.api.data.ConfigurationDatastore;
import org.opendaylight.yangtools.restconf.client.api.data.RetrievalStrategy;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class ConfigurationDataStoreImpl implements ConfigurationDatastore {


    private URI uri;

    public ConfigurationDataStoreImpl(URI uri){
        this.uri = uri;
    }

    public ListenableFuture<RpcResult<Boolean>> deleteData(InstanceIdentifier<?> path) {
        Client client = Client.create();

        WebResource webResource = client
                .resource(uri.getPath());

        ClientResponse response = webResource.accept("application/json")
                .get(ClientResponse.class);
        return null;
    }

    @Override
    public ListenableFuture<RpcResult<Boolean>> putData(InstanceIdentifier<?> path) {
        return null;
    }

    @Override
    public <T extends DataObject> ListenableFuture<Optional<T>> readData(InstanceIdentifier<T> path) {
        return null;
    }

    @Override
    public <T extends DataObject> ListenableFuture<Optional<T>> readData(InstanceIdentifier<T> path, RetrievalStrategy strategy) {
        return null;
    }

    @Override
    public <T extends DataObject> ListenableFuture<Optional<T>> readData(InstanceIdentifier path) {
        return null;
    }

    @Override
    public <T extends DataObject> ListenableFuture<Optional<T>> readData(InstanceIdentifier path, RetrievalStrategy strategy) {
        return null;
    }
}
