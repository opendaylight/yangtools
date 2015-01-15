/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client.api.data;

import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.ListenableFuture;

public interface Datastore {

    /**
     * Reads data from data store and return's result in future.
     * 
     * This call is equivalent to invocation of {@link #readData(InstanceIdentifier, RetrievalStrategy)}
     * with {@link DefaultRetrievalStrategy#getInstance()}.
     * 
     * @param path InstanceIdentifier representing path in YANG schema to be retrieved.
     * @return Future promising the data requested. If the requested data are not present returns value of {@link Optional#absent()}.
     * 
     */
    <T extends DataObject> ListenableFuture<Optional<T>> readData(InstanceIdentifier<T> path);
    
    /**
     * Reads data from data store and return's result in future.
     * 
     * @param path Representing path in YANG schema to be retrieved.
     * @param strategy Strategy which should be used to retrieve data
     * @return Future promising the data requested. If the requested data are not present returns value of {@link Optional#absent()}.
     * 
     */
    <T extends DataObject> ListenableFuture<Optional<T>> readData(InstanceIdentifier<T> path, RetrievalStrategy strategy);

}
