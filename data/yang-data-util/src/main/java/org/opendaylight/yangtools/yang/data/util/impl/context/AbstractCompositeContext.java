/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.impl.context;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContext;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

public abstract sealed class AbstractCompositeContext extends AbstractContext implements DataSchemaContext.Composite
        permits ListItemContext, ContainerContext {
    // FIXME: ImmutableMaps with compare-and-swap updates
    private final ConcurrentMap<PathArgument, AbstractContext> byArg = new ConcurrentHashMap<>();
    private final ConcurrentMap<QName, AbstractContext> byQName = new ConcurrentHashMap<>();
    private final DataNodeContainer container;

    AbstractCompositeContext(final NodeIdentifier pathStep, final DataNodeContainer container,
            final DataSchemaNode schema) {
        super(pathStep, schema);
        this.container = requireNonNull(container);
    }

    @Override
    public final AbstractContext childByArg(final PathArgument arg) {
        final var existing = byArg.get(requireNonNull(arg));
        if (existing != null) {
            return existing;
        }
        return register(fromLocalSchema(arg));
    }

    @Override
    public final AbstractContext childByQName(final QName qname) {
        var existing = byQName.get(requireNonNull(qname));
        if (existing != null) {
            return existing;
        }
        return register(fromLocalSchemaAndQName(container, qname));
    }

    @Override
    public final AbstractContext enterChild(final SchemaInferenceStack stack, final QName qname) {
        return pushToStack(stack, childByQName(qname));
    }

    @Override
    public final AbstractContext enterChild(final SchemaInferenceStack stack, final PathArgument arg) {
        return pushToStack(stack, childByArg(arg));
    }

    private static AbstractContext pushToStack(final SchemaInferenceStack stack, final AbstractContext child) {
        requireNonNull(stack);
        if (child != null) {
            child.pushToStack(stack);
        }
        return child;
    }

    private AbstractContext fromLocalSchema(final PathArgument child) {
        return fromSchemaAndQNameChecked(container, child.getNodeType());
    }

    private static AbstractContext fromLocalSchemaAndQName(final DataNodeContainer schema, final QName child) {
        return fromSchemaAndQNameChecked(schema, child);
    }

    private AbstractContext register(final AbstractContext potential) {
        if (potential != null) {
            // FIXME: use putIfAbsent() to make sure we do not perform accidental overrwrites
            byArg.put(potential.getPathStep(), potential);
            for (var qname : potential.qnameIdentifiers()) {
                byQName.put(qname, potential);
            }
        }
        return potential;
    }
}
