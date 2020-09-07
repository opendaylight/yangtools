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
import java.util.Iterator;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.util.codec.IdentityCodecUtil;
import org.opendaylight.yangtools.yang.data.util.codec.QNameCodecUtil;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;

final class IdentityrefJSONCodec implements JSONCodec<QName> {
    private final EffectiveModelContext schemaContext;
    private final QNameModule parentModule;

    IdentityrefJSONCodec(final EffectiveModelContext context, final QNameModule parentModule) {
        this.schemaContext = requireNonNull(context);
        this.parentModule = requireNonNull(parentModule);
    }

    @Override
    public Class<QName> getDataType() {
        return QName.class;
    }

    @Override
    public QName parseValue(final Object ctx, final String value) {
        return IdentityCodecUtil.parseIdentity(value, schemaContext, prefix -> {
            if (prefix.isEmpty()) {
                return parentModule;
            }

            final Iterator<? extends Module> modules = schemaContext.findModules(prefix).iterator();
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
        final String str = QNameCodecUtil.encodeQName(value, uri -> {
            final Optional<String> optName = schemaContext.findModule(uri).map(Module::getName);
            checkArgument(optName.isPresent(), "Cannot find module for %s", uri);
            return optName.get();
        });
        writer.value(str);
    }
}
