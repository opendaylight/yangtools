/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.parser;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.rfc7952.model.api.AnnotationEffectiveStatement;
import org.opendaylight.yangtools.rfc7952.model.api.AnnotationSchemaNode;
import org.opendaylight.yangtools.rfc7952.model.api.AnnotationStatement;
import org.opendaylight.yangtools.rfc7952.model.api.MetadataStatements;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaNodeDefaults;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.type.ConcreteTypeBuilder;
import org.opendaylight.yangtools.yang.model.ri.type.ConcreteTypes;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredStatement.WithQNameArgument.WithSubstatements;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.UnknownEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.SchemaPathSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class AnnotationStatementSupport
         extends AbstractStatementSupport<QName, AnnotationStatement, AnnotationEffectiveStatement> {

    private static final class Declared extends WithSubstatements implements AnnotationStatement {
        Declared(final QName argument, final ImmutableList<? extends DeclaredStatement<?>> substatements) {
            super(argument, substatements);
        }
    }

    private static final class Effective extends UnknownEffectiveStatementBase<QName, AnnotationStatement>
            implements AnnotationEffectiveStatement, AnnotationSchemaNode {

        private final @NonNull TypeDefinition<?> type;
        private final @Nullable SchemaPath path;

        Effective(final Current<QName, AnnotationStatement> stmt,
                  final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
            super(stmt, substatements);
            final QName qname = stmt.getArgument();

            // FIXME: move this into onFullDefinitionDeclared()
            final TypeEffectiveStatement<?> typeStmt = SourceException.throwIfNull(
                firstSubstatementOfType(TypeEffectiveStatement.class), stmt,
                "AnnotationStatementSupport %s is missing a 'type' statement", qname);

            final ConcreteTypeBuilder<?> builder = ConcreteTypes.concreteTypeBuilder(typeStmt.getTypeDefinition(),
                qname);
            final UnitsEffectiveStatement unitsStmt = firstSubstatementOfType(UnitsEffectiveStatement.class);
            if (unitsStmt != null) {
                builder.setUnits(unitsStmt.argument());
            }
            type = builder.build();

            path = SchemaPathSupport.toOptionalPath(stmt.getEffectiveParent().getSchemaPath().createChild(qname));
        }

        @Override
        public QName getQName() {
            return verifyNotNull(argument());
        }

        @Override
        @Deprecated
        public SchemaPath getPath() {
            return SchemaNodeDefaults.throwUnsupportedIfNull(this, path);
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

    private static final SubstatementValidator VALIDATOR = SubstatementValidator.builder(MetadataStatements.ANNOTATION)
        .addMandatory(YangStmtMapping.TYPE)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addAny(YangStmtMapping.IF_FEATURE)
        .addOptional(YangStmtMapping.REFERENCE)
        .addOptional(YangStmtMapping.STATUS)
        .addOptional(YangStmtMapping.UNITS)
        .build();

    public AnnotationStatementSupport(final YangParserConfiguration config) {
        super(MetadataStatements.ANNOTATION, StatementPolicy.reject(), config);
    }

    @Override
    public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.parseIdentifier(ctx, value);
    }

    @Override
    public void onStatementAdded(final Mutable<QName, AnnotationStatement, AnnotationEffectiveStatement> stmt) {
        final StatementDefinition parentDef = stmt.coerceParentContext().publicDefinition();
        SourceException.throwIf(YangStmtMapping.MODULE != parentDef && YangStmtMapping.SUBMODULE != parentDef,
                stmt, "Annotations may only be defined at root of either a module or a submodule");
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return VALIDATOR;
    }

    @Override
    protected AnnotationStatement createDeclared(final StmtContext<QName, AnnotationStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new Declared(ctx.getArgument(), substatements);
    }

    @Override
    protected AnnotationEffectiveStatement createEffective(final Current<QName, AnnotationStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new Effective(stmt, substatements);
    }
}
