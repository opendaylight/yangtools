/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static java.util.Objects.requireNonNull;

import javax.xml.namespace.NamespaceContext;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.impl.codec.DataStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;

/**
 * Abstract base implementation of {@link XmlCodec}, which wraps a {@link TypeDefinitionAwareCodec}.
 *
 * @param <T> Deserialized object type
 */
abstract class AbstractXmlCodec<T> implements XmlCodec<T> {
    private final DataStringCodec<T> codec;

    AbstractXmlCodec(final DataStringCodec<T> codec) {
        this.codec = requireNonNull(codec);
    }

    @Override
    public final Class<T> getDataType() {
        return codec.getInputClass();
    }

    @Override
    public final T parseValue(final NamespaceContext namespaceContext, final String str) {
        return codec.deserialize(trimValue(str));
    }

    // FIXME: YANGTOOLS-1523: remove this method
    @Deprecated
    @NonNull String trimValue(final @NonNull String str) {
        return str.trim();
    }

    final String serialize(final T input) {
        return codec.serialize(input);
    }
}
