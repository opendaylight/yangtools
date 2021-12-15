/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.parser;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointEffectiveStatement;
import org.opendaylight.yangtools.rfc8528.model.api.MountPointStatement;
import org.opendaylight.yangtools.rfc8528.model.api.SchemaMountStatements;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.BoundStmtCtx;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public final class MountPointStatementSupport
        extends AbstractQNameStatementSupport<MountPointStatement, MountPointEffectiveStatement> {
    private static final SubstatementValidator VALIDATOR =
        SubstatementValidator.builder(SchemaMountStatements.MOUNT_POINT)
            .addOptional(YangStmtMapping.CONFIG)
            .addOptional(YangStmtMapping.DESCRIPTION)
            .addOptional(YangStmtMapping.REFERENCE)
            .addOptional(YangStmtMapping.STATUS)
            .build();

    public MountPointStatementSupport(final YangParserConfiguration config) {
        super(SchemaMountStatements.MOUNT_POINT, StatementPolicy.copyDeclared((copy, current, substatements) ->
            copy.getArgument().equals(current.getArgument())
            // Implied by UnknownSchemaNode
            && copy.history().isAugmenting() == current.history().isAugmenting()
            && copy.history().isAddedByUses() == current.history().isAddedByUses()), config, VALIDATOR);
    }

    // FIXME: these two methods are not quite right. RFC8528 states that:
    //
    //    If a mount point is defined within a grouping, its label is
    //    bound to the module where the grouping is used.
    //
    // We are not doing exactly that, in that we can end up rebinding the argument through 'augment', I think.
    @Override
    public QName parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return StmtContextUtils.parseIdentifier(ctx, value);
    }

    @Override
    public QName adaptArgumentValue(final StmtContext<QName, MountPointStatement, MountPointEffectiveStatement> ctx,
            final QNameModule targetModule) {
        return ctx.getArgument().bindTo(targetModule).intern();
    }

    @Override
    public void onStatementAdded(final Mutable<QName, MountPointStatement, MountPointEffectiveStatement> stmt) {
        final StatementDefinition parentDef = stmt.coerceParentContext().publicDefinition();
        SourceException.throwIf(YangStmtMapping.CONTAINER != parentDef && YangStmtMapping.LIST != parentDef, stmt,
            "Mount points may only be defined at either a container or a list");
    }

    @Override
    protected MountPointStatement createDeclared(final BoundStmtCtx<QName> ctx,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        return new MountPointStatementImpl(ctx.getArgument(), substatements);
    }

    @Override
    protected MountPointStatement attachDeclarationReference(final MountPointStatement stmt,
            final DeclarationReference reference) {
        return new RefMountPointStatement(stmt, reference);
    }

    @Override
    protected MountPointEffectiveStatement createEffective(final Current<QName, MountPointStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new MountPointEffectiveStatementImpl(stmt, substatements);
    }
}
