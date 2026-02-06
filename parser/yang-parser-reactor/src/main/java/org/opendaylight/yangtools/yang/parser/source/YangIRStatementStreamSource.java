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

import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.ir.IRKeyword;
import org.opendaylight.yangtools.yang.ir.IRStatement;
import org.opendaylight.yangtools.yang.ir.StringEscaping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDeclaration;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.spi.meta.StatementDeclarations;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * A {@link StatementStreamSource} operating based on {@link YangIRSource}.
 */
record YangIRStatementStreamSource(
        @NonNull YangIRSource source,
        @NonNull StringEscaping escaping,
        @NonNull Map<String, QNameModule> prefixToModule) implements StatementStreamSource {
    @NonNullByDefault
    static final Factory<YangIRSource> FACTORY = (source, yangVersion, prefixToModule) ->
        new YangIRStatementStreamSource(source,
            switch (yangVersion) {
                case VERSION_1 -> StringEscaping.RFC6020;
                case VERSION_1_1 -> StringEscaping.RFC7950;
            },
            prefixToModule.entrySet().stream().collect(
                Collectors.toUnmodifiableMap(entry -> entry.getKey().getLocalName(), Map.Entry::getValue)));

    YangIRStatementStreamSource {
        requireNonNull(source);
        requireNonNull(escaping);
        requireNonNull(prefixToModule);
    }

    StatementDeclaration.@NonNull InText refOf(final IRStatement stmt) {
        return StatementDeclarations.inText(source.symbolicName(), stmt.startLine(), stmt.startColumn() + 1);
    }

    /**
     * Returns QNameModule (namespace + revision) associated with supplied prefix.
     *
     * @param prefix Prefix
     * @return QNameModule associated with supplied prefix, or null if prefix is not defined.
     */
    @Nullable QNameModule resolvePrefix(final @NonNull String prefix) {
        return prefixToModule.get(prefix);
    }

    @Override
    public void writePreLinkage(final StatementWriter writer, final StatementDefinitionResolver resolver) {
        new IRStatementVisitor(this, writer, resolver).visit(source.statement());
    }

    @Override
    public void writeLinkage(final StatementWriter writer, final StatementDefinitionResolver resolver) {
        new IRStatementVisitor(this, writer, resolver) {
            @Override
            StatementDefinition<?, ?, ?> resolveStatement(final QNameModule module, final String localName) {
                return resolver.lookupDef(module, localName);
            }
        }.visit(source.statement());
    }

    @Override
    public void writeLinkageAndStatementDefinitions(final StatementWriter writer,
            final StatementDefinitionResolver resolver) {
        new IRStatementVisitor(this, writer, resolver).visit(source.statement());
    }

    @Override
    public void writeFull(final StatementWriter writer, final StatementDefinitionResolver resolver) {
        new IRStatementVisitor(this, writer, resolver) {
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
