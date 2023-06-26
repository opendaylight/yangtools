/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import org.opendaylight.yangtools.binding.YangData;
import org.opendaylight.yangtools.binding.data.codec.api.BindingYangDataCodecTreeNode;
import org.opendaylight.yangtools.binding.runtime.api.YangDataRuntimeType;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedYangData;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;

/**
 * A {@link YangData} codec context.
 */
final class YangDataCodecContext<T extends YangData<T>>
        extends AnalyzedDataContainerCodecContext<T, YangDataRuntimeType, YangDataCodecPrototype<T>>
        implements BindingYangDataCodecTreeNode<T> {
    YangDataCodecContext(final YangDataCodecPrototype<T> prototype) {
        super(prototype, new DataContainerAnalysis<>(prototype));
    }

    YangDataCodecContext(final Class<T> javaClass, final YangDataRuntimeType runtimeType,
            final CodecContextFactory contextFactory) {
        this(new YangDataCodecPrototype<>(contextFactory, runtimeType, javaClass));
    }

    @Override
    @Deprecated(since = "13.0.0", forRemoval = true)
    public WithStatus getSchema() {
        // FIXME: Bad cast, we should be returning an EffectiveStatement perhaps?
        return (WithStatus) prototype().runtimeType().statement();
    }

    @Override
    protected NodeIdentifier getDomPathArgument() {
        return null;
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

    @Override
    Object deserializeObject(final NormalizedNode normalizedNode) {
        // FIXME: implement this
        throw new UnsupportedOperationException();
    }
}
