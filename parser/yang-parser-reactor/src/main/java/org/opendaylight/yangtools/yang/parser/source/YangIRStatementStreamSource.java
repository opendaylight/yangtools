/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
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
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.ir.IRKeyword;
import org.opendaylight.yangtools.yang.ir.StringEscaping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixResolver;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * A {@link StatementStreamSource} operating based on {@link YangIRSource}.
 */
record YangIRStatementStreamSource(
        @NonNull YangIRSource source,
        @NonNull StringEscaping escaping) implements StatementStreamSource {

    @NonNullByDefault
    static final Factory<YangIRSource> FACTORY = (source, yangVersion) -> new YangIRStatementStreamSource(source,
        switch (yangVersion) {
            case VERSION_1 -> StringEscaping.RFC6020;
            case VERSION_1_1 -> StringEscaping.RFC7950;
        });

    YangIRStatementStreamSource {
        requireNonNull(source);
    }

    private String symbolicName() {
        return source.symbolicName();
    }

    @Override
    public void writePreLinkage(final StatementWriter writer, final StatementDefinitionResolver resolver) {
        new IRStatementVisitor(escaping, symbolicName(), writer, resolver, null).visit(source.statement());
    }

    @Override
    public void writeLinkage(final StatementWriter writer, final StatementDefinitionResolver resolver,
            final PrefixResolver preLinkagePrefixes) {
        new IRStatementVisitor(escaping, symbolicName(), writer, resolver, preLinkagePrefixes) {
            @Override
            StatementDefinition<?, ?, ?> resolveStatement(final QNameModule module, final String localName) {
                return resolver.lookupDef(module, localName);
            }
        }.visit(source.statement());
    }

    @Override
    public void writeLinkageAndStatementDefinitions(final StatementWriter writer,
            final StatementDefinitionResolver resolver, final PrefixResolver prefixes) {
        new IRStatementVisitor(escaping, symbolicName(), writer, resolver, prefixes).visit(source.statement());
    }

    @Override
    public void writeFull(final StatementWriter writer, final StatementDefinitionResolver resolver,
            final PrefixResolver prefixes) {
        new IRStatementVisitor(escaping, symbolicName(), writer, resolver, prefixes) {
            @Override
            QName getValidStatementDefinition(final IRKeyword keyword, final StatementSourceReference ref) {
                final var ret = super.getValidStatementDefinition(keyword, ref);
                if (ret == null) {
                    throw new SourceException(ref, "%s is not a YANG statement or use of extension.",
                        keyword.asStringDeclaration());
                }
                return ret;
            }
        }.visit(source.statement());
    }
}
