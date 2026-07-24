/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import com.google.common.annotations.Beta;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.model.api.type.builder.AnnotableTypeBuilder;
import org.opendaylight.yangtools.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.yangtools.binding.model.api.type.builder.TypeBuilder;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.AbstractGeneratedTypeBuilder;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * A legacy {@link Archetype} for an interface class. It needs to be specified with:
 * <ul>
 *   <li>{@code package} that belongs into</li>
 *   <li>{@code interface} name (with commentary that <b>SHOULD</b> be present to proper define interface and base
 *       <i>contracts</i> specified for interface)</li>
 *   <li>Each Generated Type can define list of types that Generated Type can implement to extend it's definition
 *       (i.e. interface extends list of interfaces or java class implements list of interfaces)</li>
 *   <li>Each Generated Type can contain multiple enclosed definitions of Generated Types (i.e. interface can contain N
 *       enclosed interface definitions or enclosed classes)</li>
 *   <li>{@code enum} and {@code constant} definitions (i.e. each constant definition is by default defined as
 *       {@code public static final} + type (either primitive or object) and constant name</li>
 *   <li>{@code method definitions} with specified input parameters (with types) and return values</li>
 * </ul>
 *
 * <p>By the definition of the interface constant, enum, enclosed types and method definitions MUST be public, so there
 * is no need to specify the scope of visibility.
 *
 * @param <S> EffectiveStatement type
 */
public non-sealed interface LegacyArchetype<S extends EffectiveStatement<?, ?>> extends Archetype.WithStatement<S> {
    @Beta
    public sealed interface Builder<T extends Builder<T, S>, S extends EffectiveStatement<?, ?>>
            extends TypeBuilder, AnnotableTypeBuilder
            permits AbstractGeneratedTypeBuilder, DataRootArchetype.Builder {
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

    /**
     * {@return List of annotation definitions associated with generated type}
     */
    @NonNull List<AnnotationType> getAnnotations();

    /**
     * {@return List of Types that Generated Type will implement}
     */
    @NonNull List<Type> getImplements();

    /**
     * {@return List of Constant definitions associated with Generated Type}
     */
    @NonNull List<Constant> getConstantDefinitions();

    /**
     * Returns List of Method Definitions associated with Generated Type. The list does not contains getters and setters
     * for properties.
     *
     * @return List of Method Definitions associated with Generated Type.
     */
    @NonNull List<MethodSignature> getMethodDefinitions();
}
