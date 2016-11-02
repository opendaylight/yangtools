/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import java.util.Collection;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataDefinitionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MustStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ContainerEffectiveStatementImpl;

public class ContainerStatementImpl extends AbstractDeclaredStatement<QName> implements ContainerStatement {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(Rfc6020Mapping
            .CONTAINER)
            .addAny(Rfc6020Mapping.ANYXML)
            .addAny(Rfc6020Mapping.CHOICE)
            .addOptional(Rfc6020Mapping.CONFIG)
            .addAny(Rfc6020Mapping.CONTAINER)
            .addOptional(Rfc6020Mapping.DESCRIPTION)
            .addAny(Rfc6020Mapping.GROUPING)
            .addAny(Rfc6020Mapping.IF_FEATURE)
            .addAny(Rfc6020Mapping.LEAF)
            .addAny(Rfc6020Mapping.LEAF_LIST)
            .addAny(Rfc6020Mapping.LIST)
            .addAny(Rfc6020Mapping.MUST)
            .addOptional(Rfc6020Mapping.PRESENCE)
            .addOptional(Rfc6020Mapping.REFERENCE)
            .addOptional(Rfc6020Mapping.STATUS)
            .addAny(Rfc6020Mapping.TYPEDEF)
            .addAny(Rfc6020Mapping.USES)
            .addOptional(Rfc6020Mapping.WHEN)
            .build();

    protected ContainerStatementImpl(final StmtContext<QName, ContainerStatement,?> context) {
        super(context);
    }

    public static class Definition extends AbstractStatementSupport<QName,ContainerStatement,EffectiveStatement<QName,ContainerStatement>> {

        public Definition() {
            super(Rfc6020Mapping.CONTAINER);
        }

        @Override
        public QName parseArgumentValue(final StmtContext<?,?,?> ctx, final String value) {
            return Utils.qNameFromArgument(ctx,value);
        }

        @Override
        public void onStatementAdded(
                final Mutable<QName, ContainerStatement, EffectiveStatement<QName, ContainerStatement>> stmt) {
            stmt.getParentContext().addToNs(ChildSchemaNodes.class, stmt.getStatementArgument(), stmt);
        }

        @Override
        public ContainerStatement createDeclared(final StmtContext<QName, ContainerStatement,?> ctx) {
            return new ContainerStatementImpl(ctx);
        }

        @Override
        public EffectiveStatement<QName,ContainerStatement> createEffective(final StmtContext<QName,ContainerStatement,EffectiveStatement<QName,ContainerStatement>> ctx) {
           return new ContainerEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(final Mutable<QName, ContainerStatement,
                EffectiveStatement<QName, ContainerStatement>> stmt) {
            super.onFullDefinitionDeclared(stmt);
            SUBSTATEMENT_VALIDATOR.validate(stmt);
        }
    }

    @Nonnull
    @Override
    public QName getName() {
        return argument();
    }

    @Override
    public WhenStatement getWhenStatement() {
        return firstDeclared(WhenStatement.class);
    }

    @Nonnull
    @Override
    public Collection<? extends IfFeatureStatement> getIfFeatures() {
        return allDeclared(IfFeatureStatement.class);
    }

    @Override
    public Collection<? extends MustStatement> getMusts() {
        return allDeclared(MustStatement.class);
    }

    @Override
    public PresenceStatement getPresence() {
        return firstDeclared(PresenceStatement.class);
    }

    @Override
    public ConfigStatement getConfig() {
        return firstDeclared(ConfigStatement.class);
    }

    @Override
    public StatusStatement getStatus() {
        return firstDeclared(StatusStatement.class);
    }

    @Override
    public DescriptionStatement getDescription() {
        return firstDeclared(DescriptionStatement.class);
    }

    @Override
    public ReferenceStatement getReference() {
        return firstDeclared(ReferenceStatement.class);
    }

    @Nonnull
    @Override
    public Collection<? extends TypedefStatement> getTypedefs() {
        return allDeclared(TypedefStatement.class);
    }

    @Nonnull
    @Override
    public Collection<? extends GroupingStatement> getGroupings() {
        return allDeclared(GroupingStatement.class);
    }

    @Nonnull
    @Override
    public Collection<? extends DataDefinitionStatement> getDataDefinitions() {
        return allDeclared(DataDefinitionStatement.class);
    }

}
