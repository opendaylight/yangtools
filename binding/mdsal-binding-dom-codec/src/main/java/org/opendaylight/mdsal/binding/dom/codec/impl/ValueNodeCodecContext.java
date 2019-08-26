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
import org.opendaylight.yangtools.concepts.IllegalArgumentCodec;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * Abstract base class for atomic nodes. These are nodes which are not decomposed in the Binding Specification, such
 * as LeafNodes and LeafSetNodes.
 */
abstract class ValueNodeCodecContext extends NodeCodecContext implements NodeContextSupplier {
    abstract static class WithCodec extends ValueNodeCodecContext {
        private final @NonNull IllegalArgumentCodec<Object, Object> valueCodec;

        WithCodec(final DataSchemaNode schema, final IllegalArgumentCodec<Object, Object> codec,
                final String getterName, final Object defaultObject) {
            super(schema, getterName, defaultObject);
            this.valueCodec = requireNonNull(codec);
        }

        @Override
        final IllegalArgumentCodec<Object, Object> getValueCodec() {
            return valueCodec;
        }
    }

    private final @NonNull NodeIdentifier yangIdentifier;
    private final @NonNull String getterName;
    private final @NonNull DataSchemaNode schema;
    private final Object defaultObject;

    ValueNodeCodecContext(final DataSchemaNode schema, final String getterName, final Object defaultObject) {
        this.yangIdentifier = NodeIdentifier.create(schema.getQName());
        this.getterName = requireNonNull(getterName);
        this.schema = requireNonNull(schema);
        this.defaultObject = defaultObject;
    }

    @Override
    protected final NodeIdentifier getDomPathArgument() {
        return yangIdentifier;
    }

    @Override
    public final NodeCodecContext get() {
        return this;
    }

    final String getGetterName() {
        return getterName;
    }

    abstract IllegalArgumentCodec<Object, Object> getValueCodec();

    @Override
    public final DataSchemaNode getSchema() {
        return schema;
    }

    @Override
    final Object defaultObject() {
        return defaultObject;
    }
}
