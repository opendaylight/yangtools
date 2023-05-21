/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.impl.legacy;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

abstract sealed class DataContainerContextNode extends AbstractCompositeContextNode
        permits AbstractListItemContextNode, ContainerContextNode {
    private final ConcurrentMap<PathArgument, AbstractDataSchemaContextNode> byArg = new ConcurrentHashMap<>();
    private final ConcurrentMap<QName, AbstractDataSchemaContextNode> byQName = new ConcurrentHashMap<>();
    private final DataNodeContainer container;

    DataContainerContextNode(final NodeIdentifier pathStep, final DataNodeContainer container,
            final DataSchemaNode schema) {
        super(pathStep, schema);
        this.container = requireNonNull(container);
    }

    @Override
    public final AbstractDataSchemaContextNode getChild(final PathArgument child) {
        final var existing = byArg.get(child);
        if (existing != null) {
            return existing;
        }
        return register(fromLocalSchema(child));
    }

    @Override
    public final AbstractDataSchemaContextNode getChild(final QName child) {
        var existing = byQName.get(child);
        if (existing != null) {
            return existing;
        }
        return register(fromLocalSchemaAndQName(container, child));
    }

    @Override
    final DataSchemaContextNode enterChild(final QName child, final SchemaInferenceStack stack) {
        return pushToStack(getChild(child), stack);
    }

    @Override
    final DataSchemaContextNode enterChild(final PathArgument child, final SchemaInferenceStack stack) {
        return pushToStack(getChild(child), stack);
    }

    private static @Nullable DataSchemaContextNode pushToStack(final @Nullable AbstractDataSchemaContextNode child,
            final @NonNull SchemaInferenceStack stack) {
        if (child != null) {
            child.pushToStack(stack);
        }
        return child;
    }

    private AbstractDataSchemaContextNode fromLocalSchema(final PathArgument child) {
        return fromSchemaAndQNameChecked(container, child.getNodeType());
    }

    private static AbstractDataSchemaContextNode fromLocalSchemaAndQName(final DataNodeContainer schema,
            final QName child) {
        return fromSchemaAndQNameChecked(schema, child);
    }

    private AbstractDataSchemaContextNode register(final AbstractDataSchemaContextNode potential) {
        if (potential != null) {
            // FIXME: use putIfAbsent() to make sure we do not perform accidental overrwrites
            byArg.put(potential.getPathStep(), potential);
            for (QName qname : potential.qnameIdentifiers()) {
                byQName.put(qname, potential);
            }
        }
        return potential;
    }
}
