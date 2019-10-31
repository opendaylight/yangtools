/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.data.api.codec.EmptyCodec;
import org.opendaylight.yangtools.yang.data.api.codec.IllegalYangValueException;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;

final class EmptyStringCodec extends TypeDefinitionAwareCodec<Empty, EmptyTypeDefinition>
        implements EmptyCodec<String> {
    static final EmptyStringCodec INSTANCE = new EmptyStringCodec();

    private EmptyStringCodec() {
        super(null, Empty.class);
    }

    @Override
    protected Empty deserializeImpl(final String product) {
        if (!product.isEmpty()) {
            throw new IllegalYangValueException(
                    RpcError.ErrorSeverity.ERROR,
                    RpcError.ErrorType.PROTOCOL,
                    "The value must be empty",
                    "bad-element");
        }
        return Empty.getInstance();
    }

    @Override
    protected @NonNull String serializeImpl(final Empty input) {
        return "";
    }
}