/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import com.google.common.annotations.Beta;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Mutable;

/**
 * An {@link Archetype} which results in an interface with zero or more methods.
 *
 * @since 16.0.0
 */
// TODO: a better name perhaps?
@Beta
public sealed interface InterfaceArchetype extends Archetype permits DataRootArchetype, LegacyArchetype {
    /**
     * Base interface for builders resulting in an {@link InterfaceArchetype}.
     *
     * @since 16.0.0
     */
    @Beta
    sealed interface Builder extends Mutable permits InterfaceArchetypeBuilder {
        /**
         * {@return a {@link TypeRef} to the type this builder produces}
         */
        @NonNullByDefault
        TypeRef typeRef();

        /**
         * Add an {@link AttachedAnnotation.ToType} to this builder.
         *
         * @param annotation the {@link AttachedAnnotation.ToType}, if {@code null} this method does nothing
         * @return this instance
         */
        @NonNull Builder addAnnotation(AttachedAnnotation.@Nullable ToType annotation);

        /**
         * Adds a new enclosed {@link Archetype} into definition of Generated Type.
         *
         * <br>There is no need of specifying of Package Name because enclosing Type is already defined inside Generated
         * Type with specific package name.
         *
         * <br>The name of enclosing Type cannot be same as Name of parent type and if there is already defined
         * enclosing type with the same name, the new enclosing type will simply overwrite the older definition.
         *
         * <br>If the parameter <code>genTOBuilder</code> of enclosing type is <code>null</code> the method SHOULD throw
         * {@link IllegalArgumentException}.
         *
         * @param genType the enclosed {@link Archetype}
         */
        @NonNullByDefault
        Builder addEnclosedType(Archetype genType);

        /**
         * Add Type to implements.
         *
         * @param genType Type to implement
         * @return <code>true</code> if the addition of type is successful.
         */
        @NonNullByDefault
        Builder addImplementsType(Type genType);

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
        @NonNullByDefault
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
        @NonNullByDefault
        default Constant addConstant(final Builder builder, final String name, final Object value) {
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
        @NonNullByDefault
        MethodSignature.Builder addMethod(String name);

        /**
         * {@return a new immutable {@link InterfaceArchetype} instance}
         */
        @NonNull InterfaceArchetype build();
    }

    /**
     * {@return the list of annotations attached to interface declaration}
     */
    // FIXME: all type annotations should be implied by specialization and this method should not exist
    @NonNullByDefault
    List<AttachedAnnotation.ToType> annotations();

    /**
     * {@return the list of interfaces the interface extends}
     */
    // FIXME: this method should be replaced with sharper tools:
    //        - only allow GroupingArchetypes here
    //        - have CaseArchetype have a dedicated pointer to its inherited ChoiceArchetype
    //        everything else should be implied by the archetype itself
    @NonNullByDefault
    List<Type> getImplements();

    /**
     * {@return the list of constants the interface defines}
     */
    // FIXME: all constants should be implied by a particular archetype and this method should not exist
    @NonNullByDefault
    List<Constant> getConstantDefinitions();

    /**
     * {@return the list of methods the interface defines}
     */
    // FIXME: yes, these result in methods being generated, but they are somewhat subtle, as they also imply constants
    //        for builders, etc. Most notably, KeyArchetype is presenting a subset of these defined in its corresponding
    //        LegacyArchetype (or EntryObjectArchetype once that is created)
    @NonNullByDefault
    List<MethodSignature> getMethodDefinitions();
}
