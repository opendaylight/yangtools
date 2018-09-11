/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.openconfig.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

@Beta
public enum OpenConfigStatements implements StatementDefinition {
    OPENCONFIG_ENCRYPTED_VALUE(QName.create(OpenConfigConstants.ENCRYPTED_VALUE_MODULE, "openconfig-encrypted-value"),
        null, OpenConfigHashedValueStatement.class, OpenConfigHashedValueEffectiveStatement.class),
    OPENCONFIG_HASHED_VALUE(QName.create(OpenConfigConstants.HASHED_VALUE_MODULE, "openconfig-hashed-value"), null,
        OpenConfigHashedValueStatement.class, OpenConfigHashedValueEffectiveStatement.class),
    OPENCONFIG_VERSION(QName.create(OpenConfigConstants.MODULE_NAMESPACE, "openconfig-version"), "semver",
        OpenConfigVersionStatement.class, OpenConfigVersionEffectiveStatement.class);

    private final Class<? extends EffectiveStatement<?, ?>> effectiveRepresentation;
    private final Class<? extends DeclaredStatement<?>> declaredRepresentation;
    private final QName statementName;
    private final QName argumentName;

    OpenConfigStatements(final QName statementName, @Nullable final String argumentName,
            final Class<? extends DeclaredStatement<?>> declaredRepresentation,
            final Class<? extends EffectiveStatement<?, ?>> effectiveRepresentation) {
        this.statementName = statementName.intern();
        this.argumentName = argumentName != null ? QName.create(statementName, argumentName) : null;
        this.declaredRepresentation = requireNonNull(declaredRepresentation);
        this.effectiveRepresentation = requireNonNull(effectiveRepresentation);
    }

    @Nonnull
    @Override
    public QName getStatementName() {
        return statementName;
    }

    @Override
    @Nullable
    public QName getArgumentName() {
        return argumentName;
    }

    @Override
    @Nonnull
    public Class<? extends DeclaredStatement<?>> getDeclaredRepresentationClass() {
        return declaredRepresentation;
    }

    @Nonnull
    @Override
    public Class<? extends EffectiveStatement<?, ?>> getEffectiveRepresentationClass() {
        return effectiveRepresentation;
    }

    @Override
    public boolean isArgumentYinElement() {
        return false;
    }
}
