/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2026 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.source;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.spi.source.YinDOMSource;

/**
 * A {@link StatementStreamSource} based on a {@link YinDOMSource}.
 */
record YinDOMStatementStreamSource(@NonNull YinDOMSource source) implements StatementStreamSource {
    @NonNullByDefault
    static final Support<YinDOMSource> SUPPORT = (source, yangVersion) -> new YinDOMFactory(source);

    @NonNullByDefault
    private record YinDOMFactory(YinDOMSource source) implements Factory {
        YinDOMFactory {
            requireNonNull(source);
        }

        @Override
        public StatementStreamSource newStreamSource(
                final Map<? extends Unqualified, ? extends QNameModule> prefixToModule) {
            return new YinDOMStatementStreamSource(source);
        }
    }

    YinDOMStatementStreamSource {
        requireNonNull(source);
    }

    @Override
    public void writePreLinkage(final StatementWriter writer, final StatementDefinitionResolver resolver) {
        YinDOMSourceWalker.walkSource(source, writer, resolver);
    }

    @Override
    public void writeLinkage(final StatementWriter writer, final StatementDefinitionResolver resolver) {
        YinDOMSourceWalker.walkSource(source, writer, resolver);
    }

    @Override
    public void writeLinkageAndStatementDefinitions(final StatementWriter writer,
            final StatementDefinitionResolver resolver) {
        YinDOMSourceWalker.walkSource(source, writer, resolver);
    }

    @Override
    public void writeFull(final StatementWriter writer, final StatementDefinitionResolver resolver) {
        YinDOMSourceWalker.walkSource(source, writer, resolver);
    }
}
