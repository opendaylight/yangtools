/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.meta;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatementDecorators;
import org.opendaylight.yangtools.yang.model.ri.stmt.DeclaredStatements;
import org.opendaylight.yangtools.yang.model.ri.stmt.EffectiveStatements;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.EffectiveStatementWithFlags.FlagsBuilder;
import org.opendaylight.yangtools.yang.model.spi.meta.SubstatementIndexingException;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStmtUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractSchemaTreeStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

@Beta
public final class ContainerStatementSupport
        extends AbstractSchemaTreeStatementSupport<ContainerStatement, ContainerEffectiveStatement> {
    private static final SubstatementValidator RFC6020_VALIDATOR =
        SubstatementValidator.builder(YangStmtMapping.CONTAINER)
            .addAny(YangStmtMapping.ANYXML)
            .addAny(YangStmtMapping.CHOICE)
            .addOptional(YangStmtMapping.CONFIG)
            .addAny(YangStmtMapping.CONTAINER)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addAny(YangStmtMapping.GROUPING)
            .addAny(YangStmtMapping.IF_FEATURE)
            .addAny(YangStmtMapping.LEAF)
            .addAny(YangStmtMapping.LEAF_LIST)
            .addAny(YangStmtMapping.LIST)
            .addAny(YangStmtMapping.MUST)
            .addOptional(YangStmtMapping.PRESENCE)
            .addOptional(YangStmtMapping.REFERENCE)
            .addOptional(YangStmtMapping.STATUS)
            .addAny(YangStmtMapping.TYPEDEF)
            .addAny(YangStmtMapping.USES)
            .addOptional(YangStmtMapping.WHEN)
            .build();

    private static final SubstatementValidator RFC7950_VALIDATOR =
        SubstatementValidator.builder(YangStmtMapping.CONTAINER)
            .addAny(YangStmtMapping.ACTION)
            .addAny(YangStmtMapping.ANYDATA)
            .addAny(YangStmtMapping.ANYXML)
            .addAny(YangStmtMapping.CHOICE)
            .addOptional(YangStmtMapping.CONFIG)
            .addAny(YangStmtMapping.CONTAINER)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addAny(YangStmtMapping.GROUPING)
            .addAny(YangStmtMapping.IF_FEATURE)
            .addAny(YangStmtMapping.LEAF)
            .addAny(YangStmtMapping.LEAF_LIST)
            .addAny(YangStmtMapping.LIST)
            .addAny(YangStmtMapping.MUST)
            .addAny(YangStmtMapping.NOTIFICATION)
            .addOptional(YangStmtMapping.PRESENCE)
            .addOptional(YangStmtMapping.REFERENCE)
            .addOptional(YangStmtMapping.STATUS)
            .addAny(YangStmtMapping.TYPEDEF)
            .addAny(YangStmtMapping.USES)
            .addOptional(YangStmtMapping.WHEN)
            .build();

    private final SubstatementValidator validator;

    private ContainerStatementSupport(final YangParserConfiguration config, final SubstatementValidator validator) {
        super(YangStmtMapping.CONTAINER, instantiatedPolicy(), config);
        this.validator = requireNonNull(validator);
    }

    public static @NonNull ContainerStatementSupport rfc6020Instance(final YangParserConfiguration config) {
        return new ContainerStatementSupport(config, RFC6020_VALIDATOR);
    }

    public static @NonNull ContainerStatementSupport rfc7950Instance(final YangParserConfiguration config) {
        return new ContainerStatementSupport(config, RFC7950_VALIDATOR);
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return validator;
    }

    @Override
    protected ContainerStatement createDeclared(final StmtContext<QName, ContainerStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return DeclaredStatements.createContainer(ctx.getArgument(), substatements);
    }

    @Override
    protected ContainerStatement attachDeclarationReference(final ContainerStatement stmt,
            final DeclarationReference reference) {
        return DeclaredStatementDecorators.decorateContainer(stmt, reference);
    }

    @Override
    protected ContainerEffectiveStatement createEffective(final Current<QName, ContainerStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        EffectiveStmtUtils.checkUniqueGroupings(stmt, substatements);
        EffectiveStmtUtils.checkUniqueTypedefs(stmt, substatements);
        EffectiveStmtUtils.checkUniqueUses(stmt, substatements);

        try {
            return EffectiveStatements.createContainer(stmt.declared(), stmt.effectivePath(),
                createFlags(stmt, substatements), substatements, stmt.original(ContainerSchemaNode.class));
        } catch (SubstatementIndexingException e) {
            throw new SourceException(e.getMessage(), stmt, e);
        }
    }

    @Override
    public ContainerEffectiveStatement copyEffective(final Current<QName, ContainerStatement> stmt,
            final ContainerEffectiveStatement original) {
        return EffectiveStatements.copyContainer(original, stmt.effectivePath(),
            createFlags(stmt, original.effectiveSubstatements()), stmt.original(ContainerSchemaNode.class));
    }

    private static int createFlags(final Current<?, ?> stmt,
            final Collection<? extends EffectiveStatement<?, ?>> substatements) {
        return new FlagsBuilder()
            .setHistory(stmt.history())
            .setStatus(findFirstArgument(substatements, StatusEffectiveStatement.class, Status.CURRENT))
            .setConfiguration(stmt.effectiveConfig().asNullable())
            .setPresence(findFirstStatement(substatements, PresenceEffectiveStatement.class) != null)
            .toFlags();
    }
}

