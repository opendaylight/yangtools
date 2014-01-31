/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import java.net.URI;
import java.util.Map.Entry;
import java.util.Set;

import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;

import com.google.common.base.Optional;

public interface BindingIndependentMappingService {

    CodecRegistry getCodecRegistry();

    CompositeNode toDataDom(DataObject data);

    Entry<org.opendaylight.yangtools.yang.data.api.InstanceIdentifier, CompositeNode> toDataDom(
            Entry<InstanceIdentifier<? extends DataObject>, DataObject> entry);

    org.opendaylight.yangtools.yang.data.api.InstanceIdentifier toDataDom(InstanceIdentifier<? extends DataObject> path);

    DataObject dataObjectFromDataDom(InstanceIdentifier<? extends DataObject> path, CompositeNode result) throws DeserializationException;

    InstanceIdentifier<?> fromDataDom(org.opendaylight.yangtools.yang.data.api.InstanceIdentifier entry)  throws DeserializationException;

    /**
     * Returns the list of currently-known QNames for instances of a service.
     *
     * @param service RPC service
     * @return List of QNames. The user may not modify this list.
     */
    Set<QName> getRpcQNamesFor(Class<? extends RpcService> service);
    
    Optional<Class<? extends RpcService>> getRpcServiceClassFor(String namespace, String revision);

    DataContainer dataObjectFromDataDom(Class<? extends DataContainer> inputClass, CompositeNode domInput);

}
