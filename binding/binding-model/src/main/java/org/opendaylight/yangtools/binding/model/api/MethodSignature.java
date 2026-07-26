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
import com.google.common.base.MoreObjects;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.MethodSignatureImpl;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.util.LazyCollections;

/**
 * The Method Signature interface contains simplified meta model for Java interface method definition. Each method MUST
 * be defined by name, return type, parameters Additionally method MAY contain associated annotations and a comment.
 *
 * <p>By contract if method does not contain any comments or annotation definitions the {@link #getComment()} SHOULD
 * rather return empty string and {@link #getAnnotations()} SHOULD rather return empty list than {@code null} values.
 */
// FIXME: rename to InterfaceMethod
public interface MethodSignature extends Immutable {
    /**
     * {@return the returning {@link Type} of member}
     */
    @NonNull Type getReturnType();

    /**
     * {@return the name of member}
     */
    @NonNull String getName();

    /**
     * {@return comment string associated with member}
     */
    @Nullable TypeMemberComment getComment();

    /**
     * {@return {@code true} if this method is a {@code default} method, or {@code false} if it is abstract}
     */
    boolean isDefault();

    /**
     * Returns the List of parameters that method declare. If the method does not contain any parameters, the method
     * will return empty List.
     *
     * @return the List of parameters that method declare.
     */
    @NonNullByDefault
    List<Parameter> getParameters();

    /**
     * {@return the {@link ValueMechanics} associated with this method}
     */
    @NonNullByDefault
    ValueMechanics getMechanics();

    /**
     * {@return List of annotation definitions attached to this method}
     */
    @NonNullByDefault
    List<AttachedAnnotation.ToMethod> getAnnotations();

    @Beta
    @NonNullByDefault
    static Builder builder(final String name) {
        return new Builder(name);
    }

    /**
     * Method Signature Builder serves solely for building Method Signature and
     * returning the <code>new</code> instance of Method Signature. <br>
     * By definition of {@link MethodSignature} the Method in java MUST contain
     * Name, Return Type and Access Modifier. By default the Access Modifier can be
     * set to public. The Method Signature builder does not contain method for
     * addName due to enforce reason that MethodSignatureBuilder SHOULD be
     * instantiated only once with defined method name. <br>
     * The methods as {@link #addAnnotation(AttachedAnnotation.ToMethod)} and
     * {@link #setComment(TypeMemberComment)} can be used as optional because not all methods
     * MUST contain annotation or comment definitions.
     *
     * @see MethodSignature
     * @since 16.0.0
     */
    @Beta
    final class Builder {
        private final @NonNull String name;

        private @Nullable ArrayList<AttachedAnnotation.ToMethod> annotations = null;
        private List<MethodSignature.Parameter> parameters = List.of();
        private ValueMechanics mechanics = ValueMechanics.NORMAL;
        private boolean isDefault = false;
        private TypeMemberComment comment;
        private Type returnType;

        @NonNullByDefault
        Builder(final String name) {
            this.name = requireNonNull(name);
        }

        /**
         * Adds String definition of comment into Method Signature definition. The comment String MUST NOT contain any
         * comment specific chars (i.e. "/**" or "//") just plain String text description.
         *
         * @param newComment Structured comment
         */
        public @NonNull Builder setComment(final TypeMemberComment newComment) {
            comment = newComment;
            return this;
        }

        /**
         * Sets the flag indicating whether this is a {@code default interface} method.
         *
         * @param newIsDefault true if this signature is to represent a default method.
         * @return this builder
         */
        @NonNullByDefault
        public Builder setDefault(final boolean newIsDefault) {
            isDefault = newIsDefault;
            return this;
        }

        @NonNullByDefault
        public Builder setMechanics(final ValueMechanics newMechanics) {
            mechanics = requireNonNull(newMechanics);
            return this;
        }

        /**
         * Adds return Type into Builder definition for Generated Property. The return Type MUST NOT be {@code>null},
         * otherwise the method SHOULD throw {@link IllegalArgumentException}
         *
         * @param newReaturnType Return Type of the member
         */
        @NonNullByDefault
        public Builder setReturnType(final Type newReaturnType) {
            returnType = requireNonNull(newReaturnType);
            return this;
        }

        /**
         * Adds Parameter into the List of method parameters. Neither the Name or Type of parameter can be {@code null}.
         *
         * <br>
         * In case that any of parameters are defined as <code>null</code> the
         * method SHOULD throw an {@link IllegalArgumentException}
         *
         * @param paramType Parameter Type
         * @param paremName Parameter Name
         */
        @NonNullByDefault
        public Builder addParameter(final Type paramType, final String paremName) {
            parameters = LazyCollections.lazyAdd(parameters, new Parameter(paremName, paramType));
            return this;
        }

        /**
         * Add an {@link AttachedAnnotation.ToMethod} to this builder.
         *
         * @param annotation the {@link AttachedAnnotation.ToMethod}, if {@code null} this method does nothing
         * @return this instance
         */
        public @NonNull Builder addAnnotation(final AttachedAnnotation.@Nullable ToMethod annotation) {
            annotations = addAnnotation(annotations, annotation);
            return this;
        }

        @Beta
        public static <T extends AttachedAnnotation> @Nullable ArrayList<@NonNull T> addAnnotation(
                final @Nullable ArrayList<@NonNull T> list, final @Nullable T annotation) {
            if (annotation == null) {
                return list;
            }
            if (list == null) {
                final var ret = new ArrayList<T>(2);
                ret.add(annotation);
                return ret;
            }
            if (!annotation.repeatable()) {
                final var type = annotation.type();
                for (var existing : list) {
                    if (annotation.equals(existing)) {
                        throw new IllegalArgumentException("Attempt to repeat " + annotation);
                    }
                    if (type.equals(existing.type())) {
                        throw new IllegalArgumentException("Attempt to repeat " + annotation + " after " + existing);
                    }
                }
            }
            list.add(annotation);
            return list;
        }

        @NonNullByDefault
        private List<AttachedAnnotation.ToMethod> annotations() {
            final var local = annotations;
            if (local == null) {
                return List.of();
            }
            return local.size() == 1 ? Collections.singletonList(requireNonNull(local.getFirst())) : List.copyOf(local);
        }

        /**
         * Returns <code>new</code> <i>immutable</i> instance of Method Signature. <br>
         * The <code>definingType</code> param cannot be <code>null</code>. Every method in Java MUST be declared and
         * defined inside the scope of <code>class</code> or <code>interface</code> definition. In case that defining
         * Type will be passed as <code>null</code> reference the method SHOULD thrown {@link IllegalArgumentException}.
         *
         * @return <code>new</code> <i>immutable</i> instance of Method Signature.
         */
        @NonNullByDefault
        public MethodSignature build() {
            final var paramSize = parameters.size();
            final var params = switch (paramSize) {
                case 0 -> List.<MethodSignature.Parameter>of();
                case 1 -> Collections.singletonList(parameters.getFirst());
                default -> List.copyOf(parameters);
            };

            return new MethodSignatureImpl(name, annotations(), comment, returnType, params, isDefault, mechanics);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, parameters, returnType);
        }

        @Override
        public boolean equals(final Object obj) {
            return this == obj || obj instanceof Builder other && name.equals(other.name)
                && Objects.equals(parameters, other.parameters) && Objects.equals(returnType, other.returnType);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper("MethodSignatureBuilder").omitNullValues()
                .add("name", name)
                .add("returnType", returnType)
                .add("parameters", parameters)
                .add("annotations", annotations())
                .add("comment", comment)
                .toString();
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
    @NonNullByDefault
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
}
