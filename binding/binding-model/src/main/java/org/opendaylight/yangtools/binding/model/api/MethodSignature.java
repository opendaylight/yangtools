/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2026 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.type.builder.AnnotableTypeBuilder;

/**
 * The Method Signature interface contains simplified meta model for java method definition. Each method MUST be defined
 * by name, return type, parameters and access modifier. Additionally method MAY contain associated annotations and a
 * comment. By contract if method does not contain any comments or annotation definitions the {@link #getComment()}
 * SHOULD rather return empty string and {@link #getAnnotations()} SHOULD rather return empty list than {@code null}
 * values.
 *
 * <p>The defining Type contains the reference to Generated Type that declares Method Signature.
 */
@NonNullByDefault
public sealed interface MethodSignature extends TypeMember permits MethodSignature0, MethodSignatureF {
    @Beta
    public final class Builder implements AnnotableTypeBuilder {
        private final String name;
        private final Type returnType;
        private final ValueMechanics mechanics;
        private final boolean isDefault;

        Builder(final String name, final Type returnType, final ValueMechanics mechanics, final boolean isDefault) {
            this.name = requireNonNull(name);
            this.returnType = requireNonNull(returnType);
            this.mechanics = requireNonNull(mechanics);
            this.isDefault = isDefault;
        }

        @Override
        public Builder addAnnotation(final @Nullable AttachedAnnotation annotation) {
            if (annotation != null) {

            }
            return this;
        }

        public MethodSignature build() {

        }
    }

    /**
     * The Parameter interface is designed to hold the information of method Parameter(s). The parameter is defined by
     * his Name which MUST be unique as java does not allow multiple parameters with same names for one method and Type
     * that is associated with parameter.
     *
     * @param name the parameter name
     * @param type the {@link Type} that is bounded to parameter name
     */
    record Parameter(String name, Type type) {
        public Parameter {
            requireNonNull(name);
            requireNonNull(type);
        }
    }

    /**
     * Method return type mechanics. This is a bit of an escape hatch for various behaviors which are supported by
     * code generation.
     */
    enum ValueMechanics {
        /**
         * Usual mechanics, nothing special is going on.
         */
        NORMAL,
        /**
         * Mechanics signaling that the method should not be returning empty collections, but rather squash tham
         * to null.
         */
        NULLIFY_EMPTY,
        /**
         * Mechanics signaling that the method cannot legally return null. This is primarily useful for getters, where
         * the declaration should end up having {@link NonNull} annotation attached to return type. For setters this
         * indicates the setter should never accept a null value.
         */
        NONNULL,
    }

    /**
     * Returns {@code true} if the method signature is defined as abstract.
     *
     * <p>By default in java all method declarations in interface are defined as abstract, but the user does not need
     * necessarily to declare abstract keyword in front of each method. The abstract methods are allowed in Class
     * definitions but only when the class is declared as abstract.
     *
     * @return {@code true} if the method signature is defined as abstract.
     */
    boolean isAbstract();

    /**
     * Returns {@code true} if this method is a {@code interface default} method.
     *
     * @return {@code true} if the method signature is defined as default.
     */
    boolean isDefault();

    /**
     * Returns the List of parameters that method declare. If the method does not contain any parameters, the method
     * will return empty List.
     *
     * @return the List of parameters that method declare.
     * @implSpec Default implementation returns an empty list
     */
    default List<Parameter> getParameters() {
        return List.of();
    }

    /**
     * Return the mechanics associated with this method.
     *
     * @return Associated mechanics
     */
    ValueMechanics getMechanics();

    static Builder builderOfAbstract(final String name, final Type returnType) {
        return builderOfAbstract(name, returnType, ValueMechanics.NORMAL);
    }

    static Builder builderOfAbstract(final String name, final Type returnType, final ValueMechanics mechanics) {
        return new Builder(name, returnType, mechanics, false);
    }

    static Builder builderOfDefault(final String name, final Type returnType) {
        return builderOfDefault(name, returnType, ValueMechanics.NORMAL);
    }

    static Builder builderOfDefault(final String name, final Type returnType, final ValueMechanics mechanics) {
        return new Builder(name, returnType, mechanics, true);
    }
}
