/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api.type.builder;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.binding.model.api.AttachedAnnotation;
import org.opendaylight.yangtools.binding.model.api.Constant;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.TypeRef;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.AbstractGeneratedTypeBuilder;

public sealed interface GeneratedTypeBuilderBase<T extends GeneratedTypeBuilderBase<T>>
        permits AbstractGeneratedTypeBuilder, DataRootArchetype.Builder {
    /**
     * {@return the name of the type this builder produces}
     */
    @NonNullByDefault
    JavaTypeName typeName();

    /**
     * {@return a {@link TypeRef} to the type this builder produces}
     */
    @NonNullByDefault
    default TypeRef typeRef() {
        return TypeRef.of(typeName());
    }

    /**
     * Add an {@link AttachedAnnotation.ToType} to this builder.
     *
     * @param annotation the {@link AttachedAnnotation.ToType}, if {@code null} this method does nothing
     * @return this instance
     */
    @NonNull T addAnnotation(AttachedAnnotation.@Nullable ToType annotation);

    /**
     * Adds a new enclosed {@link Archetype} into definition of Generated Type.
     *
     * <br>
     * There is no need of specifying of Package Name because enclosing Type is already defined inside Generated Type
     * with specific package name.<br>
     * The name of enclosing Type cannot be same as Name of parent type and if there is already defined enclosing type
     * with the same name, the new enclosing type will simply overwrite the older definition.<br>
     * If the parameter <code>genTOBuilder</code> of enclosing type is <code>null</code> the method SHOULD throw
     * {@link IllegalArgumentException}.
     *
     * @param genType the enclosed {@link Archetype}
     */
    @NonNullByDefault
    T addEnclosedType(Archetype genType);

    /**
     * Add Type to implements.
     *
     * @param genType Type to implement
     * @return <code>true</code> if the addition of type is successful.
     */
    @NonNullByDefault
    T addImplementsType(Type genType);

    /**
     * Adds Constant definition and returns <code>new</code> Constant instance.<br>
     * By definition Constant MUST be defined by return Type, Name and assigned value. The name SHOULD be defined
     * with capital letters. Neither of method parameters can be <code>null</code> and the method SHOULD throw
     * {@link IllegalArgumentException} if the contract is broken.
     *
     * @param type Constant Type
     * @param name Name of Constant
     * @param value Assigned Value
     * @return <code>new</code> Constant instance.
     */
    Constant addConstant(Type type, String name, Object value);

    /**
     * Adds Constant definition and returns <code>new</code> Constant instance.<br>
     * By definition Constant MUST be defined by return Type, Name and assigned value. The name SHOULD be defined
     * with capital letters. Neither of method parameters can be <code>null</code> and the method SHOULD throw
     * {@link IllegalArgumentException} if the contract is broken.
     *
     * @param builder builder for Constant Type
     * @param name Name of Constant
     * @param value Assigned Value
     * @return <code>new</code> Constant instance.
     */
    default Constant addConstant(final GeneratedTypeBuilderBase<?> builder, final String name, final Object value) {
        return addConstant(builder.typeRef(), name, value);
    }

    /**
     * Add new Method Signature definition for Generated Type Builder and returns Method Signature Builder
     * for specifying all Method parameters.<br>
     * Name of Method cannot be <code>null</code>, if it is <code>null</code> the method SHOULD throw
     * {@link IllegalArgumentException}.<br>
     *
     * @param name Name of Method
     * @return <code>new</code> instance of Method Signature Builder.
     */
    MethodSignature.Builder addMethod(String name);

    /**
     * {@return a new immutable {@link Archetype.OfCompositeInterface} instance}
     */
    @NonNullByDefault
    Archetype.OfCompositeInterface build();
}
