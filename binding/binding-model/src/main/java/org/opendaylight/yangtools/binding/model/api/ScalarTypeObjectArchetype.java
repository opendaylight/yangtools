/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.ScalarTypeObject;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.ri.TypeConstants;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;

/**
 * An archetype for a {@link ScalarTypeObject}.
 *
 * @since 16.0.0
 */
@Beta
@NonNullByDefault
public record ScalarTypeObjectArchetype(
        JavaTypeName name,
        TypeEffectiveStatement.MandatoryIn<?, ?> statement,
        TypeDefinition<?> typeDefinition,
        ConcreteType valueType,
        Restrictions getRestrictions,
        @Nullable ScalarTypeObjectArchetype getSuperType)
        implements GeneratedTransferObject<ScalarTypeObject<?>>,
                   Archetype.Compat<TypeEffectiveStatement.MandatoryIn<?, ?>> {
    public ScalarTypeObjectArchetype {
        requireNonNull(name);
        requireNonNull(statement);
        requireNonNull(typeDefinition);
        requireNonNull(valueType);
        requireNonNull(getRestrictions);
    }

    @Override
    public long serialVersionUID() {
        final var helper = new SerialVersionHelper(name)
            .setAbstract(false)
            .addInterface(BitsTypeObjectArchetype.SERIALIZABLE);
        if (getSuperType == null) {
            helper.addField(Naming.getPropertyName(TypeConstants.VALUE_PROP));
        }
        return helper.computeSerialVersion();
    }

    @Override
    @Deprecated(forRemoval = true)
    public @NonNull TypeDefinition<?> getBaseType() {
        return typeDefinition;
    }

    @Override
    @Deprecated(forRemoval = true)
    public boolean isTypedef() {
        return statement instanceof TypedefEffectiveStatement;
    }

    @Override
    public final int hashCode() {
        return name.hashCode();
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof Type other && name.equals(other.name());
    }

    @Override
    public final String toString() {
        final var helper = MoreObjects.toStringHelper(this).add("name", name).add("type", typeDefinition);
        final var local = getSuperType;
        if (local != null) {
            helper.add("extends", local.name);
        }
        return helper.toString();
    }
}
