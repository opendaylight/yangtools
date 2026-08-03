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
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * The Method Signature interface contains simplified meta model for Java interface method definition. Each method MUST
 * be defined by name, return type, parameters Additionally method MAY contain associated annotations and a comment.
 */
// FIXME: rename to InterfaceMethod or something, potentially nested in DataContainerArchetype
@Beta
public sealed interface MethodSignature extends Immutable permits MethodSignature0, MethodSignature1, MethodSignatureN {
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

    @NonNullByDefault
    static MethodSignature of(final EffectiveStatement<?, ?> statement, final String name, final Type returnType,
            final ValueMechanics mechanics) {
        return new MethodSignature0(statement, checkName(name), returnType, mechanics, false);
    }

    @NonNullByDefault
    static MethodSignature of(final EffectiveStatement<?, ?> statement, final String name, final Type returnType,
            final ValueMechanics mechanics, final AttachedAnnotation.ToMethod annotation) {
        return new MethodSignature1(statement, checkName(name), returnType, mechanics, false, annotation);
    }

    @NonNullByDefault
    static MethodSignature ofDefault(final EffectiveStatement<?, ?> statement, final String name, final Type returnType,
            final ValueMechanics mechanics) {
        return new MethodSignature0(statement, checkName(name), returnType, mechanics, true);
    }

    @NonNullByDefault
    static MethodSignature ofDefault(final EffectiveStatement<?, ?> statement, final String name, final Type returnType,
            final ValueMechanics mechanics, final AttachedAnnotation.ToMethod annotation) {
        return new MethodSignature1(statement, checkName(name), returnType, mechanics, true, annotation);
    }

    @Beta
    @NonNullByDefault
    static Builder builder(final EffectiveStatement<?, ?> statement, final String name, final Type returnType,
            final ValueMechanics mechanics) {
        return new Builder(statement, checkName(name), returnType, mechanics, false);
    }

    @Beta
    @NonNullByDefault
    static Builder builderOfDefault(final EffectiveStatement<?, ?> statement, final String name, final Type returnType,
            final ValueMechanics mechanics) {
        return new Builder(statement, checkName(name), returnType, mechanics, true);
    }

    @NonNullByDefault
    private static String checkName(final String name) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("empty name");
        }
        return name;
    }

    /**
     * A builder for {@link MethodSignature}s.
     *
     * @see MethodSignature
     * @since 16.0.0
     */
    @Beta
    final class Builder {
        private final @NonNull EffectiveStatement<?, ?> statement;
        private final @NonNull String name;
        private final @NonNull Type returnType;
        private final @NonNull ValueMechanics mechanics;
        private final boolean isDefault;

        private @Nullable ArrayList<AttachedAnnotation.@NonNull ToMethod> annotations = null;

        @NonNullByDefault
        Builder(final EffectiveStatement<?, ?> statement, final String name, final Type returnType,
                final ValueMechanics mechanics, final boolean isDefault) {
            this.statement = requireNonNull(statement);
            this.name = requireNonNull(name);
            this.returnType = requireNonNull(returnType);
            this.mechanics = requireNonNull(mechanics);
            this.isDefault = isDefault;
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
            final var local = annotations;
            if (local == null) {
                return new MethodSignature0(statement, name, returnType, mechanics, isDefault);
            }
            return local.size() == 1
                ? new MethodSignature1(statement, name, returnType, mechanics, isDefault, local.getFirst())
                : new MethodSignatureN(statement, name, returnType, mechanics, isDefault, List.copyOf(local));
        }
    }
}
