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
import java.net.URI;
import java.util.Iterator;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.util.AbstractModuleStringInstanceIdentifierCodec;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;

abstract class JSONInstanceIdentifierCodec extends AbstractModuleStringInstanceIdentifierCodec
        implements JSONCodec<YangInstanceIdentifier> {
    private final DataSchemaContextTree dataContextTree;
    private final JSONCodecFactory codecFactory;
    private final EffectiveModelContext context;

    JSONInstanceIdentifierCodec(final EffectiveModelContext context, final JSONCodecFactory jsonCodecFactory) {
        this.context = requireNonNull(context);
        this.dataContextTree = DataSchemaContextTree.from(context);
        this.codecFactory = requireNonNull(jsonCodecFactory);
    }

    @Override
    protected final Module moduleForPrefix(final String prefix) {
        final Iterator<? extends Module> modules = context.findModules(prefix).iterator();
        return modules.hasNext() ? modules.next() : null;
    }

    @Override
    protected final String prefixForNamespace(final URI namespace) {
        final Iterator<? extends Module> modules = context.findModules(namespace).iterator();
        return modules.hasNext() ? modules.next().getName() : null;
    }

    @Override
    protected final DataSchemaContextTree getDataContextTree() {
        return dataContextTree;
    }

    @Override
    protected final Object deserializeKeyValue(final DataSchemaNode schemaNode, final String value) {
        requireNonNull(schemaNode, "schemaNode cannot be null");
        checkArgument(schemaNode instanceof LeafSchemaNode, "schemaNode must be of type LeafSchemaNode");
        final JSONCodec<?> objectJSONCodec = codecFactory.codecFor((LeafSchemaNode) schemaNode);
        return objectJSONCodec.parseValue(null, value);
    }

    @Override
    public final Class<YangInstanceIdentifier> getDataType() {
        return YangInstanceIdentifier.class;
    }

    @Override
    public final YangInstanceIdentifier parseValue(final Object ctx, final String str) {
        return deserialize(str);
    }

    @Override
    public final void writeValue(final JsonWriter ctx, final YangInstanceIdentifier value) throws IOException {
        ctx.value(serialize(value));
    }
}
