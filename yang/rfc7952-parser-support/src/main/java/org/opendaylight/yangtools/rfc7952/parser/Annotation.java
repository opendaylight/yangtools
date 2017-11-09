/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.parser;

import org.opendaylight.yangtools.rfc7952.model.api.AnnotationEffectiveStatement;
import org.opendaylight.yangtools.rfc7952.model.api.AnnotationStatement;
import org.opendaylight.yangtools.rfc7952.model.api.MetadataStatements;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsStatement;
import org.opendaylight.yangtools.yang.model.util.type.ConcreteTypeBuilder;
import org.opendaylight.yangtools.yang.model.util.type.ConcreteTypes;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.UnknownEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

final class Annotation extends AbstractStatementSupport<String, AnnotationStatement, AnnotationEffectiveStatement> {

    private static final class Declared extends AbstractDeclaredStatement<String> implements AnnotationStatement {
        Declared(final StmtContext<String, ?, ?> context) {
            super(context);
        }

        @Override
        public String getArgument() {
            return argument();
        }
    }

    private static final class Effective extends UnknownEffectiveStatementBase<String, AnnotationStatement>
            implements AnnotationEffectiveStatement {

        private final TypeDefinition<?> type;
        private final SchemaPath path;

        Effective(final StmtContext<String, AnnotationStatement, ?> ctx) {
            super(ctx);
            path = ctx.getParentContext().getSchemaPath().get().createChild(
                StmtContextUtils.qnameFromArgument(ctx, argument()));

            final TypeEffectiveStatement<?> typeStmt = SourceException.throwIfNull(
                firstSubstatementOfType(TypeEffectiveStatement.class), ctx.getStatementSourceReference(),
                "Annotation %s is missing a 'type' statement", argument());

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
        public SchemaPath getPath() {
            return path;
        }

        @Override
        public TypeDefinition<?> getType() {
            return type;
        }

        @Override
        public boolean isConfiguration() {
            return false;
        }

        @Override
        public ConstraintDefinition getConstraints() {
            return null;
        }

        @Override
        public boolean isAugmenting() {
            return false;
        }
    }

    private static final Annotation INSTANCE = new Annotation(MetadataStatements.ANNOTATION);

    private final SubstatementValidator validator;

    Annotation(final StatementDefinition definition) {
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

    static Annotation getInstance() {
        return INSTANCE;
    }

    @Override
    public AnnotationStatement createDeclared(final StmtContext<String, AnnotationStatement, ?> ctx) {
        return new Declared(ctx);
    }

    @Override
    public AnnotationEffectiveStatement createEffective(
            final StmtContext<String, AnnotationStatement, AnnotationEffectiveStatement> ctx) {
        return new Effective(ctx);
    }

    @Override
    public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        // FIXME: validate this is in fact an identifier as per RFC7950 Section 6.2
        return value;
    }

    @Override
    public void onStatementAdded(final Mutable<String, AnnotationStatement, AnnotationEffectiveStatement> stmt) {
        final StatementDefinition parentDef = stmt.getParentContext().getPublicDefinition();
        SourceException.throwIf(YangStmtMapping.MODULE != parentDef && YangStmtMapping.SUBMODULE != parentDef,
                stmt.getStatementSourceReference(),
                "Annotations may only be defined at root of either a module or a submodule");
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return validator;
    }
}
