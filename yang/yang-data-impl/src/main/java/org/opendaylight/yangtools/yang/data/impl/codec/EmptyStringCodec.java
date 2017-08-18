/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Strings;
import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.codec.EmptyCodec;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;

final class EmptyStringCodec extends TypeDefinitionAwareCodec<Void, EmptyTypeDefinition> implements
        EmptyCodec<String> {
    static final EmptyStringCodec INSTANCE = new EmptyStringCodec();

    private EmptyStringCodec() {
        super(Optional.empty(), Void.class);
    }

    @Override
    public String serialize(final Void data) {
        return "";
    }

    @Override
    public Void deserialize(final String stringRepresentation) {
        checkArgument(Strings.isNullOrEmpty(stringRepresentation), "The value must be empty");
        return null;
    }
}