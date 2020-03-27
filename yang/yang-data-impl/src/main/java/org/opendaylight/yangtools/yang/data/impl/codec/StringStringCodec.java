/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.data.api.codec.IllegalYangValueException;
import org.opendaylight.yangtools.yang.data.api.codec.StringCodec;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;

/**
 * Do not use this class outside of yangtools, its presence does not fall into the API stability contract.
 */
@Beta
public class StringStringCodec extends TypeDefinitionAwareCodec<String, StringTypeDefinition>
        implements StringCodec<String> {
    private final LengthConstraint lengthConstraint;

    StringStringCodec(final StringTypeDefinition typeDef) {
        super(requireNonNull(typeDef), String.class);
        lengthConstraint = typeDef.getLengthConstraint().orElse(null);
    }

    public static StringStringCodec from(final StringTypeDefinition normalizedType) {
        return normalizedType.getPatternConstraints().isEmpty() ? new StringStringCodec(normalizedType)
                : new StringPatternCheckingCodec(normalizedType);
    }

    @Override
    protected final String deserializeImpl(final String stringRepresentation) {
        validate(stringRepresentation);
        return stringRepresentation;
    }

    @Override
    protected final String serializeImpl(final String data) {
        return data;
    }

    void validate(final String str) {
        if (lengthConstraint != null) {
            if (!lengthConstraint.getAllowedRanges().contains(str.length())) {
                throw new IllegalYangValueException(
                        RpcError.ErrorSeverity.ERROR,
                        RpcError.ErrorType.PROTOCOL,
                        "bad-element",
                        "String " + str + " does not match allowed length constraint " + lengthConstraint);
            }
        }
    }
}
