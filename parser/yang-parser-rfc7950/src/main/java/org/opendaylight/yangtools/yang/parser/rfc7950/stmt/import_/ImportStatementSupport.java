/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.import_;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PrefixEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractUnqualifiedStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class ImportStatementSupport
        extends AbstractUnqualifiedStatementSupport<ImportStatement, ImportEffectiveStatement> {
    private static final SubstatementValidator RFC6020_VALIDATOR = SubstatementValidator.builder(YangStmtMapping.IMPORT)
        .addMandatory(YangStmtMapping.PREFIX)
        .addOptional(YangStmtMapping.REVISION_DATE)
        .build();
    private static final SubstatementValidator RFC7950_VALIDATOR = SubstatementValidator.builder(YangStmtMapping.IMPORT)
        .addMandatory(YangStmtMapping.PREFIX)
        .addOptional(YangStmtMapping.REVISION_DATE)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addOptional(YangStmtMapping.REFERENCE)
        .build();

    private ImportStatementSupport(final YangParserConfiguration config, final SubstatementValidator validator) {
        super(YangStmtMapping.IMPORT, StatementPolicy.reject(), config, validator);
    }

    public static @NonNull ImportStatementSupport rfc6020Instance(final YangParserConfiguration config) {
        return new ImportStatementSupport(config, RFC6020_VALIDATOR);
    }

    public static @NonNull ImportStatementSupport rfc7950Instance(final YangParserConfiguration config) {
        return new ImportStatementSupport(config, RFC7950_VALIDATOR);
    }

    @Override
    protected ImportStatement createDeclared(final BoundStmtCtx<Unqualified> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createImport(ctx.getArgument(), substatements);
    }

    @Override
    protected ImportStatement attachDeclarationReference(final ImportStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateImport(stmt, reference);
    }

    @Override
    protected ImportEffectiveStatement createEffective(final Current<Unqualified, ImportStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        InferenceException.throwIf(substatements.isEmpty(), stmt, "Unexpected empty effective import statement");
        final var resolvedInfo = verifyNotNull(stmt.namespaceItem(ParserNamespaces.RESOLVED_INFO, Empty.value()));
        final SourceIdentifier importedSourceId =
            verifyNotNull(resolvedInfo.imports().get(getPrefix(substatements))).sourceId();
        return EffectiveStatements.createImport(stmt.declared(), substatements, importedSourceId);
    }

    private String getPrefix(final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return substatements.stream()
            .filter(substmt -> substmt instanceof PrefixEffectiveStatement)
            .findFirst()
            .map(prefixStmt -> (String) prefixStmt.argument())
            .orElseThrow();
    }
}
