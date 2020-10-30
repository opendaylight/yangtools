/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.extension;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.collect.ImmutableList;
import java.util.IdentityHashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigStatements;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ArgumentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YinElementStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.BaseQNameStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.ExtensionNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementDefinitionNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;

public final class ExtensionStatementSupport
        extends BaseQNameStatementSupport<ExtensionStatement, ExtensionEffectiveStatement> {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
        .EXTENSION)
        .addOptional(YangStmtMapping.ARGUMENT)
        .addOptional(YangStmtMapping.DESCRIPTION)
        .addOptional(YangStmtMapping.REFERENCE)
        .addOptional(YangStmtMapping.STATUS)
        .build();
    private static final ExtensionStatementSupport INSTANCE = new ExtensionStatementSupport();
    private static final ThreadLocal<Map<StmtContext<?, ?, ?>, ExtensionEffectiveStatementImpl>> TL_BUILDERS =
            new ThreadLocal<>();

    private ExtensionStatementSupport() {
        super(YangStmtMapping.EXTENSION);
    }

    public static ExtensionStatementSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public QName parseArgumentValue(final StmtContext<?,?,?> ctx, final String value) {
        return StmtContextUtils.parseIdentifier(ctx, value);
    }

    @Override
    public void onStatementDefinitionDeclared(
            final Mutable<QName, ExtensionStatement, ExtensionEffectiveStatement> stmt) {
        super.onStatementDefinitionDeclared(stmt);

        QName stmtName = stmt.coerceStatementArgument();
        if (OpenConfigStatements.OPENCONFIG_VERSION.getStatementName().isEqualWithoutRevision(stmtName)) {
            stmtName = stmtName.withoutRevision();
        }

        stmt.addContext(ExtensionNamespace.class, stmtName, stmt);

        final StmtContext<QName, ?, ?> argument = StmtContextUtils.findFirstDeclaredSubstatement(stmt,
            ArgumentStatement.class);
        final StmtContext<Boolean, ?, ?> yinElement = StmtContextUtils.findFirstDeclaredSubstatement(stmt,
            YinElementStatement.class);

        stmt.addToNs(StatementDefinitionNamespace.class, stmt.getStatementArgument(),
            new ModelDefinedStatementSupport(new ModelDefinedStatementDefinition(stmt.getStatementArgument(),
                argument != null ? argument.getStatementArgument() : null,
                        yinElement != null && yinElement.coerceStatementArgument())));
    }

    @Override
    protected SubstatementValidator getSubstatementValidator() {
        return SUBSTATEMENT_VALIDATOR;
    }

    @Override
    protected ExtensionStatement createDeclared(final StmtContext<QName, ExtensionStatement, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        return new RegularExtensionStatement(ctx.coerceStatementArgument(), substatements);
    }

    @Override
    protected ExtensionStatement createEmptyDeclared(final StmtContext<QName, ExtensionStatement, ?> ctx) {
        return new EmptyExtensionStatement(ctx.coerceStatementArgument());
    }

    @Override
    public ExtensionEffectiveStatement createEffective(
            final StmtContext<QName, ExtensionStatement, ExtensionEffectiveStatement> ctx) {
        Map<StmtContext<?, ?, ?>, ExtensionEffectiveStatementImpl> tl = TL_BUILDERS.get();
        if (tl == null) {
            tl = new IdentityHashMap<>();
            TL_BUILDERS.set(tl);
        }

        final ExtensionEffectiveStatementImpl existing = tl.get(ctx);
        if (existing != null) {
            // Implies non-empty map, no cleanup necessary
            return existing;
        }

        try {
            final ExtensionEffectiveStatementImpl created = new ExtensionEffectiveStatementImpl(ctx.buildDeclared(),
                ctx.getSchemaPath().get());
            verify(tl.put(ctx, created) == null);
            try {
                return super.createEffective(ctx);
            } finally {
                verify(tl.remove(ctx) == created);

            }
        } finally {
            if (tl.isEmpty()) {
                TL_BUILDERS.remove();
            }
        }
    }

    @Override
    protected ExtensionEffectiveStatement createEffective(
            final StmtContext<QName, ExtensionStatement, ExtensionEffectiveStatement> ctx,
            final ExtensionStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        return finishCreate(ctx, substatements);
    }

    @Override
    protected ExtensionEffectiveStatement createEmptyEffective(
            final StmtContext<QName, ExtensionStatement, ExtensionEffectiveStatement> ctx,
            final ExtensionStatement declared) {
        return finishCreate(ctx, ImmutableList.of());
    }

    private static @NonNull ExtensionEffectiveStatement finishCreate(final StmtContext<?, ?, ?> ctx,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        final ExtensionEffectiveStatementImpl ret = verifyNotNull(verifyNotNull(TL_BUILDERS.get(),
            "Statement build state not initialized").get(ctx), "No build state found for %s", ctx);
        ret.setSubstatements(substatements);
        return ret;
    }
}