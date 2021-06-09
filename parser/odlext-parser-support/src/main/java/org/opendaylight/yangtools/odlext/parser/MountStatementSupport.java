/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.parser;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.Set;
import org.opendaylight.yangtools.odlext.model.api.MountEffectiveStatement;
import org.opendaylight.yangtools.odlext.model.api.MountStatement;
import org.opendaylight.yangtools.odlext.model.api.OpenDaylightExtensionsStatements;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractEmptyStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

@Beta
public final class MountStatementSupport
        extends AbstractEmptyStatementSupport<MountStatement, MountEffectiveStatement> {
    private static final SubstatementValidator VALIDATOR =
        SubstatementValidator.builder(OpenDaylightExtensionsStatements.MOUNT).build();
    private static final Set<StatementDefinition> ALLOWED_PARENTS =
        Set.of(YangStmtMapping.CONTAINER, YangStmtMapping.LIST);

    public MountStatementSupport(final YangParserConfiguration config) {
        super(OpenDaylightExtensionsStatements.MOUNT, StatementPolicy.exactReplica(), config, VALIDATOR);
    }

    @Override
    public void onStatementAdded(final Mutable<Empty, MountStatement, MountEffectiveStatement> stmt) {
        final StatementDefinition parentDef = stmt.coerceParentContext().publicDefinition();
        SourceException.throwIf(!ALLOWED_PARENTS.contains(parentDef), stmt,
            "Mount is allowed only under container or list, not %s", parentDef);
    }

    @Override
    protected MountStatement createDeclared(final StmtContext<Empty, MountStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new MountStatementImpl(substatements);
    }

    @Override
    protected MountStatement attachDeclarationReference(final MountStatement stmt,
            final DeclarationReference reference) {
        return new RefMountStatement(stmt, reference);
    }

    @Override
    protected MountEffectiveStatement createEffective(final Current<Empty, MountStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return new MountEffectiveStatementImpl(stmt.declared(), substatements);
    }
}
