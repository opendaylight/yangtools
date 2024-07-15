/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.codec;

import org.opendaylight.yangtools.yang.data.api.codec.AbstractStringConverter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizationException;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;

final class BooleanStringConverter extends AbstractStringConverter<Boolean, BooleanTypeDefinition> {
    BooleanStringConverter(final BooleanTypeDefinition typeDef) {
        super(Boolean.class, typeDef);
    }

    @Override
    protected Boolean normalizeFromString(final BooleanTypeDefinition typedef, final String str)
            throws NormalizationException {
        return switch (str) {
            case "true" -> Boolean.TRUE;
            case "false" -> Boolean.FALSE;
            default -> throw NormalizationException.ofMessage(
                "Invalid value '" + str + "' for boolean type. Allowed values are 'true' and 'false'");
        };
    }

    @Override
    protected String canonizeToString(final BooleanTypeDefinition typedef, final Boolean obj) {
        return obj.toString();
    }
}
