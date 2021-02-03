/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.extension;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnqualifiedQName;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.EffectiveStmtCtx.Current;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

// FIXME: YANGTOOLS-1196: remove this class
@Deprecated
final class UnrecognizedStatementSupport
        extends AbstractStatementSupport<Object, UnrecognizedStatement, UnrecognizedEffectiveStatement> {
    UnrecognizedStatementSupport(final StatementDefinition publicDefinition) {
        super(publicDefinition, StatementPolicy.alwaysCopyDeclared());
    }

    @Override
    public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
        return value;
    }

    @Override
    public Optional<StatementSupport<?, ?, ?>> getUnknownStatementDefinitionOf(
            final StatementDefinition yangStmtDef) {
        final QName baseQName = getStatementName();
        final QName statementName = QName.create(baseQName, yangStmtDef.getStatementName().getLocalName());

        final ModelDefinedStatementDefinition def;
        final Optional<ArgumentDefinition> optArgDef = yangStmtDef.getArgumentDefinition();
        if (optArgDef.isPresent()) {
            final ArgumentDefinition argDef = optArgDef.get();
            def = new ModelDefinedStatementDefinition(statementName, argDef.getArgumentName(), argDef.isYinElement());
        } else {
            def = new ModelDefinedStatementDefinition(statementName);
        }
        return Optional.of(new UnrecognizedStatementSupport(def));
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return null;
    }

    @Override
    protected UnrecognizedStatement createDeclared(final StmtContext<Object, UnrecognizedStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new UnrecognizedStatementImpl(ctx.rawArgument(), ctx.publicDefinition(), substatements);
    }

    @Override
    protected UnrecognizedStatement createEmptyDeclared(final StmtContext<Object, UnrecognizedStatement, ?> ctx) {
        return createDeclared(ctx, ImmutableList.of());
    }

    @Override
    protected UnrecognizedEffectiveStatement createEffective(final Current<Object, UnrecognizedStatement> stmt,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        // FIXME: Remove following section after fixing 4380
        final UnknownSchemaNode original = (UnknownSchemaNode) stmt.original();
        return new UnrecognizedEffectiveStatementImpl(stmt, substatements,
            original == null ? qnameFromArgument(stmt) : original.getQName());
    }

    private static QName qnameFromArgument(final Current<Object, UnrecognizedStatement> stmt) {
        final String value = stmt.rawArgument();
        if (value == null || value.isEmpty()) {
            return stmt.publicDefinition().getStatementName();
        }

        final int colon = value.indexOf(':');
        if (colon == -1) {
            final UnqualifiedQName qname = UnqualifiedQName.tryCreate(value);
            return qname == null ? null : qname.bindTo(stmt.moduleName().getModule()).intern();
        }

        final QNameModule qnameModule = StmtContextUtils.getModuleQNameByPrefix(stmt.caerbannog(),
            value.substring(0, colon));
        if (qnameModule == null) {
            return null;
        }

        final int next = value.indexOf(':', colon + 1);
        final String localName = next == -1 ? value.substring(colon + 1) : value.substring(colon + 1, next);
        // Careful: selected string may still not be an identifier
        final UnqualifiedQName qname = UnqualifiedQName.tryCreate(localName);
        return qname == null ? null : qname.bindTo(qnameModule).intern();
    }
}