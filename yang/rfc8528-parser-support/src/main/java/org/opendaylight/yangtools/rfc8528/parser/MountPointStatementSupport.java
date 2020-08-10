/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.parser;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointEffectiveStatement;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointSchemaNode;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointStatement;
import org.opendaylight.yangtools.rfc8528.model.api.SchemaMountStatements;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredStatement.WithQNameArgument.WithSubstatements;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.UnknownEffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class MountPointStatementSupport
        extends BaseQNameStatementSupport<MountPointStatement, MountPointEffectiveStatement> {

    private static final class Declared extends WithSubstatements implements MountPointStatement {
        Declared(final QName argument, final ImmutableList<? extends DeclaredStatement<?>> substatements) {
            super(argument, substatements);
        }
    }

    private static final class Effective extends UnknownEffectiveStatementBase<QName, MountPointStatement>
            implements MountPointEffectiveStatement, MountPointSchemaNode {

        private final @NonNull SchemaPath path;

        Effective(final StmtContext<QName, MountPointStatement, ?> ctx,
                final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
            super(ctx, substatements);
            path = ctx.coerceParentContext().getSchemaPath().get().createChild(argument());
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
    }

    private static final MountPointStatementSupport INSTANCE = new MountPointStatementSupport(
        SchemaMountStatements.MOUNT_POINT);

    private final SubstatementValidator validator;

    MountPointStatementSupport(final StatementDefinition definition) {
        super(definition);
        this.validator = SubstatementValidator.builder(definition)
                .addOptional(YangStmtMapping.CONFIG)
                .addOptional(YangStmtMapping.DESCRIPTION)
                .addOptional(YangStmtMapping.REFERENCE)
                .addOptional(YangStmtMapping.STATUS)
                .build();
    }

    public static MountPointStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.parseIdentifier(ctx, value);
    }

    @Override
    public QName adaptArgumentValue(final StmtContext<QName, MountPointStatement, MountPointEffectiveStatement> ctx,
            final QNameModule targetModule) {
        return ctx.coerceStatementArgument().bindTo(targetModule).intern();
    }

    @Override
    public void onStatementAdded(final Mutable<QName, MountPointStatement, MountPointEffectiveStatement> stmt) {
        final StatementDefinition parentDef = stmt.coerceParentContext().getPublicDefinition();
        SourceException.throwIf(YangStmtMapping.CONTAINER != parentDef && YangStmtMapping.LIST != parentDef,
                stmt.getStatementSourceReference(), "Mount points may only be defined at either a container or a list");
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return validator;
    }

    @Override
    protected MountPointStatement createDeclared(@NonNull final StmtContext<QName, MountPointStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new Declared(ctx.coerceStatementArgument(), substatements);
    }

    @Override
    protected MountPointStatement createEmptyDeclared(final StmtContext<QName, MountPointStatement, ?> ctx) {
        return createDeclared(ctx, ImmutableList.of());
    }

    @Override
    protected MountPointEffectiveStatement createEffective(
            final StmtContext<QName, MountPointStatement, MountPointEffectiveStatement> ctx,
            final MountPointStatement declared, final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new Effective(ctx, substatements);
    }

    @Override
    protected MountPointEffectiveStatement createEmptyEffective(
            final StmtContext<QName, MountPointStatement, MountPointEffectiveStatement> ctx,
            final MountPointStatement declared) {
        return createEffective(ctx, declared, ImmutableList.of());
    }
}
