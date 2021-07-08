/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.parser;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc7952.model.api.AnnotationEffectiveStatement;
import org.opendaylight.yangtools.rfc7952.model.api.AnnotationSchemaNode;
import org.opendaylight.yangtools.rfc7952.model.api.AnnotationStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.type.ConcreteTypeBuilder;
import org.opendaylight.yangtools.yang.model.ri.type.ConcreteTypes;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.UnknownEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

final class AnnotationEffectiveStatementImpl extends UnknownEffectiveStatementBase<QName, AnnotationStatement>
        implements AnnotationEffectiveStatement, AnnotationSchemaNode {
    private final @NonNull TypeDefinition<?> type;

    AnnotationEffectiveStatementImpl(final Current<QName, AnnotationStatement> stmt,
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
    }

    @Override
    public QName getQName() {
        return verifyNotNull(argument());
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