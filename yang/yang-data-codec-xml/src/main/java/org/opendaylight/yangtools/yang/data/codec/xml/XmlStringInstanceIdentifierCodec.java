/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.codec.xml;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.Deque;
import javax.annotation.Nonnull;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.util.AbstractModuleStringInstanceIdentifierCodec;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

final class XmlStringInstanceIdentifierCodec extends AbstractModuleStringInstanceIdentifierCodec
        implements XmlCodec<YangInstanceIdentifier> {

    private static final ThreadLocal<Deque<NamespaceContext>> TL_CONTEXT = new ThreadLocal<>();

    private final DataSchemaContextTree dataContextTree;
    private final XmlCodecFactory codecFactory;
    private final SchemaContext context;

    XmlStringInstanceIdentifierCodec(final SchemaContext context, final XmlCodecFactory xmlCodecFactory) {
        this.context = requireNonNull(context);
        this.dataContextTree = DataSchemaContextTree.from(context);
        this.codecFactory = requireNonNull(xmlCodecFactory);
    }

    @Override
    protected Module moduleForPrefix(@Nonnull final String prefix) {
        final String prefixedNS = getNamespaceContext().getNamespaceURI(prefix);
        return context.findModuleByNamespaceAndRevision(URI.create(prefixedNS), null);
    }

    @Override
    protected String prefixForNamespace(@Nonnull final URI namespace) {
        final Module module = context.findModuleByNamespaceAndRevision(namespace, null);
        return module == null ? null : module.getName();
    }

    @Nonnull
    @Override
    protected DataSchemaContextTree getDataContextTree() {
        return dataContextTree;
    }

    @Override
    protected Object deserializeKeyValue(final DataSchemaNode schemaNode, final String value) {
        requireNonNull(schemaNode, "schemaNode cannot be null");
        checkArgument(schemaNode instanceof LeafSchemaNode, "schemaNode must be of type LeafSchemaNode");
        final XmlCodec<?> objectXmlCodec = codecFactory.codecFor((LeafSchemaNode) schemaNode);
        return objectXmlCodec.parseValue(getNamespaceContext(), value);
    }

    @Override
    public Class<YangInstanceIdentifier> getDataType() {
        return YangInstanceIdentifier.class;
    }

    @Override
    public YangInstanceIdentifier parseValue(final NamespaceContext ctx, final String str) {
        pushNamespaceContext(ctx);
        try {
            return deserialize(str);
        } finally {
            popNamespaceContext();
        }
    }

    @Override
    public void writeValue(final XMLStreamWriter ctx, final YangInstanceIdentifier value)
            throws XMLStreamException {
        ctx.writeCharacters(serialize(value));
    }

    private static NamespaceContext getNamespaceContext() {
        return TL_CONTEXT.get().getFirst();
    }

    private static void popNamespaceContext() {
        final Deque<NamespaceContext> stack = TL_CONTEXT.get();
        stack.pop();
        if (stack.isEmpty()) {
            TL_CONTEXT.set(null);
        }
    }

    private static void pushNamespaceContext(final NamespaceContext context) {
        Deque<NamespaceContext> stack = TL_CONTEXT.get();
        if (stack == null) {
            stack = new ArrayDeque<>(1);
            TL_CONTEXT.set(stack);
        }
        stack.push(context);
    }
}
