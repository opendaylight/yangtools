/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.data.codec.api.BindingYangDataCodecTreeNode;
import org.opendaylight.yangtools.binding.runtime.api.YangDataRuntimeType;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataSchemaNode;
import org.opendaylight.yangtools.binding.YangData;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedYangData;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;

/**
 * A {@link YangData} codec context.
 */
final class YangDataCodecContext<T extends YangData<T>> extends DataContainerCodecContext<T, YangDataRuntimeType>
        implements BindingYangDataCodecTreeNode<T> {
    private final @NonNull YangDataRuntimeType type;
    private final @NonNull CodecContextFactory factory;
    private final @NonNull Class<T> bindingClass;

    YangDataCodecContext(final Class<T> bindingClass, final YangDataRuntimeType type,
            final CodecContextFactory factory) {
        super(type);
        this.bindingClass = requireNonNull(bindingClass);
        this.type = requireNonNull(type);
        this.factory = requireNonNull(factory);

        final var analysis = new DataContainerAnalysis<>(bindingClass, type, factory, CodecItemFactory.of());


    }

    @Override
    public WithStatus getSchema() {
        return (YangDataSchemaNode) type.statement();
    }

    @Override
    public Class<T> getBindingClass() {
        return bindingClass;
    }

    @Override
    protected CodecContextFactory factory() {
        return factory;
    }

    @Override
    protected YangDataRuntimeType type() {
        return type;
    }

    @Override
    protected NodeIdentifier getDomPathArgument() {
        return null;
    }

    @Override
    CommonDataObjectCodecPrototype<?> streamChildPrototype(final Class<?> childClass) {
        // FIXME: implement this
        throw new UnsupportedOperationException();
    }

    @Override
    CodecContextSupplier yangChildSupplier(final NodeIdentifier arg) {
        // FIXME: implement this
        throw new UnsupportedOperationException();
    }

    @Override
    protected Object deserializeObject(final NormalizedNode normalizedNode) {
        // FIXME: implement this
        throw new UnsupportedOperationException();
    }

    @Override
    public T toBinding(final NormalizedYangData dom) {
        // FIXME: implement this
        throw new UnsupportedOperationException();
    }

    @Override
    public NormalizedYangData fromBinding(final T binding) {
        // FIXME: implement this
        throw new UnsupportedOperationException();
    }
}
