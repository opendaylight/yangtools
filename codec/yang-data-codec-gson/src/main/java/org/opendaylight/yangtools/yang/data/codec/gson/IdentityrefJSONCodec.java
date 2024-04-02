/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONValue.Kind;
import org.opendaylight.yangtools.yang.data.util.codec.IdentityCodecUtil;
import org.opendaylight.yangtools.yang.data.util.codec.QNameCodecUtil;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

final class IdentityrefJSONCodec implements JSONCodec<QName> {
    private final @NonNull EffectiveModelContext context;
    private final @NonNull QNameModule parentModule;

    IdentityrefJSONCodec(final EffectiveModelContext context, final QNameModule parentModule) {
        this.context = requireNonNull(context);
        this.parentModule = requireNonNull(parentModule);
    }

    @Override
    public Class<QName> getDataType() {
        return QName.class;
    }

    @Override
    public QName parseValue(final String value) {
        return IdentityCodecUtil.parseIdentity(value, context, prefix -> {
            if (prefix.isEmpty()) {
                return parentModule;
            }

            final var modules = context.findModuleStatements(prefix).iterator();
            checkArgument(modules.hasNext(), "Could not find module %s", prefix);
            return modules.next().localQNameModule();
        }).getQName();
    }

    @Override
    public JSONValue unparseValue(final QName value) {
        return new JSONValue(encode(value), Kind.STRING);
    }

    @Override
    public void writeValue(final JSONValueWriter ctx, final QName value) throws IOException {
        ctx.writeString(encode(value));
    }

    private @NonNull String encode(final QName value) {
        return QNameCodecUtil.encodeQName(value, uri -> context.findModuleStatement(uri)
            .map(module -> module.argument().getLocalName())
            .orElseThrow(() -> new IllegalArgumentException("Cannot find module for " + uri)));
    }
}
