/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.parser;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc7952.model.api.AnnotationEffectiveStatement;
import org.opendaylight.yangtools.rfc7952.model.api.AnnotationSchemaNode;
import org.opendaylight.yangtools.rfc7952.model.api.AnnotationStatement;
import org.opendaylight.yangtools.rfc7952.model.api.MetadataStatements;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsStatement;
import org.opendaylight.yangtools.yang.model.util.type.ConcreteTypeBuilder;
import org.opendaylight.yangtools.yang.model.util.type.ConcreteTypes;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredStatement.WithQNameArgument.WithSubstatements;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.UnknownEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class AnnotationStatementSupport
        extends BaseStatementSupport<QName, AnnotationStatement, AnnotationEffectiveStatement> {

    private static final class Declared extends WithSubstatements implements AnnotationStatement {
        Declared(final QName argument, final ImmutableList<? extends DeclaredStatement<?>> substatements) {
            super(argument, substatements);
        }
    }

    private static final class Effective extends UnknownEffectiveStatementBase<QName, AnnotationStatement>
            implements AnnotationEffectiveStatement, AnnotationSchemaNode {

        private final @NonNull TypeDefinition<?> type;
        private final @NonNull SchemaPath path;

        Effective(final StmtContext<QName, AnnotationStatement, ?> ctx,
                final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
            super(ctx, substatements);
            path = ctx.coerceParentContext().getSchemaPath().get().createChild(argument());

            final TypeEffectiveStatement<?> typeStmt = SourceException.throwIfNull(
                firstSubstatementOfType(TypeEffectiveStatement.class), ctx.getStatementSourceReference(),
                "AnnotationStatementSupport %s is missing a 'type' statement", argument());

            final ConcreteTypeBuilder<?> builder = ConcreteTypes.concreteTypeBuilder(typeStmt.getTypeDefinition(),
                path);
            final StmtContext<String, ?, ?> unitsStmt = StmtContextUtils.findFirstEffectiveSubstatement(ctx,
                UnitsStatement.class);
            if (unitsStmt != null) {
                builder.setUnits(unitsStmt.getStatementArgument());
            }
            type = builder.build();
        }

        @Override
        public QName getQName() {
            return path.getLastComponent();
        }

        @Override
        @Deprecated
        public SchemaPath getPath() {
            return path;
        }

        @Override
        public TypeDefinition<?> getType() {
            return type;
        }

        @Override
        public TypeDefinition<?> getTypeDefinition() {
            return type;
        }

        @Override
        public AnnotationEffectiveStatement asEffectiveStatement() {
            return this;
        }
    }

    private static final AnnotationStatementSupport INSTANCE = new AnnotationStatementSupport(
        MetadataStatements.ANNOTATION);

    private final SubstatementValidator validator;

    AnnotationStatementSupport(final StatementDefinition definition) {
        super(definition);
        this.validator = SubstatementValidator.builder(definition)
                .addMandatory(YangStmtMapping.TYPE)
                .addOptional(YangStmtMapping.DESCRIPTION)
                .addAny(YangStmtMapping.IF_FEATURE)
                .addOptional(YangStmtMapping.REFERENCE)
                .addOptional(YangStmtMapping.STATUS)
                .addOptional(YangStmtMapping.UNITS)
                .build();
    }

    public static AnnotationStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.parseIdentifier(ctx, value);
    }

    @Override
    public void onStatementAdded(final Mutable<QName, AnnotationStatement, AnnotationEffectiveStatement> stmt) {
        final StatementDefinition parentDef = stmt.coerceParentContext().getPublicDefinition();
        SourceException.throwIf(YangStmtMapping.MODULE != parentDef && YangStmtMapping.SUBMODULE != parentDef,
                stmt.getStatementSourceReference(),
                "Annotations may only be defined at root of either a module or a submodule");
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return validator;
    }

    @Override
    protected AnnotationStatement createDeclared(final StmtContext<QName, AnnotationStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new Declared(ctx.coerceStatementArgument(), substatements);
    }

    @Override
    protected AnnotationStatement createEmptyDeclared(final StmtContext<QName, AnnotationStatement, ?> ctx) {
        return createDeclared(ctx, ImmutableList.of());
    }

    @Override
    protected AnnotationEffectiveStatement createEffective(
            final StmtContext<QName, AnnotationStatement, AnnotationEffectiveStatement> ctx,
            final AnnotationStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new Effective(ctx, substatements);
    }

    @Override
    protected AnnotationEffectiveStatement createEmptyEffective(
            final StmtContext<QName, AnnotationStatement, AnnotationEffectiveStatement> ctx,
            final AnnotationStatement declared) {
        return new Effective(ctx, ImmutableList.of());
    }
}
