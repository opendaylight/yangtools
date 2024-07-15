/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.codec;

import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.data.api.codec.AbstractStringConverter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizationException;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;

final class EmptyStringConverter extends AbstractStringConverter<Empty, EmptyTypeDefinition> {
    EmptyStringConverter(final EmptyTypeDefinition typeDef) {
        super(Empty.class, typeDef);
    }

    @Override
    protected Empty normalizeFromString(final EmptyTypeDefinition typedef, final String str)
            throws NormalizationException {
        if (str.isEmpty()) {
            return Empty.value();
        }
        throw NormalizationException.ofMessage(
            "Invalid value '" + str + "' for empty type. Only empty string is allowed.");
    }

    @Override
    protected String canonizeToString(final EmptyTypeDefinition typedef, final Empty obj) {
        return "";
    }
}
