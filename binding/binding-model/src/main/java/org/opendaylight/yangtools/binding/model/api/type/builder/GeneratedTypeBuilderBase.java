/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api.type.builder;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.binding.model.api.Constant;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.LegacyArchetype;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.AbstractGeneratedTypeBuilder;

public sealed interface GeneratedTypeBuilderBase<T extends GeneratedTypeBuilderBase<T>>
        extends TypeBuilder, AnnotableTypeBuilder
        permits AbstractGeneratedTypeBuilder, DataRootArchetype.Builder, GeneratedTypeBuilder {
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
    T addEnclosedType(Archetype genType);

    List<Type> getImplementsTypes();

    /**
     * Add Type to implements.
     *
     * @param genType Type to implement
     * @return <code>true</code> if the addition of type is successful.
     */
    T addImplementsType(Type genType);

    /**
     * Add Type to implements.
     *
     * @param builder builder for the Type to implement
     * @return <code>true</code> if the addition of type is successful.
     */
    default T addImplementsType(final TypeBuilder builder) {
        return addImplementsType(builder.typeRef());
    }

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
    default Constant addConstant(final TypeBuilder builder, final String name, final Object value) {
        return addConstant(builder.typeRef(), name, value);
    }

    List<MethodSignatureBuilder> getMethodDefinitions();

    /**
     * Add new Method Signature definition for Generated Type Builder and returns Method Signature Builder
     * for specifying all Method parameters.<br>
     * Name of Method cannot be <code>null</code>, if it is <code>null</code> the method SHOULD throw
     * {@link IllegalArgumentException}.<br>
     * By <i>Default</i> the MethodSignatureBuilder SHOULD be pre-set as
     * {@link MethodSignatureBuilder#setAbstract(boolean)}, {TypeMemberBuilder#setFinal(boolean)} and
     * {TypeMemberBuilder#setAccessModifier(boolean)}
     *
     * @param name Name of Method
     * @return <code>new</code> instance of Method Signature Builder.
     */
    MethodSignatureBuilder addMethod(String name);

    /**
     * Checks if GeneratedTypeBuilder contains method with name <code>methodName</code>.
     *
     * @param methodName is method name
     */
    boolean containsMethod(String methodName);

    /**
     * {@return a new immutable {@link LegacyArchetype} instance}
     */
    @NonNull LegacyArchetype<?> build();
}
