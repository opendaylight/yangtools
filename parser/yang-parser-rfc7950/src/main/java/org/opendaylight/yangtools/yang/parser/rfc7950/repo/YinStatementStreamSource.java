/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2026 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import javax.xml.transform.TransformerException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.spi.source.YinDomSource;
import org.opendaylight.yangtools.yang.model.spi.source.YinXmlSource;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixResolver;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementDefinitionResolver;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter;

/**
 * A {@link StatementStreamSource} based on a {@link YinXmlSource}. Internal implementation works on top
 * of {@link YinDomSource} and its DOM document.
 */
@Beta
public final class YinStatementStreamSource implements StatementStreamSource {
    private final @NonNull YinDomSource source;

    private YinStatementStreamSource(final YinDomSource source) {
        this.source = requireNonNull(source);
    }

    public static StatementStreamSource create(final YinXmlSource source) throws TransformerException {
        return create(YinDomSource.transform(source));
    }

    public static StatementStreamSource create(final YinDomSource source) {
        return new YinStatementStreamSource(source);
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
