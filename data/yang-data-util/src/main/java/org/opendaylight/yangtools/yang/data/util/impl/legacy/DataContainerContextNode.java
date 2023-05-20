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
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextNode;

abstract class DataContainerContextNode<T extends PathArgument> extends AbstractInteriorContextNode<T> {
    private final ConcurrentMap<PathArgument, AbstractDataSchemaContextNode<?>> byArg = new ConcurrentHashMap<>();
    private final ConcurrentMap<QName, AbstractDataSchemaContextNode<?>> byQName = new ConcurrentHashMap<>();
    private final DataNodeContainer container;

    DataContainerContextNode(final T identifier, final DataNodeContainer container, final DataSchemaNode schema) {
        super(identifier, schema);
        this.container = requireNonNull(container);
    }

    @Override
    public AbstractDataSchemaContextNode<?> getChild(final PathArgument child) {
        AbstractDataSchemaContextNode<?> potential = byArg.get(child);
        if (potential != null) {
            return potential;
        }
        potential = fromLocalSchema(child);
        return register(potential);
    }

    @Override
    public AbstractDataSchemaContextNode<?> getChild(final QName child) {
        AbstractDataSchemaContextNode<?> potential = byQName.get(child);
        if (potential != null) {
            return potential;
        }
        potential = fromLocalSchemaAndQName(container, child);
        return register(potential);
    }

    @Override
    protected final DataSchemaContextNode enterChild(final QName child, final SchemaInferenceStack stack) {
        return pushToStack(getChild(child), stack);
    }

    @Override
    protected final DataSchemaContextNode enterChild(final PathArgument child, final SchemaInferenceStack stack) {
        return pushToStack(getChild(child), stack);
    }

    private static @Nullable DataSchemaContextNode pushToStack(final @Nullable AbstractDataSchemaContextNode<?> child,
            final @NonNull SchemaInferenceStack stack) {
        if (child != null) {
            child.pushToStack(stack);
        }
        return child;
    }

    private AbstractDataSchemaContextNode<?> fromLocalSchema(final PathArgument child) {
        return fromSchemaAndQNameChecked(container, child.getNodeType());
    }

    protected AbstractDataSchemaContextNode<?> fromLocalSchemaAndQName(final DataNodeContainer schema,
            final QName child) {
        return fromSchemaAndQNameChecked(schema, child);
    }

    private AbstractDataSchemaContextNode<?> register(final AbstractDataSchemaContextNode<?> potential) {
        if (potential != null) {
            // FIXME: use putIfAbsent() to make sure we do not perform accidental overrwrites
            byArg.put(potential.getIdentifier(), potential);
            for (QName qname : potential.qnameIdentifiers()) {
                byQName.put(qname, potential);
            }
        }
        return potential;
    }
}
