/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.data.api.codec.BooleanCodec;
import org.opendaylight.yangtools.yang.data.api.codec.IllegalYangValueException;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;

/**
 * Do not use this class outside of yangtools, its presence does not fall into the API stability contract.
 */
@Beta
public final class BooleanStringCodec extends TypeDefinitionAwareCodec<Boolean, BooleanTypeDefinition>
        implements BooleanCodec<String> {
    private BooleanStringCodec(final @NonNull BooleanTypeDefinition typeDef) {
        super(typeDef, Boolean.class);
    }

    public static @NonNull BooleanStringCodec from(final BooleanTypeDefinition normalizedType) {
        return new BooleanStringCodec(requireNonNull(normalizedType));
    }

    @Override
    protected Boolean deserializeImpl(final String product) {
        // FIXME: should forbid "TRUE" ?
        if ("true".equalsIgnoreCase(product)) {
            return Boolean.TRUE;
        } else if ("false".equalsIgnoreCase(product)) {
            return Boolean.FALSE;
        } else {
            throw new IllegalYangValueException(
                    RpcError.ErrorSeverity.ERROR,
                    RpcError.ErrorType.PROTOCOL,
                    "bad-element",
                    "Invalid value '" + product + "' for boolean type. Allowed values are 'true' and 'false'");
        }
    }

    @Override
    protected String serializeImpl(final Boolean input) {
        return input.toString();
    }
}
