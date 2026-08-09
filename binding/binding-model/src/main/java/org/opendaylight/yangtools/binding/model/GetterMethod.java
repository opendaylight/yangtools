/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2026 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model;

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
import org.opendaylight.yangtools.binding.model.api.AnyItemObject;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.SystemEntryObject;
import org.opendaylight.yangtools.binding.model.api.SystemLeafList;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.UserEntryObject;
import org.opendaylight.yangtools.binding.model.api.UserLeafList;
import org.opendaylight.yangtools.binding.model.impl.GetterMethod0;
import org.opendaylight.yangtools.binding.model.impl.GetterMethod1;
import org.opendaylight.yangtools.binding.model.impl.GetterMethodN;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;

/**
 * Prototype for a getter method carried in a {@link DataContainer}.
 *
 * @since 16.0.0
 */
@Beta
public sealed interface GetterMethod extends Immutable permits GetterMethod0, GetterMethod1, GetterMethodN {
    /**
     * {@return the {@link SchemaTreeEffectiveStatement} which led to this method}
     */
    // TODO: this is separate from returnType construct, but in some cases they overlap, like in:
    //         container foo {
    //           container bar;    <-- generates getBar() with ContainerObjectArchetype which has the same statement
    //         }
    @NonNull SchemaTreeEffectiveStatement<?> statement();

    /**
     * {@return the method's suffix, e.g. the {@code Foo} in {@code getFoo}. Typically computed on each access.}
     */
    default @NonNull String suffix() {
        return Naming.getGetterSuffix(statement().argument());
    }

    /**
     * {@return the method {@link ReturnType} which needs to be combined with {@link #statement()}}
     * @see #returnType()
     * @see #statement()
     */
    @NonNull ReturnType type();

    /**
     * {@return the method return type in legacy format}
     * @see #type()
     */
    default @NonNull Type returnType() {
        return switch (type()) {
            // simple types: check whether the use is in leaf or leaf-list
            case ConcreteType type -> leafOrLeafList(statement(), type);
            case IdentityArchetype type -> leafOrLeafList(statement(), type);
            case TypeObjectArchetype<?> type -> leafOrLeafList(statement(), type);
            case UnknownLeafrefType type -> leafOrLeafList(statement(), type);

            // non-values
            case ChoiceInArchetype type -> type;
            case ContainerObjectArchetype type -> type;
            case EntryObjectArchetype type -> switch (type.statement().effectiveOrdering()) {
                case SYSTEM -> new SystemEntryObject(type);
                case USER -> new UserEntryObject(type);
            };
            case ItemObjectArchetype type -> new AnyItemObject(type);
            case OpaqueObjectArchetype<?> type -> type;
        };
    }

    @NonNullByDefault
    private static Type leafOrLeafList(final EffectiveStatement<?, ?> statement, final Type type) {
        return switch (statement) {
            case LeafEffectiveStatement stmt -> type;
            case LeafListEffectiveStatement stmt -> {
                // If we are a leafref and the reference cannot be resolved, we need to generate a list wildcard, not
                // List<Object>, we will try to narrow the return type in subclasses.
                yield switch (stmt.effectiveOrdering()) {
                    case SYSTEM -> SystemLeafList.of(type);
                    case USER -> UserLeafList.of(type);
                };
            }
            default -> throw new VerifyException("Unexpected shape of " + type + " with " + statement);
        };
    }

    /**
     * {@return List of annotation definitions attached to this method}
     */
    @NonNullByDefault
    List<GetterAnnotation> annotations();

    // FIXME: do not take a name
    @NonNullByDefault
    static GetterMethod of(final SchemaTreeEffectiveStatement<?> statement, final ReturnType type) {
        return new GetterMethod0(statement, type);
    }

    @NonNullByDefault
    static GetterMethod of(final SchemaTreeEffectiveStatement<?> statement, final ReturnType type,
            final GetterAnnotation annotation) {
        return new GetterMethod1(statement, type, annotation);
    }

    @NonNullByDefault
    static GetterMethod of(final SchemaTreeEffectiveStatement<?> statement, final ReturnType type,
            final List<GetterAnnotation> annotations) {
        return switch (annotations.size()) {
            case 0 -> of(statement, type);
            case 1 -> of(statement, type, annotations.getFirst());
            default -> {
                final var checked = new ArrayList<GetterAnnotation>(annotations.size());
                for (var annotation : annotations) {
                    if (!annotation.repeatable()) {
                        final var annType = annotation.type();
                        for (var existing : checked) {
                            if (annotation.equals(existing)) {
                                throw new IllegalArgumentException("Attempt to repeat " + annotation);
                            }
                            if (annType.equals(existing.type())) {
                                throw new IllegalArgumentException(
                                    "Attempt to repeat " + annotation + " after " + existing);
                            }
                        }
                    }
                    checked.add(annotation);
                }
                yield new GetterMethodN(statement, type, List.copyOf(checked));
            }
        };
    }

    @Beta
    @NonNullByDefault
    static Builder builder(final SchemaTreeEffectiveStatement<?> statement, final ReturnType type) {
        return new Builder(statement, type);
    }

    /**
     * A builder for {@link GetterMethod}s.
     *
     * @see GetterMethod
     * @since 16.0.0
     */
    @Beta
    final class Builder {
        private final @NonNull SchemaTreeEffectiveStatement<?> statement;
        private final @NonNull ReturnType type;

        private @Nullable ArrayList<@NonNull GetterAnnotation> annotations = null;

        @NonNullByDefault
        Builder(final SchemaTreeEffectiveStatement<?> statement, final ReturnType type) {
            this.statement = requireNonNull(statement);
            this.type = requireNonNull(type);
        }

        /**
         * Add an {@link GetterAnnotation} to this builder.
         *
         * @param annotation the {@link GetterAnnotation}
         * @return this instance
         */
        @NonNullByDefault
        public Builder addAnnotation(final GetterAnnotation annotation) {
            annotations = addAnnotation(annotations, requireNonNull(annotation));
            return this;
        }

        @NonNullByDefault
        private static ArrayList<GetterAnnotation> addAnnotation(
                final @Nullable ArrayList<GetterAnnotation> list, final GetterAnnotation annotation) {
            if (list == null) {
                final var ret = new ArrayList<GetterAnnotation>(2);
                ret.add(annotation);
                return ret;
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
            return local == null ? of(statement, type) : of(statement, type, local);
        }
    }
}
