/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8040.model.api;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * {@link StatementDefinition}s for statements defined by RFC8040.
 *
 * @author Robert Varga
 */
@NonNullByDefault
public enum YangDataStatements implements StatementDefinition {
    YANG_DATA(QName.create(YangDataConstants.RFC8040_MODULE, "yang-data"), "name", YangDataStatement.class,
        YangDataEffectiveStatement.class);

    private final Class<? extends EffectiveStatement<?, ?>> effectiveRepresentation;
    private final Class<? extends DeclaredStatement<?>> declaredRepresentation;
    private final QName statementName;
    private final QName argumentName;

    YangDataStatements(final QName statementName, final String argumentName,
            final Class<? extends DeclaredStatement<?>> declaredRepresentation,
                    final Class<? extends EffectiveStatement<?, ?>> effectiveRepresentation) {
        this.statementName = statementName.intern();
        this.argumentName = QName.create(statementName, argumentName);
        this.declaredRepresentation = requireNonNull(declaredRepresentation);
        this.effectiveRepresentation = requireNonNull(effectiveRepresentation);
    }

    @Override
    public @NonNull QName getArgumentName() {
        return argumentName;
    }

    @Override
    public boolean isArgumentYinElement() {
        return true;
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
