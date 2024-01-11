/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * Abstract base class for atomic nodes. These are nodes which are not decomposed in the Binding Specification, such
 * as LeafNodes and LeafSetNodes.
 */
abstract sealed class ValueNodeCodecContext extends CodecContext implements CodecContextSupplier
        permits AbstractOpaqueCodecContext, ValueNodeCodecContext.WithCodec {
    abstract static sealed class WithCodec extends ValueNodeCodecContext
            permits LeafNodeCodecContext, LeafSetNodeCodecContext {
        private final @NonNull ValueCodec<Object, Object> valueCodec;

        WithCodec(final DataSchemaNode schema, final ValueCodec<Object, Object> codec, final String getterName,
                final Object defaultObject) {
            super(schema, getterName, defaultObject);
            valueCodec = requireNonNull(codec);
        }

        @Override
        final ValueCodec<Object, Object> getValueCodec() {
            return valueCodec;
        }
    }

    private final @NonNull NodeIdentifier yangIdentifier;
    private final @NonNull String getterName;
    private final @NonNull DataSchemaNode schema;
    private final Object defaultObject;

    ValueNodeCodecContext(final DataSchemaNode schema, final String getterName, final Object defaultObject) {
        yangIdentifier = NodeIdentifier.create(schema.getQName());
        this.getterName = requireNonNull(getterName);
        this.schema = requireNonNull(schema);
        this.defaultObject = defaultObject;
    }

    @Override
    protected final NodeIdentifier getDomPathArgument() {
        return yangIdentifier;
    }

    @Override
    public final CodecContext getCodecContext() {
        return this;
    }

    final String getGetterName() {
        return getterName;
    }

    abstract ValueCodec<Object, Object> getValueCodec();

    @Override
    public final DataSchemaNode getSchema() {
        return schema;
    }

    @Override
    final Object defaultObject() {
        return defaultObject;
    }
}
