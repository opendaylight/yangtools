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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.spi.source.YinDOMSource;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixResolver;

/**
 * A {@link StatementStreamSource} based on a {@link YinDOMSource}.
 */
record YinDOMStatementStreamSource(@NonNull YinDOMSource source) implements StatementStreamSource {
    @NonNullByDefault
    static final Factory<YinDOMSource> FACTORY = (source, unused) -> new YinDOMStatementStreamSource(source);

    YinDOMStatementStreamSource {
        requireNonNull(source);
    }

    @Override
    public void writeRoot(StatementWriter writer, StatementDefinitionResolver resolver) {
        YinDOMSourceWalker.visitRoot(source, writer, resolver);
    }

    @Override
    public void writeLinkageAndStatementDefinitions(final StatementWriter writer,
            final StatementDefinitionResolver resolver, final PrefixResolver prefixes) {
        YinDOMSourceWalker.skipRootAndWalkSource(source, writer, resolver);
    }

    @Override
    public void writeFull(final StatementWriter writer, final StatementDefinitionResolver resolver,
            final PrefixResolver prefixes) {
        YinDOMSourceWalker.walkSource(source, writer, resolver);
    }
}
