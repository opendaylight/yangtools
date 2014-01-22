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
import org.opendaylight.yangtools.restconf.client.api.data.OperationalDatastore;
import org.opendaylight.yangtools.restconf.client.api.data.RetrievalStrategy;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class OperationalDataStoreImpl implements OperationalDatastore {
    @Override
    public <T extends DataObject> ListenableFuture<Optional<T>> readData(InstanceIdentifier path) {
        return null;
    }

    @Override
    public <T extends DataObject> ListenableFuture<Optional<T>> readData(InstanceIdentifier path, RetrievalStrategy strategy) {
        return null;
    }
}
