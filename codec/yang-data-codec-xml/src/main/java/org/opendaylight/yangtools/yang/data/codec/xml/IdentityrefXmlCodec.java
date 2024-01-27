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
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.util.codec.IdentityCodecUtil;
import org.opendaylight.yangtools.yang.data.util.codec.QNameCodecUtil;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

final class IdentityrefXmlCodec implements XmlCodec<QName> {
    private final @NonNull EffectiveModelContext modelContext;
    private final @NonNull QNameModule parentModule;
    private final @Nullable PreferredPrefixes pref;

    IdentityrefXmlCodec(final EffectiveModelContext modelContext, final QNameModule parentModule,
            final @Nullable PreferredPrefixes pref) {
        this.modelContext = requireNonNull(modelContext);
        this.parentModule = requireNonNull(parentModule);
        this.pref = pref;
    }

    @Override
    public Class<QName> getDataType() {
        return QName.class;
    }

    @Override
    public QName parseValue(final NamespaceContext ctx, final String str) {
        return IdentityCodecUtil.parseIdentity(str, modelContext, prefix -> {
            if (prefix.isEmpty()) {
                return parentModule;
            }

            final var prefixedNS = ctx.getNamespaceURI(prefix);
            checkArgument(prefixedNS != null, "Failed to resolve prefix %s", prefix);

            final var modules = modelContext.findModuleStatements(XMLNamespace.of(prefixedNS)).iterator();
            checkArgument(modules.hasNext(), "Could not find module for namespace %s", prefixedNS);
            return modules.next().localQNameModule();
        }).getQName();
    }

    @Override
    public void writeValue(final XMLStreamWriter ctx, final QName value) throws XMLStreamException {
        final var prefixes = new NamespacePrefixes(ctx.getNamespaceContext(), pref);
        final var str = QNameCodecUtil.encodeQName(value, uri -> prefixes.encodePrefix(uri.namespace()));

        for (var entry : prefixes.emittedPrefixes()) {
            ctx.writeNamespace(entry.getValue(), entry.getKey().toString());
        }
        ctx.writeCharacters(str);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("module", parentModule).toString();
    }
}
