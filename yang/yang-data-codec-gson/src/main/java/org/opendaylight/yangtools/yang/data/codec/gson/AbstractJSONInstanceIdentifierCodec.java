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
import java.net.URI;
import java.util.Iterator;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.util.AbstractModuleStringInstanceIdentifierCodec;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

abstract class AbstractJSONInstanceIdentifierCodec extends AbstractModuleStringInstanceIdentifierCodec
        implements JSONCodec<YangInstanceIdentifier> {
    DataSchemaContextTree dataContextTree;
    SchemaContext context;

    @Override
    protected Module moduleForPrefix(final String prefix) {
        final Iterator<Module> modules = context.findModules(prefix).iterator();
        return modules.hasNext() ? modules.next() : null;
    }

    @Override
    protected String prefixForNamespace(final URI namespace) {
        final Iterator<Module> modules = context.findModules(namespace).iterator();
        return modules.hasNext() ? modules.next().getName() : null;
    }

    @Override
    protected DataSchemaContextTree getDataContextTree() {
        return dataContextTree;
    }

    @Override
    public Class<YangInstanceIdentifier> getDataType() {
        return YangInstanceIdentifier.class;
    }

    @Override
    public YangInstanceIdentifier parseValue(final Object ctx, final String str) {
        return deserialize(str);
    }

    @Override
    public void writeValue(final JsonWriter ctx, final YangInstanceIdentifier value) throws IOException {
        ctx.value(serialize(value));
    }
}
