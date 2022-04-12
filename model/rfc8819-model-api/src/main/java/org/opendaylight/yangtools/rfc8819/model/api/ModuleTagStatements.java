/*
 * Copyright (c) 2022 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8819.model.api;

import com.google.common.annotations.Beta;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Beta
@NonNullByDefault
public enum ModuleTagStatements implements StatementDefinition {

    MODULE_TAG(QName.create(XMLNamespace.of("urn:ietf:params:xml:ns:yang:ietf-module-tags").intern(),
            "module-tag"), "tag", ModuleTagStatement.class, ModuleTagEffectiveStatement.class);

    private final Class<? extends EffectiveStatement<?, ?>> effectiveRepresentation;
    private final Class<? extends DeclaredStatement<?>> declaredRepresentation;
    private final QName statementName;
    private final @Nullable QName argumentName;

    @SuppressFBWarnings("NP_STORE_INTO_NONNULL_FIELD")
    ModuleTagStatements(final QName statementName, @Nullable final String argumentName,
                         final Class<? extends DeclaredStatement<?>> declaredRepresentation,
                         final Class<? extends EffectiveStatement<?, ?>> effectiveRepresentation) {
        this.statementName = statementName.intern();
        this.argumentName = argumentName != null ? QName.create(statementName, argumentName) : null;
        this.declaredRepresentation = requireNonNull(declaredRepresentation);
        this.effectiveRepresentation = requireNonNull(effectiveRepresentation);
    }

    @Override
    public @NonNull QName getStatementName() {
        return statementName;
    }

    @Override
    public @NonNull Optional<ArgumentDefinition> getArgumentDefinition() {
        return ArgumentDefinition.ofNullable(argumentName, false);
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
