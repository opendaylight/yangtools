/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.codec;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.codec.BooleanCodec;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;

/**
 * Do not use this class outside of yangtools, its presence does not fall into the API stability contract.
 */
@Beta
public final class BooleanStringCodec extends TypeDefinitionAwareCodec<Boolean, BooleanTypeDefinition>
        implements BooleanCodec<String> {
    private BooleanStringCodec(final BooleanTypeDefinition typeDef) {
        super(Boolean.class, typeDef);
    }

    public static @NonNull BooleanStringCodec from(final BooleanTypeDefinition typeDef) {
        return new BooleanStringCodec(typeDef);
    }

    @Override
    protected Boolean deserializeImpl(final String product) {
        return switch (product) {
            case "true" -> Boolean.TRUE;
            case "false" -> Boolean.FALSE;
            default -> throw new IllegalArgumentException(
                "Invalid value '" + product + "' for boolean type. Allowed values are 'true' and 'false'");
        };
    }

    @Override
    protected String serializeImpl(final Boolean input) {
        return input.toString();
    }
}
