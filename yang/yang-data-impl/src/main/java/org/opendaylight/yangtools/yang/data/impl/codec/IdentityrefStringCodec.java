/*
 * Copyright (c) 2016 Intel Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.codec;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.net.URI;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.codec.IdentityrefCodec;
import org.opendaylight.yangtools.yang.data.util.ModuleStringIdentityrefCodec;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;

public class IdentityrefStringCodec
        extends TypeDefinitionAwareCodec<QName, IdentityrefTypeDefinition>
        implements IdentityrefCodec<String> {
    private final ModuleStringIdentityrefCodec codec;

    private IdentityrefStringCodec(
        final IdentityrefTypeDefinition type, final SchemaContext context, final QNameModule parentModule) {
        super(Optional.of(type), QName.class);
        codec = new ModuleStringIdentityrefCodec(context, parentModule);
    }

    static TypeDefinitionAwareCodec<?, IdentityrefTypeDefinition> from(
        final IdentityrefTypeDefinition type, final SchemaContext context, final QNameModule parentModule) {
        return new IdentityrefStringCodec(type, context, parentModule);
    }

    @Override
    public String serialize(final QName data) {
        return codec.serialize(data);
    }

    @Override
    public QName deserialize(final String data) {
        return codec.deserialize(data);
    }
}
