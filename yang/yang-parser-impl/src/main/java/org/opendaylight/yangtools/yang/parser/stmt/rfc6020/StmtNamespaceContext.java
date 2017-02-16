/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Iterators;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * A {@link NamespaceContext} implementation based on the set of imports and local module namespace.
 *
 * TODO: this is a useful utility, so should probably move to yang.parser.spi.meta.
 */
final class StmtNamespaceContext implements NamespaceContext {
    private final StmtContext<?, ?, ?> ctx;
    private final BiMap<String,String> prefixToURIMap;
    private String localNamespaceURI;

    private StmtNamespaceContext(final StmtContext<?, ?, ?> ctx) {
        this(ctx, ImmutableBiMap.of());
    }

    private StmtNamespaceContext(final StmtContext<?, ?, ?> ctx, final BiMap<String, String> prefixToURIMap) {
        this.ctx = Preconditions.checkNotNull(ctx);
        this.prefixToURIMap = ImmutableBiMap.copyOf(Preconditions.checkNotNull(prefixToURIMap));
    }

    public static NamespaceContext create(final StmtContext<?, ?, ?> ctx) {
        return new StmtNamespaceContext(ctx);
    }

    public static NamespaceContext create(final StmtContext<?, ?, ?> ctx, final BiMap<String,String> prefixToURIMap) {
        return new StmtNamespaceContext(ctx, prefixToURIMap);
    }

    private String localNamespaceURI() {
        if (localNamespaceURI == null) {
            localNamespaceURI = Verify.verifyNotNull(
                ctx.getPublicDefinition().getStatementName().getNamespace().toString(),
                "Local namespace URI not found in %s", ctx);
        }
        return localNamespaceURI;
    }

    @Override
    public String getNamespaceURI(final String prefix) {
        // API-mandated by NamespaceContext
        Preconditions.checkArgument(prefix != null);

        final String uri = prefixToURIMap.get(prefix);
        if(uri != null) {
            return uri;
        }

        if (prefix.isEmpty()) {
            return localNamespaceURI();
        }

        final QNameModule module = Utils.getModuleQNameByPrefix(ctx, prefix);
        return module == null ? null : module.getNamespace().toString();
    }

    @Override
    public String getPrefix(final String namespaceURI) {
        // API-mandated by NamespaceContext
        Preconditions.checkArgument(namespaceURI != null);

        final String prefix = prefixToURIMap.inverse().get(namespaceURI);
        if(prefix != null) {
            return prefix;
        }

        if (localNamespaceURI().equals(namespaceURI)) {
            return "";
        }
        return ctx.getFromNamespace(URIStringToImpPrefix.class, namespaceURI);
    }

    @Override
    public Iterator<String> getPrefixes(final String namespaceURI) {
        // Ensures underlying map remains constant
        final Map<String, String> result = new HashMap<>();
        result.putAll(ctx.getAllFromNamespace(URIStringToImpPrefix.class));
        result.putAll(prefixToURIMap.inverse());
        return Iterators.unmodifiableIterator(
                result.values().iterator());
    }
}
