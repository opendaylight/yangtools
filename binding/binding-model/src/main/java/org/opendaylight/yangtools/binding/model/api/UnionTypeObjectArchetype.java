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
import com.google.common.collect.Streams;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.UnionTypeObject;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.GeneratedPropertyBuilderImpl;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;

/**
 * An archetype for a {@link UnionTypeObject}.
 *
 * @param typePropertyNames list of property names corresponding to individual {@code type} statements within this
 *        union. The ordering of the returned list matches the ordering of the type statements.
 */
@Beta
@NonNullByDefault
public record UnionTypeObjectArchetype(
        JavaTypeName name,
        TypeEffectiveStatement.MandatoryIn<?, ?> statement,
        List<String> typePropertyNames,
        List<Type> typePropertyTypes,
        List<GeneratedType> getEnclosedTypes,
        Restrictions getRestrictions,
        @Nullable UnionTypeObjectArchetype getSuperType)
        implements GeneratedTransferObject<UnionTypeObject>,
                   Archetype.Compat<TypeEffectiveStatement.MandatoryIn<?, ?>> {
    public UnionTypeObjectArchetype {
        requireNonNull(name);
        requireNonNull(statement);
        requireNonNull(getRestrictions);
        typePropertyNames = List.copyOf(typePropertyNames);
        typePropertyTypes = List.copyOf(typePropertyTypes);
        getEnclosedTypes = List.copyOf(getEnclosedTypes);

        final var uniqueNames = typePropertyNames.stream().distinct().count();
        if (uniqueNames != typePropertyTypes.size()) {
            throw new IllegalArgumentException(uniqueNames + " names does not match " + typePropertyTypes);
        }
    }

    @Override
    public final long serialVersionUID() {
        final var svb = new SerialVersionHelper(name())
            .setAbstract(false)
            .addInterface(BitsTypeObjectArchetype.SERIALIZABLE);

        typePropertyNames.stream().distinct().forEach(svb::addField);

        return svb.computeSerialVersion();
    }

    @Override
    @Deprecated(forRemoval = true)
    public boolean isTypedef() {
        return statement instanceof TypedefEffectiveStatement;
    }

    @Override
    public List<GeneratedType> getEnclosedTypes() {
        return getEnclosedTypes;
    }

    // FIXME: remove this method
    @Override
    public List<GeneratedProperty> getProperties() {
        return Streams.zip(typePropertyNames().stream().distinct(), typePropertyTypes().stream(),
            (pn, pt) -> new GeneratedPropertyBuilderImpl(pn).setReadOnly(true).setReturnType(pt).toInstance())
            .toList();
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
    public @Nullable TypeDefinition<?> getBaseType() {
        return null;
    }
}
