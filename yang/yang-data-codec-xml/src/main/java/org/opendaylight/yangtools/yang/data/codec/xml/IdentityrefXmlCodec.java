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
import java.util.Iterator;
import java.util.Map.Entry;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.util.codec.IdentityCodecUtil;
import org.opendaylight.yangtools.yang.data.util.codec.QNameCodecUtil;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;

final class IdentityrefXmlCodec implements XmlCodec<QName> {
    private final EffectiveModelContext schemaContext;
    private final QNameModule parentModule;

    IdentityrefXmlCodec(final EffectiveModelContext context, final QNameModule parentModule) {
        this.schemaContext = requireNonNull(context);
        this.parentModule = requireNonNull(parentModule);
    }

    @Override
    public Class<QName> getDataType() {
        return QName.class;
    }

    @Override
    public QName parseValue(final NamespaceContext ctx, final String str) {
        return IdentityCodecUtil.parseIdentity(str, schemaContext, prefix -> {
            if (prefix.isEmpty()) {
                return parentModule;
            }

            final String prefixedNS = ctx.getNamespaceURI(prefix);
            final Iterator<? extends Module> modules = schemaContext.findModules(URI.create(prefixedNS)).iterator();
            checkArgument(modules.hasNext(), "Could not find module for namespace %s", prefixedNS);
            return modules.next().getQNameModule();
        }).getQName();
    }

    @Override
    public void writeValue(final XMLStreamWriter ctx, final QName value) throws XMLStreamException {
        final RandomPrefix prefixes = new RandomPrefix(ctx.getNamespaceContext());
        final String str = QNameCodecUtil.encodeQName(value, uri -> prefixes.encodePrefix(uri.getNamespace()));

        for (Entry<URI, String> e : prefixes.getPrefixes()) {
            ctx.writeNamespace(e.getValue(), e.getKey().toString());
        }
        ctx.writeCharacters(str);
    }
}
