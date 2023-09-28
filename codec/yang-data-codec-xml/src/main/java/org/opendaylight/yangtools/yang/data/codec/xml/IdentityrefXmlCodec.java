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

import com.google.common.base.MoreObjects;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.util.codec.IdentityCodecUtil;
import org.opendaylight.yangtools.yang.data.util.codec.QNameCodecUtil;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

final class IdentityrefXmlCodec implements XmlCodec<QName> {
    private final @NonNull EffectiveModelContext context;
    private final @NonNull PreferredPrefixes pref;
    private final @NonNull QNameModule parentModule;

    IdentityrefXmlCodec(final EffectiveModelContext context, final PreferredPrefixes pref,
            final QNameModule parentModule) {
        this.context = requireNonNull(context);
        this.pref = requireNonNull(pref);
        this.parentModule = requireNonNull(parentModule);
    }

    @Override
    public Class<QName> getDataType() {
        return QName.class;
    }

    @Override
    public QName parseValue(final NamespaceContext ctx, final String str) {
        // FIXME: YANGTOOLS-1523: do not trim()
        return IdentityCodecUtil.parseIdentity(str.trim(), context, prefix -> {
            if (prefix.isEmpty()) {
                return parentModule;
            }

            final var prefixedNS = ctx.getNamespaceURI(prefix);
            checkArgument(prefixedNS != null, "Failed to resolve prefix %s", prefix);

            final var modules = context.findModuleStatements(XMLNamespace.of(prefixedNS)).iterator();
            checkArgument(modules.hasNext(), "Could not find module for namespace %s", prefixedNS);
            return modules.next().localQNameModule();
        }).getQName();
    }

    @Override
    public void writeValue(final XMLStreamWriter ctx, final QName value) throws XMLStreamException {
        final var prefixes = new NamespacePrefixes(pref, ctx.getNamespaceContext());
        final var str = QNameCodecUtil.encodeQName(value, uri -> prefixes.encodePrefix(uri.getNamespace()));

        for (var e : prefixes.emittedPrefixes()) {
            ctx.writeNamespace(e.getValue(), e.getKey().toString());
        }
        ctx.writeCharacters(str);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("module", parentModule).toString();
    }
}
