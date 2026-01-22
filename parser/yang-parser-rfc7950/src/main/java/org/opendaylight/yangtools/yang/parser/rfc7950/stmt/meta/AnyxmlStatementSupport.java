/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import static com.google.common.base.Verify.verify;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MustStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractSchemaTreeStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStatementState;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.QNameWithFlagsEffectiveStatementState;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class AnyxmlStatementSupport
        extends AbstractSchemaTreeStatementSupport<AnyxmlStatement, AnyxmlEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR =
        SubstatementValidator.builder(YangStmtMapping.ANYXML)
            .addOptional(ConfigStatement.DEFINITION)
            .addOptional(DescriptionStatement.DEFINITION)
            .addAny(IfFeatureStatement.DEFINITION)
            .addOptional(MandatoryStatement.DEFINITION)
            .addAny(MustStatement.DEFINITION)
            .addOptional(ReferenceStatement.DEFINITION)
            .addOptional(StatusStatement.DEFINITION)
            .addOptional(WhenStatement.DEFINITION)
            .build();

    public AnyxmlStatementSupport(final YangParserConfiguration config) {
        super(YangStmtMapping.ANYXML, instantiatedPolicy(), config, SUBSTATEMENT_VALIDATOR);
    }

    @Override
    protected AnyxmlStatement createDeclared(final BoundStmtCtx<QName> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createAnyxml(ctx.getArgument(), substatements);
    }

    @Override
    protected AnyxmlStatement attachDeclarationReference(final AnyxmlStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateAnyxml(stmt, reference);
    }

    @Override
    protected AnyxmlEffectiveStatement createEffective(final Current<QName, AnyxmlStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return EffectiveStatements.createAnyxml(stmt.declared(), stmt.getArgument(), createFlags(stmt, substatements),
            substatements);
    }

    @Override
    public AnyxmlEffectiveStatement copyEffective(final Current<QName, AnyxmlStatement> stmt,
            final AnyxmlEffectiveStatement original) {
        return EffectiveStatements.copyAnyxml(original, stmt.getArgument(),
            createFlags(stmt, original.effectiveSubstatements()));
    }

    @Override
    public EffectiveStatementState extractEffectiveState(final AnyxmlEffectiveStatement stmt) {
        verify(stmt instanceof AnyxmlSchemaNode, "Unexpected statement %s", stmt);
        final var schema = (AnyxmlSchemaNode) stmt;
        return new QNameWithFlagsEffectiveStatementState(stmt.argument(), new FlagsBuilder()
            .setHistory(schema)
            .setStatus(schema.getStatus())
            .setConfiguration(schema.effectiveConfig().orElse(null))
            .setMandatory(schema.isMandatory())
            .toFlags());
    }

    private static int createFlags(final Current<QName, AnyxmlStatement> stmt,
            final Collection<? extends EffectiveStatement<?, ?>> substatements) {
        return new FlagsBuilder()
            .setHistory(stmt.history())
            .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
            .setConfiguration(stmt.effectiveConfig().asNullable())
            .setMandatory(findFirstArgument(substatements, MandatoryEffectiveStatement.class, Boolean.FALSE))
            .toFlags();
    }
}
