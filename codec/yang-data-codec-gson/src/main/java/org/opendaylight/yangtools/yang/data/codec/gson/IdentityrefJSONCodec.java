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

import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.util.codec.IdentityCodecUtil;
import org.opendaylight.yangtools.yang.data.util.codec.QNameCodecUtil;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;

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

            final var modules = context.findModules(prefix).iterator();
            checkArgument(modules.hasNext(), "Could not find module %s", prefix);
            return modules.next().getQNameModule();
        }).getQName();
    }

    /**
     * Serialize QName with specified JsonWriter.
     *
     * @param writer JsonWriter
     * @param value QName
     */
    @Override
    public void writeValue(final JsonWriter writer, final QName value) throws IOException {
        final String str = QNameCodecUtil.encodeQName(value, uri -> context.findModule(uri)
            .map(Module::getName).orElseThrow(() -> new IllegalArgumentException("Cannot find module for " + uri)));
        writer.value(str);
    }
}
