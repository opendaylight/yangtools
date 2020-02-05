/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.typedef;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStmtUtils;
import org.opendaylight.yangtools.yang.parser.spi.TypeNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class TypedefStatementSupport extends
        BaseQNameStatementSupport<TypedefStatement, TypedefEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(
        YangStmtMapping.TYPEDEF)
        .addOptional(YangStmtMapping.DEFAULT)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addOptional(YangStmtMapping.REFERENCE)
        .addOptional(YangStmtMapping.STATUS)
        .addMandatory(YangStmtMapping.TYPE)
        .addOptional(YangStmtMapping.UNITS)
        .build();
    private static final TypedefStatementSupport INSTANCE = new TypedefStatementSupport();

    private TypedefStatementSupport() {
        super(YangStmtMapping.TYPEDEF);
    }

    public static TypedefStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.parseIdentifier(ctx, value);
    }

    @Override
    public void onFullDefinitionDeclared(final Mutable<QName, TypedefStatement, TypedefEffectiveStatement> stmt) {
        super.onFullDefinitionDeclared(stmt);

        if (stmt != null) {
            final Mutable<?, ?, ?> parent = stmt.getParentContext();
            if (parent != null) {
                // Shadowing check: make sure we do not trample on pre-existing definitions. This catches sibling
                // declarations and parent declarations which have already been declared.
                checkConflict(parent, stmt);
                parent.addContext(TypeNamespace.class, stmt.coerceStatementArgument(), stmt);
            }
        }
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected TypedefStatement createDeclared(final StmtContext<QName, TypedefStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        checkDeclared(ctx);
        return new RegularTypedefStatement(ctx.coerceStatementArgument(), substatements);
    }

    @Override
    protected TypedefStatement createEmptyDeclared(final StmtContext<QName, TypedefStatement, ?> ctx) {
        checkDeclared(ctx);
        return new EmptyTypedefStatement(ctx.coerceStatementArgument());
    }

    @Override
    protected TypedefEffectiveStatement createEffective(
            final StmtContext<QName, TypedefStatement, TypedefEffectiveStatement> ctx,
            final TypedefStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final TypeEffectiveStatement<?> typeEffectiveStmt = findFirstStatement(substatements,
            TypeEffectiveStatement.class);
        final String dflt = findFirstArgument(substatements, DefaultEffectiveStatement.class, null);
        SourceException.throwIf(
            EffectiveStmtUtils.hasDefaultValueMarkedWithIfFeature(ctx.getRootVersion(), typeEffectiveStmt, dflt),
            ctx.getStatementSourceReference(),
            "Typedef '%s' has default value '%s' marked with an if-feature statement.", ctx.getStatementArgument(),
            dflt);

        return new TypedefEffectiveStatementImpl(declared, ctx.getSchemaPath().get(), computeFlags(substatements),
            substatements);
    }

    @Override
    protected TypedefEffectiveStatement createEmptyEffective(
            final StmtContext<QName, TypedefStatement, TypedefEffectiveStatement> ctx,
            final TypedefStatement declared) {
        throw new IllegalStateException("Refusing to create empty typedef for " + declared);
    }

    private static void checkConflict(final StmtContext<?, ?, ?> parent, final StmtContext<QName, ?, ?> stmt) {
        final QName arg = stmt.coerceStatementArgument();
        final StmtContext<?, ?, ?> existing = parent.getFromNamespace(TypeNamespace.class, arg);
        // RFC7950 sections 5.5 and 6.2.1: identifiers must not be shadowed
        SourceException.throwIf(existing != null, stmt.getStatementSourceReference(), "Duplicate name for typedef %s",
                arg);
    }

    private static void checkDeclared(final StmtContext<QName, TypedefStatement, ?> ctx) {
        // Shadowing check: make sure grandparent does not see a conflicting definition. This is required to ensure
        // that a typedef in child scope does not shadow a typedef in parent scope which occurs later in the text.
        final StmtContext<?, ?, ?> parent = ctx.getParentContext();
        if (parent != null) {
            final StmtContext<?, ?, ?> grandParent = parent.getParentContext();
            if (grandParent != null) {
                checkConflict(grandParent, ctx);
            }
        }
    }

    private static int computeFlags(final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new FlagsBuilder()
                .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
                .toFlags();
    }
}