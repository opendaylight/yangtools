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
import com.google.common.base.VerifyException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListEffectiveStatement;

/**
 * Prototype for a getter method carried in a {@link DataContainer}.
 */
@Beta
public sealed interface GetterMethod extends Immutable permits GetterMethod0, GetterMethod1, GetterMethodN {
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
    @NonNull ReturnType returnType();

    default @NonNull Type javaReturnType() {
        return switch (returnType()) {
            // simple types: check whether the use is in leaf or leaf-list
            case ConcreteType type -> leafOrLeafList(type);
            case IdentityArchetype type -> leafOrLeafList(type);
            case TypeObjectArchetype<?> type -> leafOrLeafList(type);

            // non-values
            case ChoiceInArchetype type -> type;
            case ContainerObjectArchetype type -> type;
            case EntryObjectArchetype type -> switch (type.statement().effectiveOrdering()) {
                case SYSTEM -> Types.mapTypeFor(TypeRef.of(type.keyName()), type);
                case USER -> Types.listTypeFor(type);
            };
            case ItemObjectArchetype type -> Types.listTypeFor(type);
            case OpaqueObjectArchetype<?> type -> type;

        };
    }

    @NonNullByDefault
    private Type leafOrLeafList(final Type type) {
        final var statement = statement();
        return switch (statement) {
            case LeafEffectiveStatement stmt -> type;
            case LeafListEffectiveStatement stmt -> {
                // If we are a leafref and the reference cannot be resolved, we need to generate a list wildcard, not
                // List<Object>, we will try to narrow the return type in subclasses.
                final boolean isObject = Types.OBJECT.equals(type);
                yield switch (stmt.effectiveOrdering()) {
                    case SYSTEM -> isObject ? Types.setTypeWildcard() : Types.setTypeFor(type);
                    case USER -> isObject ? Types.listTypeWildcard() : Types.listTypeFor(type);
                };
            }
            default -> throw new VerifyException("Unexpected shape of " + this);
        };
    }

    /**
     * {@return List of annotation definitions attached to this method}
     */
    @NonNullByDefault
    List<AttachedAnnotation.ToMethod> annotations();

    // FIXME: require QName-based statement
    @NonNullByDefault
    static GetterMethod of(final EffectiveStatement<?, ?> statement, final String name, final ReturnType returnType) {
        return new GetterMethod0(statement, checkName(name), returnType);
    }

    @NonNullByDefault
    static GetterMethod of(final EffectiveStatement<?, ?> statement, final String name, final ReturnType returnType,
            final AttachedAnnotation.ToMethod annotation) {
        return new GetterMethod1(statement, checkName(name), returnType, annotation);
    }

    @Beta
    @NonNullByDefault
    static Builder builder(final EffectiveStatement<?, ?> statement, final String name, final ReturnType returnType) {
        return new Builder(statement, checkName(name), returnType);
    }

    @NonNullByDefault
    private static String checkName(final String name) {
        if (!Naming.isGetterMethodName(name)) {
            throw new IllegalArgumentException("invalid getter name '" + name + "'");
        }
        return name;
    }

    /**
     * A builder for {@link GetterMethod}s.
     *
     * @see GetterMethod
     * @since 16.0.0
     */
    @Beta
    final class Builder {
        private final @NonNull EffectiveStatement<?, ?> statement;
        private final @NonNull String name;
        private final @NonNull ReturnType returnType;

        private @Nullable ArrayList<AttachedAnnotation.@NonNull ToMethod> annotations = null;

        @NonNullByDefault
        Builder(final EffectiveStatement<?, ?> statement, final String name, final ReturnType returnType) {
            this.statement = requireNonNull(statement);
            this.name = requireNonNull(name);
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
        public GetterMethod build() {
            final var local = annotations;
            if (local == null) {
                return new GetterMethod0(statement, name, returnType);
            }
            return local.size() == 1 ? new GetterMethod1(statement, name, returnType, local.getFirst())
                : new GetterMethodN(statement, name, returnType, List.copyOf(local));
        }
    }
}
