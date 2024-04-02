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
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.util.AbstractStringInstanceIdentifierCodec;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.util.LeafrefResolver;

abstract sealed class JSONInstanceIdentifierCodec extends AbstractStringInstanceIdentifierCodec
        implements JSONCodec<YangInstanceIdentifier> {
    static final class Lhotka02 extends JSONInstanceIdentifierCodec {
        Lhotka02(final EffectiveModelContext context, final JSONCodecFactory jsonCodecFactory) {
            super(context, jsonCodecFactory);
        }
    }

    static final class RFC7951 extends JSONInstanceIdentifierCodec {
        RFC7951(final EffectiveModelContext context, final JSONCodecFactory jsonCodecFactory) {
            super(context, jsonCodecFactory);
        }

        @Override
        protected StringBuilder appendQName(final StringBuilder sb, final QName qname, final QNameModule lastModule) {
            return qname.getModule().equals(lastModule) ? sb.append(qname.getLocalName())
                : super.appendQName(sb, qname, lastModule);
        }

        @Override
        protected QName createQName(final QNameModule lastModule, final String localName) {
            checkArgument(lastModule != null, "Unprefixed leading name %s", localName);
            return QName.create(lastModule, localName);
        }
    }

    private final @NonNull DataSchemaContextTree dataContextTree;
    private final JSONCodecFactory codecFactory;
    private final EffectiveModelContext context;

    JSONInstanceIdentifierCodec(final EffectiveModelContext context, final JSONCodecFactory jsonCodecFactory) {
        this.context = requireNonNull(context);
        dataContextTree = DataSchemaContextTree.from(context);
        codecFactory = requireNonNull(jsonCodecFactory);
    }

    @Override
    protected final QNameModule moduleForPrefix(final String prefix) {
        final var modules = context.findModuleStatements(prefix).iterator();
        return modules.hasNext() ? modules.next().localQNameModule() : null;
    }

    @Override
    protected final String prefixForNamespace(final XMLNamespace namespace) {
        final var modules = context.findModuleStatements(namespace).iterator();
        return modules.hasNext() ? modules.next().argument().getLocalName() : null;
    }

    @Override
    protected final DataSchemaContextTree getDataContextTree() {
        return dataContextTree;
    }

    @Override
    protected final Object deserializeKeyValue(final DataSchemaNode schemaNode, final LeafrefResolver resolver,
            final String value) {
        requireNonNull(schemaNode, "schemaNode cannot be null");
        if (schemaNode instanceof LeafSchemaNode leafSchemaNode) {
            return codecFactory.codecFor(leafSchemaNode, resolver).parseValue(value);
        } else if (schemaNode instanceof LeafListSchemaNode leafListSchemaNode) {
            return codecFactory.codecFor(leafListSchemaNode, resolver).parseValue(value);
        }
        throw new IllegalArgumentException("schemaNode " + schemaNode
                + " must be of type LeafSchemaNode or LeafListSchemaNode");
    }

    @Override
    public final Class<YangInstanceIdentifier> getDataType() {
        return YangInstanceIdentifier.class;
    }

    @Override
    public final YangInstanceIdentifier parseValue(final String str) {
        return deserialize(str);
    }

    @Override
    public final void writeValue(final JSONValueWriter ctx, final YangInstanceIdentifier value) throws IOException {
        final String str;
        try {
            str = serialize(value);
        } catch (IllegalArgumentException e) {
            throw new IOException("Failed to encode instance-identifier", e);
        }
        ctx.writeString(str);
    }
}
