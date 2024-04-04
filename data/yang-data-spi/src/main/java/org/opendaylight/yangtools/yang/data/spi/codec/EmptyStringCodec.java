/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.codec;

import static com.google.common.base.Preconditions.checkArgument;

import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.data.api.codec.EmptyCodec;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;

final class EmptyStringCodec extends TypeDefinitionAwareCodec<Empty, EmptyTypeDefinition>
        implements EmptyCodec<String> {
    EmptyStringCodec(final EmptyTypeDefinition typeDef) {
        super(Empty.class, typeDef);
    }

    @Override
    protected Empty deserializeImpl(final String product) {
        checkArgument(product.isEmpty(), "The value must be empty");
        return Empty.value();
    }

    @Override
    protected String serializeImpl(final Empty input) {
        return "";
    }
}