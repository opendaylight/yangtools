/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.extension;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ArgumentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementDefinitions;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class ExtensionStatementSupport
        extends AbstractQNameStatementSupport<ExtensionStatement, ExtensionEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(ExtensionStatement.DEF)
            .addOptional(ArgumentStatement.DEF)
            .addOptional(DescriptionStatement.DEF)
            .addOptional(ReferenceStatement.DEF)
            .addOptional(StatusStatement.DEF)
            .build();

    private final @NonNull YangParserConfiguration config;

    public ExtensionStatementSupport(final YangParserConfiguration config) {
        super(ExtensionStatement.DEF, StatementPolicy.reject(), config, SUBSTATEMENT_VALIDATOR);
        this.config = requireNonNull(config);
    }

    @Override
    public void onStatementAdded(final Mutable<QName, ExtensionStatement, ExtensionEffectiveStatement> stmt) {
        // FIXME: 17.0.0: setUnsupported() if there is no corresponding reactor support, mirroring how we project
        //                supported features. That has impact on SchemaContext.getExtensions(): we probably want
        //                to deprecate ExtensionDefinition as such as the first step towards full SchemaNode deprecation
    }

    @Override
    public void onStatementDefinitionDeclared(
            final Mutable<QName, ExtensionStatement, ExtensionEffectiveStatement> stmt) {
        // RFC7950 section 6.2.1:
        //        o  All extension names defined in a module and its submodules share
        //           the same extension identifier namespace.
        if (!stmt.coerceParentContext().producesAnyOf(ModuleStatement.DEF, SubmoduleStatement.DEF)) {
            return;
        }

        final var name = stmt.getArgument();
        final var prev = stmt.namespaceItem(ParserNamespaces.EXTENSION, name);
        if (prev != null) {
            throw new SourceException(stmt,
                "Extension name collision: '%s' already declared at %s", name, prev.sourceReference());
        }
        stmt.addToNs(ParserNamespaces.EXTENSION, name, stmt);
        stmt.addToNs(StatementDefinitions.NAMESPACE, name, UnrecognizedStatementSupport.ofExtension(stmt, config));
    }

    @Override
    protected ExtensionStatement createDeclared(final BoundStmtCtx<QName> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createExtension(ctx.getArgument(), substatements);
    }

    @Override
    protected ExtensionStatement attachDeclarationReference(final ExtensionStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateExtesion(stmt, reference);
    }

    @Override
    protected ExtensionEffectiveStatement createEffective(final Current<QName, ExtensionStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createExtension(stmt.declared(), substatements);
    }
}
