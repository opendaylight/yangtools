/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2026 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.spi.source.YinDomSource;

/**
 * A {@link StatementStreamSource} based on a {@link YinDomSource}.
 */
public record YinDOMStatementStreamSource(@NonNull YinDomSource source) implements StatementStreamSource {
    public YinDOMStatementStreamSource {
        requireNonNull(source);
    }

    @Override
    public void writePreLinkage(final StatementWriter writer, final StatementDefinitionResolver resolver) {
        YinDOMSourceWalker.walkSource(source, writer, resolver);
    }

    @Override
    public void writeLinkage(final StatementWriter writer, final StatementDefinitionResolver resolver,
            final PrefixResolver preLinkagePrefixes, final YangVersion yangVersion) {
        YinDOMSourceWalker.walkSource(source, writer, resolver);
    }

    @Override
    public void writeLinkageAndStatementDefinitions(final StatementWriter writer,
            final StatementDefinitionResolver resolver, final PrefixResolver prefixes, final YangVersion yangVersion) {
        YinDOMSourceWalker.walkSource(source, writer, resolver);
    }

    @Override
    public void writeFull(final StatementWriter writer, final StatementDefinitionResolver resolver,
            final PrefixResolver prefixes, final YangVersion yangVersion) {
        YinDOMSourceWalker.walkSource(source, writer, resolver);
    }
}
