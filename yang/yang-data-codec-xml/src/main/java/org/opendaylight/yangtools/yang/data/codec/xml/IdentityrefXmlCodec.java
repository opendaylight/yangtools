/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.codec.xml;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.Deque;
import javax.annotation.Nonnull;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.util.ModuleStringIdentityrefCodec;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

final class IdentityrefXmlCodec extends ModuleStringIdentityrefCodec implements XmlCodec<QName> {
    private static final ThreadLocal<Deque<NamespaceContext>> TL_NSCONTEXT = new ThreadLocal<>();

    IdentityrefXmlCodec(final SchemaContext context, final QNameModule parentModule) {
        super(context, parentModule);
    }

    @Override
    protected Module moduleForPrefix(@Nonnull final String prefix) {
        if (prefix.isEmpty()) {
            return context.findModuleByNamespaceAndRevision(parentModuleQname.getNamespace(),
                    parentModuleQname.getRevision());
        }

        final String prefixedNS = getNamespaceContext().getNamespaceURI(prefix);
        return context.findModuleByNamespaceAndRevision(URI.create(prefixedNS), null);
    }

    @Override
    public Class<QName> getDataType() {
        return QName.class;
    }

    /**
     * Serialize QName with specified XMLStreamWriter.
     *
     * @param writer XMLStreamWriter
     * @param value QName
     */
    @Override
    public void writeValue(final XMLStreamWriter writer, final QName value) throws XMLStreamException {
        // FIXME: this does not work correctly, as we need to populate entries into the namespace context
        writer.writeCharacters(serialize(value));
    }

    @Override
    public QName parseValue(final NamespaceContext namespaceContext, final String value) {
        pushNamespaceContext(namespaceContext);
        try {
            return deserialize(value);
        } finally {
            popNamespaceContext();
        }
    }

    private static NamespaceContext getNamespaceContext() {
        return TL_NSCONTEXT.get().getFirst();
    }

    private static void popNamespaceContext() {
        final Deque<NamespaceContext> stack = TL_NSCONTEXT.get();
        stack.pop();
        if (stack.isEmpty()) {
            TL_NSCONTEXT.set(null);
        }
    }

    private static void pushNamespaceContext(final NamespaceContext context) {
        Deque<NamespaceContext> stack = TL_NSCONTEXT.get();
        if (stack == null) {
            stack = new ArrayDeque<>(1);
            TL_NSCONTEXT.set(stack);
        }
        stack.push(context);
    }
}
