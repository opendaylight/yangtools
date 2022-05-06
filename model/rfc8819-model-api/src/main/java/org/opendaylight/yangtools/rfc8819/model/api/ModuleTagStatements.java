/*
 * Copyright (c) 2022 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8819.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

@Beta
@NonNullByDefault
public enum ModuleTagStatements implements StatementDefinition {
    MODULE_TAG(QName.create(ModuleTagsConstants.RFC8819_MODULE, "module-tag"), "tag",
        ModuleTagStatement.class, ModuleTagEffectiveStatement.class);

    private final Class<? extends EffectiveStatement<?, ?>> effectiveRepresentation;
    private final Class<? extends DeclaredStatement<?>> declaredRepresentation;
    private final ArgumentDefinition argumentDef;
    private final QName statementName;

    ModuleTagStatements(final QName statementName, final String argumentName,
                        final Class<? extends DeclaredStatement<?>> declaredRepresentation,
                        final Class<? extends EffectiveStatement<?, ?>> effectiveRepresentation) {
        this.statementName = statementName.intern();
        this.declaredRepresentation = requireNonNull(declaredRepresentation);
        this.effectiveRepresentation = requireNonNull(effectiveRepresentation);
        argumentDef = ArgumentDefinition.of(QName.create(statementName, argumentName), false);
    }

    @Override
    public QName getStatementName() {
        return statementName;
    }

    @Override
    public Optional<ArgumentDefinition> getArgumentDefinition() {
        return Optional.of(argumentDef);
    }

    @Override
    public @NonNull Class<? extends DeclaredStatement<?>> getDeclaredRepresentationClass() {
        return declaredRepresentation;
    }

    @Override
    public @NonNull Class<? extends EffectiveStatement<?, ?>> getEffectiveRepresentationClass() {
        return effectiveRepresentation;
    }
}
