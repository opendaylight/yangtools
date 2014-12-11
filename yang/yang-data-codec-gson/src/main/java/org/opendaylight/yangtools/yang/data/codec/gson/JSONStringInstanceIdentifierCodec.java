/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.common.base.Preconditions;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.net.URI;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.util.AbstractModuleStringInstanceIdentifierCodec;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

final class JSONStringInstanceIdentifierCodec extends AbstractModuleStringInstanceIdentifierCodec implements JSONCodec<YangInstanceIdentifier> {
    private final SchemaContext context;

    JSONStringInstanceIdentifierCodec(final SchemaContext context) {
        this.context = Preconditions.checkNotNull(context);
    }

    @Override
    protected Module moduleForPrefix(final String prefix) {
        return context.findModuleByName(prefix, null);
    }

    @Override
    protected String prefixForNamespace(final URI namespace) {
        final Module module = context.findModuleByNamespaceAndRevision(namespace, null);
        return module == null ? null : module.getName();
    }

    @Override
    public boolean needQuotes() {
        return true;
    }

    @Override
    public void serializeToWriter(JsonWriter writer, YangInstanceIdentifier value) throws IOException {
        writer.value(serialize(value));
    }
}
