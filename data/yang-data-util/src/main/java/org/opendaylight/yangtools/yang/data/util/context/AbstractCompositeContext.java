/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.context;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContext.Composite;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree.Step;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

public abstract sealed class AbstractCompositeContext extends AbstractContext implements Composite
        permits ListItemContext, ContainerContext, YangDataContext {
    // FIXME: CAS-to-immutable once fully instantiated
    private final ConcurrentHashMap<PathArgument, AbstractContext> byArg = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<QName, AbstractContext> byQName = new ConcurrentHashMap<>();
    private final @NonNull DataNodeContainer container;

    AbstractCompositeContext(final NodeIdentifier pathStep, final DataNodeContainer container,
            final DataSchemaNode schema) {
        super(pathStep, schema);
        this.container = requireNonNull(container);
    }

    @Override
    public final AbstractContext childByArg(final PathArgument arg) {
        final var existing = byArg.get(requireNonNull(arg));
        return existing != null ? existing : childFromSchema(arg.getNodeType());
    }

    @Override
    public final AbstractContext childByQName(final QName qname) {
        var existing = byQName.get(requireNonNull(qname));
        return existing != null ? existing : childFromSchema(qname);
    }

    private @Nullable AbstractContext childFromSchema(final @NonNull QName child) {
        final var childSchema = childByName(container, child);
        return childSchema == null ? null : loadChildFromSchema(childSchema);
    }

    @NonNullByDefault
    private AbstractContext loadChildFromSchema(final DataSchemaNode childSchema) {
        final var child = AbstractContext.of(childSchema);
        // FIXME: use putIfAbsent() to make sure we do not perform accidental overrwrites
        byArg.put(child.getPathStep(), child);
        for (var qname : child.qnameIdentifiers()) {
            byQName.put(qname, child);
        }
        return child;
    }

    @Override
    public final AbstractContext enterChild(final SchemaInferenceStack stack, final PathArgument child) {
        return enterChild(stack, childByArg(child));
    }

    @Override
    public final AbstractContext enterChild(final SchemaInferenceStack stack, final QName child) {
        return enterChild(stack, childByQName(child));
    }

    private static AbstractContext enterChild(final SchemaInferenceStack stack, final @Nullable AbstractContext child) {
        requireNonNull(stack);
        if (child != null) {
            child.pushToStack(stack);
        }
        return child;
    }

    @Override
    public final Step.Exact stepTo(final QName child) {
        final var childContext = childByQName(child);
        return childContext == null ? null : new Step.Exact(childContext);
    }
}
