/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

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

    /**
     * Get codec registry.
     *
     * @return codec registry
     */
    CodecRegistry getCodecRegistry();

    /**
     * Convert given DataObject data to DOM-like node.
     *
     * @param data
     *            DataObject instance
     * @return CompositeNode created from DataObject instance
     */
    CompositeNode toDataDom(DataObject data);

    /**
     * Create map entry representing node data (key = data schema node
     * identifier, value = value is node data representation as Composite node)
     * from entry representing node class (key = class object identifier, value
     * = class object).
     *
     * @param entry
     *            map entry, where key is class object identifier and value
     *            class object
     * @return data schema node identifier
     */
    Entry<org.opendaylight.yangtools.yang.data.api.InstanceIdentifier, CompositeNode> toDataDom(
            Entry<InstanceIdentifier<? extends DataObject>, DataObject> entry);

    /**
     * Create data schema node identifier from class object identifier.
     *
     * @param path
     *            class object identifier
     * @return data schema node identifier
     */
    org.opendaylight.yangtools.yang.data.api.InstanceIdentifier toDataDom(InstanceIdentifier<? extends DataObject> path);

    /**
     * Create DataObject instance from CompositeNode data based on given path.
     *
     * @param path
     *            node identifier
     * @param result
     *            node data
     * @return inputClass instance created from composite node input
     */
    DataObject dataObjectFromDataDom(InstanceIdentifier<? extends DataObject> path, CompositeNode result)
            throws DeserializationException;

    /**
     * Create class object identifier from data schema node identifier.
     *
     * @param entry
     *            data schema node identifier
     * @return class object identifier
     */
    InstanceIdentifier<?> fromDataDom(org.opendaylight.yangtools.yang.data.api.InstanceIdentifier entry)
            throws DeserializationException;

    /**
     * Returns the list of currently-known QNames for instances of a service.
     *
     * @param service
     *            RPC service
     * @return List of QNames. The user may not modify this list.
     */
    Set<QName> getRpcQNamesFor(Class<? extends RpcService> service);

    /**
     * Get RpcService by namespace and revision.
     *
     * @param namespace
     *            rpc service namespace
     * @param revision
     *            rpc service revision
     * @return Optional reference on RpcServices based on given namespace and
     *         revision
     */
    Optional<Class<? extends RpcService>> getRpcServiceClassFor(String namespace, String revision);

    /**
     * Create inputClass instance from CompositeNode data.
     *
     * @param inputClass
     *            expected type of resulting object
     * @param domInput
     *            node data
     * @return inputClass instance created from composite node input
     */
    DataContainer dataObjectFromDataDom(Class<? extends DataContainer> inputClass, CompositeNode domInput);

}
