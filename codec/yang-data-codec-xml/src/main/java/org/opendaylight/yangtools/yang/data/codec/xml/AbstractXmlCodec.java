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
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.data.spi.codec.DataStringCodec;

/**
 * Abstract base implementation of {@link XmlCodec}, which wraps a {@link TypeDefinitionAwareCodec}.
 *
 * @param <T> Deserialized object type
 */
abstract sealed class AbstractXmlCodec<T> implements XmlCodec<T>
        permits BooleanXmlCodec, NumberXmlCodec, QuotedXmlCodec {
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
        return codec.deserialize(str);
    }

    final String serialize(final T input) {
        return codec.serialize(input);
    }
}
