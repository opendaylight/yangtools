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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * The Method Signature interface contains simplified meta model for Java interface method definition. Each method MUST
 * be defined by name, return type, parameters Additionally method MAY contain associated annotations and a comment.
 *
 * <p>By contract if method does not contain any comments or annotation definitions the {@link #getComment()} SHOULD
 * rather return empty string and {@link #annotations()} SHOULD rather return empty list than {@code null} values.
 */
// FIXME: rename to InterfaceMethod or something, potentially nested in DataContainerArchetype
// FIXME: these should carry an EffectiveStatement, because while they are not individual templates, they are emitted
//        in response to a statement. Further thought needs to go into this, as UnionTypeObjectArchetype and
//        KeyArchetype both have the notion of GeneratedProperty, which carries similar data.
public sealed interface MethodSignature extends Immutable permits MethodSignatureImpl {
    /**
     * Method return type mechanics. This is a bit of an escape hatch for various behaviors which are supported by
     * code generation.
     */
    // FIXME: remove this enum and the notion of 'isDefault', as we have very crisp model derivable from returnType()
    //        and statement():
    //        - abstract + NORMAL are:
    //          - getFoo
    //          - nonnullFoo for structural containers -- where we provide a default implementation anyway
    //        - abstract + NULLIFY_EMPTY:
    //          - getFoo for list
    //        - default + NORMAL are:
    //          - nonNullFoo for list
    //        - default + NONNULL are:
    //          - requireFoo generated leaf/leaf-list/anydata/anyxml
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
     * {@return the {@link EffectiveStatement} which led to this method}
     */
    // FIXME: sharpen to SchemaTreeEffectiveStatement
    // TODO: this is separate from returnType construct, but in some cases they overlap, like in:
    //         container foo {
    //           container bar;    <-- generates getBar() with ContainerObjectArchetype which has the same statement
    //         }
    @NonNull EffectiveStatement<?, ?> statement();

    /**
     * {@return the method name}
     */
    // TODO: Investigate the relationship with statement once we remove ValueMechanics, as then we can generate
    //       nonNullFoo/requireFoo from getter name -- and getterName is always derived from
    //       Naming.getGetterMethodName(QName).
    //
    //       That may be a bug in the implementation not handling conflicts like:
    //         container foo {
    //           leaf bar { type string; }
    //           leaf Bar { type uint64; }
    //         }
    //
    //       Our ability to address such problems is limited by groupings, as they essentially freeze their view on
    //       naming and may be sitting in a different compilation unit, so providing backpressure from instantiations
    //       is a challenge.
    //
    //       Anyway, our ability to deal with these kinds of problems is vastly improved with the introduction of
    //       DataContainerArchetype and we should be doing our level best to make things work even in face of such
    //       challenging models.
    @NonNull String name();

    /**
     * {@return the method return type}
     */
    // FIXME: dedicated 'ReturnType'
    @NonNull Type returnType();

    /**
     * {@return List of annotation definitions attached to this method}
     */
    @NonNullByDefault
    List<AttachedAnnotation.ToMethod> annotations();

    /**
     * {@return the {@link ValueMechanics} associated with this method}
     */
    // FIXME: remove
    @NonNullByDefault
    ValueMechanics mechanics();

    /**
     * {@return {@code true} if this method is a {@code default} method, or {@code false} if it is abstract}
     */
    // FIXME: remove
    boolean isDefault();

    /**
     * {@return comment string associated with member}
     */
    // FIXME: remove
    @Nullable TypeMemberComment getComment();

    @Beta
    @NonNullByDefault
    static Builder builder(final String name, final Type returnType, final ValueMechanics mechanics) {
        return new Builder(name, returnType, mechanics, false);
    }

    @Beta
    @NonNullByDefault
    static Builder builderOfDefault(final String name, final Type returnType, final ValueMechanics mechanics) {
        return new Builder(name, returnType, mechanics, true);
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
        private final @NonNull Type returnType;
        private final @NonNull ValueMechanics mechanics;
        private final boolean isDefault;

        private @Nullable ArrayList<AttachedAnnotation.@NonNull ToMethod> annotations = null;
        private TypeMemberComment comment;

        @NonNullByDefault
        Builder(final String name, final Type returnType, final ValueMechanics mechanics, final boolean isDefault) {
            this.name = requireNonNull(name);
            this.returnType = requireNonNull(returnType);
            this.mechanics = requireNonNull(mechanics);
            this.isDefault = isDefault;
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
         * Add an {@link AttachedAnnotation.ToMethod} to this builder.
         *
         * @param annotation the {@link AttachedAnnotation.ToMethod}
         * @return this instance
         */
        @NonNullByDefault
        public Builder addAnnotation(final AttachedAnnotation.ToMethod annotation) {
            annotations = addAnnotation(annotations, requireNonNull(annotation));
            return this;
        }

        @NonNullByDefault
        private static ArrayList<AttachedAnnotation.ToMethod> addAnnotation(
                final @Nullable ArrayList<AttachedAnnotation.ToMethod> list,
                final AttachedAnnotation.ToMethod annotation) {
            if (list == null) {
                final var ret = new ArrayList<AttachedAnnotation.ToMethod>(2);
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
            return new MethodSignatureImpl(name, annotations(), comment, returnType, isDefault, mechanics);
        }
    }
}
