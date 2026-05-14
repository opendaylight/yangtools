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
import java.util.List;
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
        @Nullable ScalarTypeObjectArchetype superType)
        implements GeneratedTransferObject<ScalarTypeObject<?>>,
                   Archetype.Compat<TypeEffectiveStatement.MandatoryIn<?, ?>> {
    public ScalarTypeObjectArchetype {
        requireNonNull(name);
        requireNonNull(statement);
        requireNonNull(typeDefinition);
        requireNonNull(valueType);
    }

    @Override
    public long serialVersionUID() {
        return new SerialVersionHelper(name)
            .setAbstract(false)
            .addInterface(BitsTypeObjectArchetype.SERIALIZABLE)
            .addField(Naming.getPropertyName(TypeConstants.VALUE_PROP))
            .computeSerialVersion();
    }

    @Override
    @Deprecated(forRemoval = true)
    public @NonNull TypeDefinition<?> getBaseType() {
        return typeDefinition;
    }

    @Override
    @Deprecated(forRemoval = true)
    public @Nullable ScalarTypeObjectArchetype getSuperType() {
        return superType;
    }

    @Override
    @Deprecated(forRemoval = true)
    public boolean isTypedef() {
        return statement instanceof TypedefEffectiveStatement;
    }


    @Override
    @Deprecated(forRemoval = true)
    public List<AnnotationType> getAnnotations() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    public List<Type> getImplements() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    public List<GeneratedType> getEnclosedTypes() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    public List<EnumTypeObjectArchetype> getEnumerations() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    public List<Constant> getConstantDefinitions() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    public List<MethodSignature> getMethodDefinitions() {
        return List.of();
    }

    @Override
    @Deprecated(forRemoval = true)
    public List<GeneratedProperty> getProperties() {
        return List.of();
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
        final var local = superType;
        if (local != null) {
            helper.add("extends", local.name);
        }
        return helper.toString();
    }
}
