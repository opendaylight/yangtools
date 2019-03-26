/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

class DataContainerContextNode<T extends PathArgument> extends AbstractInteriorContextNode<T> {
    private final DataNodeContainer schema;
    private final Map<QName, DataSchemaContextNode<?>> byQName;
    private final Map<PathArgument, DataSchemaContextNode<?>> byArg;

    DataContainerContextNode(final T identifier, final DataNodeContainer schema, final DataSchemaNode node) {
        super(identifier, node);
        this.schema = schema;
        this.byArg = new ConcurrentHashMap<>();
        this.byQName = new ConcurrentHashMap<>();
    }

    @Override
    public DataSchemaContextNode<?> getChild(final PathArgument child) {
        DataSchemaContextNode<?> potential = byArg.get(child);
        if (potential != null) {
            return potential;
        }
        potential = fromLocalSchema(child);
        return register(potential);
    }

    @Override
    public DataSchemaContextNode<?> getChild(final QName child) {
        DataSchemaContextNode<?> potential = byQName.get(child);
        if (potential != null) {
            return potential;
        }
        potential = fromLocalSchemaAndQName(schema, child);
        return register(potential);
    }

    private DataSchemaContextNode<?> fromLocalSchema(final PathArgument child) {
        if (child instanceof AugmentationIdentifier) {
            return fromSchemaAndQNameChecked(schema, ((AugmentationIdentifier) child).getPossibleChildNames()
                    .iterator().next());
        }
        return fromSchemaAndQNameChecked(schema, child.getNodeType());
    }

    protected DataSchemaContextNode<?> fromLocalSchemaAndQName(final DataNodeContainer schema2, final QName child) {
        return fromSchemaAndQNameChecked(schema2, child);
    }

    private DataSchemaContextNode<?> register(final DataSchemaContextNode<?> potential) {
        if (potential != null) {
            byArg.put(potential.getIdentifier(), potential);
            for (QName qname : potential.getQNameIdentifiers()) {
                byQName.put(qname, potential);
            }
        }
        return potential;
    }

}
