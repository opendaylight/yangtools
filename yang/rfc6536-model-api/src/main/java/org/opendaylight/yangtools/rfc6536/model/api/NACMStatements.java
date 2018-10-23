/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6536.model.api;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * {@link StatementDefinition}s for statements defined by RFC6536.
 *
 * @author Robert Varga
 */
@NonNullByDefault
public enum NACMStatements implements StatementDefinition {
    DEFAULT_DENY_ALL(QName.create(NACMConstants.RFC6536_MODULE, "default-deny-all"), DefaultDenyAllStatement.class,
        DefaultDenyAllEffectiveStatement.class),
    DEFAULT_DENY_WRITE(QName.create(NACMConstants.RFC6536_MODULE, "default-deny-write"),
        DefaultDenyWriteStatement.class, DefaultDenyWriteEffectiveStatement.class);

    private final Class<? extends EffectiveStatement<?, ?>> effectiveRepresentation;
    private final Class<? extends DeclaredStatement<?>> declaredRepresentation;
    private final QName statementName;

    NACMStatements(final QName statementName, final Class<? extends DeclaredStatement<?>> declaredRepresentation,
            final Class<? extends EffectiveStatement<?, ?>> effectiveRepresentation) {
        this.statementName = statementName.intern();
        this.declaredRepresentation = requireNonNull(declaredRepresentation);
        this.effectiveRepresentation = requireNonNull(effectiveRepresentation);
    }

    @Override
    public @Nullable QName getArgumentName() {
        return null;
    }

    @Override
    public boolean isArgumentYinElement() {
        return false;
    }

    @Override
    public QName getStatementName() {
        return statementName;
    }

    @Override
    public Class<? extends EffectiveStatement<?, ?>> getEffectiveRepresentationClass() {
        return effectiveRepresentation;
    }

    @Override
    public Class<? extends DeclaredStatement<?>> getDeclaredRepresentationClass() {
        return declaredRepresentation;
    }
}
