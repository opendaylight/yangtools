/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.util.ModuleStringIdentityrefCodec;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

final class JSONStringIdentityrefCodec extends ModuleStringIdentityrefCodec implements JSONCodec<QName> {
    JSONStringIdentityrefCodec(final SchemaContext context, final QNameModule parentModule) {
        super(context, parentModule);
    }

    @Override
    protected Module moduleForPrefix(@Nonnull final String prefix) {
        if (prefix.isEmpty()) {
            return context.findModuleByNamespaceAndRevision(parentModuleQname.getNamespace(),
                    parentModuleQname.getRevision());
        } else {
            return context.findModuleByName(prefix, null);
        }
    }

    /**
     * Serialize QName with specified JsonWriter.
     *
     * @param writer JsonWriter
     * @param value QName
     */
    @Override
    public void serializeToWriter(JsonWriter writer, QName value) throws IOException {
        writer.value(serialize(value));
    }
}
