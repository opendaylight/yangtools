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
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ArgumentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YinElementStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentSyntaxException;
import org.opendaylight.yangtools.yang.model.spi.stmt.IdentifierParser;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementDefinitions;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextNamespaceBinding;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class ExtensionStatementSupport
        extends AbstractQNameStatementSupport<ExtensionStatement, ExtensionEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(YangStmtMapping.EXTENSION)
            .addOptional(YangStmtMapping.ARGUMENT)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addOptional(YangStmtMapping.REFERENCE)
            .addOptional(YangStmtMapping.STATUS)
            .build();

    private final YangParserConfiguration config;

    public ExtensionStatementSupport(final YangParserConfiguration config) {
        super(YangStmtMapping.EXTENSION, StatementPolicy.reject(), config, SUBSTATEMENT_VALIDATOR);
        this.config = requireNonNull(config);
    }

    @Override
    public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        try {
            return new IdentifierParser(new StmtContextNamespaceBinding(ctx.getRoot())).parseArgument(value);
        } catch (ArgumentSyntaxException e) {
            throw SourceException.ofArgumentSyntax(ctx, value, e);
        }
    }

    @Override
    public void onStatementDefinitionDeclared(
            final Mutable<QName, ExtensionStatement, ExtensionEffectiveStatement> stmt) {
        super.onStatementDefinitionDeclared(stmt);

        stmt.addToNs(ParserNamespaces.EXTENSION, stmt.getArgument(), stmt);

        final var argument = StmtContextUtils.findFirstDeclaredSubstatement(stmt, ArgumentStatement.class);
        final var yinElement = StmtContextUtils.findFirstDeclaredSubstatement(stmt, YinElementStatement.class);

        stmt.addToNs(StatementDefinitions.NAMESPACE, stmt.argument(),
            new UnrecognizedStatementSupport(new ModelDefinedStatementDefinition(stmt.getArgument(),
                argument != null ? argument.argument() : null, yinElement != null && yinElement.getArgument()),
                config));
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
