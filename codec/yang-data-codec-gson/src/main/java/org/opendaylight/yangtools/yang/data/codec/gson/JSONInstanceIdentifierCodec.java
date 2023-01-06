/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static java.util.Objects.requireNonNull;

import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Iterator;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.util.AbstractModuleStringInstanceIdentifierCodec;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.util.LeafrefResolver;

abstract class JSONInstanceIdentifierCodec extends AbstractModuleStringInstanceIdentifierCodec
        implements JSONCodec<YangInstanceIdentifier> {
    private final DataSchemaContextTree dataContextTree;
    private final JSONCodecFactory codecFactory;
    private final EffectiveModelContext context;

    JSONInstanceIdentifierCodec(final EffectiveModelContext context, final JSONCodecFactory jsonCodecFactory) {
        this.context = requireNonNull(context);
        dataContextTree = DataSchemaContextTree.from(context);
        codecFactory = requireNonNull(jsonCodecFactory);
    }

    @Override
    protected final Module moduleForPrefix(final String prefix) {
        final Iterator<? extends Module> modules = context.findModules(prefix).iterator();
        return modules.hasNext() ? modules.next() : null;
    }

    @Override
    protected final String prefixForNamespace(final XMLNamespace namespace) {
        final Iterator<? extends Module> modules = context.findModules(namespace).iterator();
        return modules.hasNext() ? modules.next().getName() : null;
    }

    @Override
    protected final DataSchemaContextTree getDataContextTree() {
        return dataContextTree;
    }

    @Override
    protected final Object deserializeKeyValue(final DataSchemaNode schemaNode, final LeafrefResolver resolver,
            final String value) {
        requireNonNull(schemaNode, "schemaNode cannot be null");
        if (schemaNode instanceof LeafSchemaNode) {
            return codecFactory.codecFor((LeafSchemaNode) schemaNode, resolver).parseValue(null, value);
        } else if (schemaNode instanceof LeafListSchemaNode) {
            return codecFactory.codecFor((LeafListSchemaNode) schemaNode, resolver).parseValue(null, value);
        }
        throw new IllegalArgumentException("schemaNode " + schemaNode
                + " must be of type LeafSchemaNode or LeafListSchemaNode");
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
