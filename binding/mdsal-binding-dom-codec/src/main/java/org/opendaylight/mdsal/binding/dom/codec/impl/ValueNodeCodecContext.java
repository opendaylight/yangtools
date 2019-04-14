/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Method;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * Abstract base class for atomic nodes. These are nodes which are not decomposed in the Binding Specification, such
 * as LeafNodes and LeafSetNodes.
 */
abstract class ValueNodeCodecContext extends NodeCodecContext implements NodeContextSupplier {
    abstract static class WithCodec extends ValueNodeCodecContext {
        private final @NonNull Codec<Object, Object> valueCodec;

        WithCodec(final DataSchemaNode schema, final Codec<Object, Object> codec, final Method getter,
                final Object defaultObject) {
            super(schema, getter, defaultObject);
            this.valueCodec = requireNonNull(codec);
        }

        @Override
        final Codec<Object, Object> getValueCodec() {
            return valueCodec;
        }
    }

    private final @NonNull NodeIdentifier yangIdentifier;
    private final @NonNull Method getter;
    private final @NonNull DataSchemaNode schema;
    private final Object defaultObject;

    ValueNodeCodecContext(final DataSchemaNode schema, final Method getter, final Object defaultObject) {
        this.yangIdentifier = NodeIdentifier.create(schema.getQName());
        this.getter = requireNonNull(getter);
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

    final Method getGetter() {
        return getter;
    }

    abstract Codec<Object, Object> getValueCodec();

    @Override
    public final DataSchemaNode getSchema() {
        return schema;
    }

    @Override
    final Object defaultObject() {
        return defaultObject;
    }
}
