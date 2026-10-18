/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStmtUtils;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

// RFC7950 section 6.2.1:
//        o  All derived type names defined within a parent node or at the top
//           level of the module or its submodules share the same type
//           identifier namespace.  This namespace is scoped to all descendant
//           nodes of the parent node or module.  This means that any
//           descendant node may use that typedef, and it MUST NOT define a
//           typedef with the same name.
public final class TypedefStatementSupport
        extends NestedNamespaceStatementSupport<TypedefStatement, TypedefEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(TypedefStatement.DEF)
            .addOptional(DefaultStatement.DEF)
            .addOptional(DescriptionStatement.DEF)
            .addOptional(ReferenceStatement.DEF)
            .addOptional(StatusStatement.DEF)
            .addMandatory(TypeStatement.DEF)
            .addOptional(UnitsStatement.DEF)
            .build();

    public TypedefStatementSupport(final YangParserConfiguration config) {
        super(TypedefStatement.DEF, StatementPolicy.exactReplica(), SubtreePolicy.normal(), config,
            SUBSTATEMENT_VALIDATOR, ParserNamespaces.TYPE);
    }

    @Override
    SourceException newSourceException(final StmtContext<?, ?, ?> prev, final StmtContext<?, ?, ?> next) {
        // TODO: improve the message with coordinates of conflict
        return new SourceException(next, "Duplicate name for typedef %s", next.getArgument());
    }

    @Override
    protected TypedefStatement createDeclared(final BoundStmtCtx<QName> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createTypedef(ctx.getArgument(), substatements);
    }

    @Override
    protected TypedefStatement attachDeclarationReference(final TypedefStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateTypedef(stmt, reference);
    }

    @Override
    protected TypedefEffectiveStatement createEffective(final Current<QName, TypedefStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final var declared = stmt.declared();
        checkState(!substatements.isEmpty(), "Refusing to create empty typedef for %s", stmt.declared());

        final var typeEffectiveStmt = findFirstStatement(substatements, TypeEffectiveStatement.class);
        final String dflt = findFirstArgument(substatements, DefaultEffectiveStatement.class, null);
        SourceException.throwIf(
            EffectiveStmtUtils.hasDefaultValueMarkedWithIfFeature(stmt.sourceVersion(), typeEffectiveStmt, dflt), stmt,
            "Typedef '%s' has default value '%s' marked with an if-feature statement.", stmt.argument(), dflt);

        return EffectiveStatements.createTypedef(declared, computeFlags(substatements), substatements);
    }

    private static int computeFlags(final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new FlagsBuilder()
            .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
            .toFlags();
    }
}
