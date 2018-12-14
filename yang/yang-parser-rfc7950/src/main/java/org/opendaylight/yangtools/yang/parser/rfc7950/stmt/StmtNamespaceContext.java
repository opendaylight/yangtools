/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Iterators;
import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.parser.rfc7950.namespace.URIStringToImportPrefix;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;

/**
 * A {@link NamespaceContext} implementation based on the set of imports and local module namespace.
 */
// TODO: this is a useful utility, so it may be useful to expose it either in this package, or yang.parser.spi.source.
final class StmtNamespaceContext implements NamespaceContext {
    private final StmtContext<?, ?, ?> ctx;
    private final ImmutableBiMap<String, String> uriToPrefix;

    private String localNamespaceURI;

    private StmtNamespaceContext(final StmtContext<?, ?, ?> ctx) {
        this(ctx, ImmutableBiMap.of());
    }

    private StmtNamespaceContext(final StmtContext<?, ?, ?> ctx, final BiMap<String, String> uriToPrefix) {
        this.ctx = requireNonNull(ctx);
        this.uriToPrefix = ImmutableBiMap.copyOf(uriToPrefix);
    }

    public static NamespaceContext create(final StmtContext<?, ?, ?> ctx) {
        return new StmtNamespaceContext(ctx);
    }

    public static NamespaceContext create(final StmtContext<?, ?, ?> ctx, final BiMap<String, String> uriToPrefix) {
        return new StmtNamespaceContext(ctx, uriToPrefix);
    }

    private String localNamespaceURI() {
        if (localNamespaceURI == null) {
            localNamespaceURI = verifyNotNull(ctx.getPublicDefinition().getStatementName().getNamespace().toString(),
                "Local namespace URI not found in %s", ctx);
        }
        return localNamespaceURI;
    }

    @Override
    public String getNamespaceURI(final String prefix) {
        // API-mandated by NamespaceContext
        checkArgument(prefix != null);

        final String uri = uriToPrefix.inverse().get(prefix);
        if (uri != null) {
            return uri;
        }

        if (prefix.isEmpty()) {
            return localNamespaceURI();
        }

        final QNameModule module = StmtContextUtils.getModuleQNameByPrefix(ctx, prefix);
        return module == null ? null : module.getNamespace().toString();
    }

    @Override
    public String getPrefix(final String namespaceURI) {
        // API-mandated by NamespaceContext
        checkArgument(namespaceURI != null);

        final String prefix = uriToPrefix.get(namespaceURI);
        if (prefix != null) {
            return prefix;
        }

        if (localNamespaceURI().equals(namespaceURI)) {
            return "";
        }
        return ctx.getFromNamespace(URIStringToImportPrefix.class, namespaceURI);
    }

    @Override
    public Iterator<String> getPrefixes(final String namespaceURI) {
        // Ensures underlying map remains constant
        return Iterators.unmodifiableIterator(Iterators.concat(
                ctx.getAllFromNamespace(URIStringToImportPrefix.class).values().iterator(),
                uriToPrefix.values().iterator()));
    }
}
