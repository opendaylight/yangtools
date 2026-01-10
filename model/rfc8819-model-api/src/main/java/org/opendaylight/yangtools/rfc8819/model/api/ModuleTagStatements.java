/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8819.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * {@link StatementDefinition}s for statements defined by RFC8819.
 */
@Beta
public enum ModuleTagStatements implements StatementDefinition {
    /**
     * The {@code module-tag} statement.
     */
    MODULE_TAG(ModuleTagStatement.class, ModuleTagEffectiveStatement.class, "module-tag", "tag");

    private final @NonNull Class<? extends DeclaredStatement<?>> type;
    private final @NonNull Class<? extends EffectiveStatement<?, ?>> effectiveType;
    private final @NonNull QName name;
    private final @NonNull ArgumentDefinition argument;

    ModuleTagStatements(final Class<? extends DeclaredStatement<?>> declared,
                        final Class<? extends EffectiveStatement<?, ?>> effective, final String statementName,
                        final String argumentName) {
        type = requireNonNull(declared);
        effectiveType = requireNonNull(effective);
        name = createQName(statementName);
        argument = ArgumentDefinition.of(createQName(argumentName), false);
    }

    private static @NonNull QName createQName(final String localName) {
        return QName.create(ModuleTagConstants.RFC8819_MODULE, localName).intern();
    }

    @Override
    public QName statementName() {
        return name;
    }

    @Override
    public ArgumentDefinition argumentDefinition() {
        return argument;
    }

    @Override
    public Class<? extends EffectiveStatement<?, ?>> getEffectiveRepresentationClass() {
        return effectiveType;
    }

    @Override
    public Class<? extends DeclaredStatement<?>> getDeclaredRepresentationClass() {
        return type;
    }
}
