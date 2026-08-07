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
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;

/**
 * The Method Signature interface contains simplified meta model for Java interface method definition. Each method MUST
 * be defined by name, return type, parameters Additionally method MAY contain associated annotations and a comment.
 */
// FIXME: rename to InterfaceMethod or something, potentially nested in DataContainerArchetype
@Beta
public sealed interface MethodSignature extends Immutable permits MethodSignature0, MethodSignature1, MethodSignatureN {
    /**
     * {@return the {@link SchemaTreeEffectiveStatement} which led to this method}
     */
    // TODO: this is separate from returnType construct, but in some cases they overlap, like in:
    //         container foo {
    //           container bar;    <-- generates getBar() with ContainerObjectArchetype which has the same statement
    //         }
    @NonNull SchemaTreeEffectiveStatement<?> statement();

    /**
     * {@return the method name}
     */
    // FIXME: 'suffix' only
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

    // FIXME: do not take a name
    @NonNullByDefault
    static MethodSignature of(final SchemaTreeEffectiveStatement<?> statement, final Type returnType) {
        return new MethodSignature0(statement, Naming.getGetterMethodName(statement.argument()), returnType);
    }

    @NonNullByDefault
    static MethodSignature of(final SchemaTreeEffectiveStatement<?> statement, final Type returnType,
            final AttachedAnnotation.ToMethod annotation) {
        return new MethodSignature1(statement, Naming.getGetterMethodName(statement.argument()), returnType,
            annotation);
    }

    @Beta
    @NonNullByDefault
    static Builder builder(final SchemaTreeEffectiveStatement<?> statement, final Type returnType) {
        return new Builder(statement, returnType);
    }

    /**
     * A builder for {@link MethodSignature}s.
     *
     * @see MethodSignature
     * @since 16.0.0
     */
    @Beta
    final class Builder {
        private final @NonNull SchemaTreeEffectiveStatement<?> statement;
        private final @NonNull Type returnType;

        private @Nullable ArrayList<AttachedAnnotation.@NonNull ToMethod> annotations = null;

        @NonNullByDefault
        Builder(final SchemaTreeEffectiveStatement<?> statement, final Type returnType) {
            this.statement = requireNonNull(statement);
            this.returnType = requireNonNull(returnType);
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
            final var name = Naming.getGetterMethodName(statement.argument());
            final var local = annotations;
            if (local == null) {
                return new MethodSignature0(statement, name, returnType);
            }
            return local.size() == 1 ? new MethodSignature1(statement, name, returnType, local.getFirst())
                : new MethodSignatureN(statement, name, returnType, List.copyOf(local));
        }
    }
}
