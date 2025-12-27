/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.parser;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.rfc7952.model.api.AnnotationEffectiveStatement;
import org.opendaylight.yangtools.rfc7952.model.api.AnnotationStatement;
import org.opendaylight.yangtools.rfc7952.model.api.MetadataStatements;
import org.opendaylight.yangtools.yang.common.AnnotationName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class AnnotationStatementSupport
         extends AbstractStatementSupport<AnnotationName, AnnotationStatement, AnnotationEffectiveStatement> {
    private static final SubstatementValidator VALIDATOR = SubstatementValidator.builder(MetadataStatements.ANNOTATION)
        .addMandatory(YangStmtMapping.TYPE)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addAny(YangStmtMapping.IF_FEATURE)
        .addOptional(YangStmtMapping.REFERENCE)
        .addOptional(YangStmtMapping.STATUS)
        .addOptional(YangStmtMapping.UNITS)
        .build();

    public AnnotationStatementSupport(final YangParserConfiguration config) {
        super(MetadataStatements.ANNOTATION, StatementPolicy.reject(), config, VALIDATOR);
    }

    @Override
    public AnnotationName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return new AnnotationName(ctx.parseIdentifier(value)).intern();
    }

    @Override
    public AnnotationName adaptArgumentValue(
            final StmtContext<AnnotationName, AnnotationStatement, AnnotationEffectiveStatement> ctx,
            final QNameModule targetModule) {
        return new AnnotationName(ctx.getArgument().qname().bindTo(targetModule)).intern();
    }

    @Override
    public void onStatementAdded(
            final Mutable<AnnotationName, AnnotationStatement, AnnotationEffectiveStatement> stmt) {
        final var parent = stmt.coerceParentContext().publicDefinition().getDeclaredRepresentationClass();
        if (!ModuleStatement.class.isAssignableFrom(parent) && !SubmoduleStatement.class.isAssignableFrom(parent)) {
            throw new SourceException(stmt,
                "Annotations may only be defined at root of either a module or a submodule");
        }
    }

    @Override
    protected AnnotationStatement createDeclared(final BoundStmtCtx<AnnotationName> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return new AnnotationStatementImpl(ctx.getArgument(), substatements);
    }

    @Override
    protected AnnotationStatement attachDeclarationReference(final AnnotationStatement stmt,
            final DeclarationReference reference) {
        return new RefAnnotationStatement(stmt, reference);
    }

    @Override
    protected AnnotationEffectiveStatement createEffective(final Current<AnnotationName, AnnotationStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new AnnotationEffectiveStatementImpl(stmt, substatements);
    }
}
