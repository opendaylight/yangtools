/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AddedByUsesAware;
import org.opendaylight.yangtools.yang.model.api.CopyableNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.MandatoryAware;
import org.opendaylight.yangtools.yang.model.api.MustConstraintAware;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.WhenConditionAware;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyHistory;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;

/**
 * Mix-in interfaces providing services required by SchemaNode et al. These interfaces provide implementations, or
 * implementation helpers based on default methods, so the correct behavior can be logically centralized.
 */
@Beta
public final class EffectiveStatementMixins {
    // Marker interface requiring all mixins to be derived from EffectiveStatement.
    private interface Mixin<A, D extends DeclaredStatement<A>> extends EffectiveStatement<A, D> {
        // Utility method with lower restrictions than streamEffectiveStatements()
        default <T> Stream<T> filterEffectiveStatements(final Class<T> type) {
            return effectiveSubstatements().stream().filter(type::isInstance).map(type::cast);
        }
    }

    /**
     * Bridge between {@link EffectiveStatementWithFlags} and {@link AddedByUsesAware}.
     *
     * @param <A> Argument type ({@link Void} if statement does not have argument.)
     * @param <D> Class representing declared version of this statement.
     */
    public interface AddedByUsesMixin<A, D extends DeclaredStatement<A>>
            extends EffectiveStatementWithFlags<A, D>, AddedByUsesAware {
        @Override
        default boolean isAddedByUses() {
            return (flags() & EffectiveStatementWithFlags.FlagsBuilder.ADDED_BY_USES) != 0;
        }
    }

    /**
     * Bridge between {@link EffectiveStatementWithFlags} and {@link MustConstraintAware}.
     *
     * @param <A> Argument type ({@link Void} if statement does not have argument.)
     * @param <D> Class representing declared version of this statement.
     */
    public interface MustConstraintMixin<A, D extends DeclaredStatement<A>> extends Mixin<A, D>, MustConstraintAware {
        @Override
        default Collection<MustDefinition> getMustConstraints() {
            return filterEffectiveStatements(MustDefinition.class).collect(ImmutableList.toImmutableList());
        }
    }

    /**
     * Bridge between {@link EffectiveStatementWithFlags} and {@link CopyableNode}.
     *
     * @param <A> Argument type ({@link Void} if statement does not have argument.)
     * @param <D> Class representing declared version of this statement.
     */
    public interface CopyableMixin<A, D extends DeclaredStatement<A>> extends AddedByUsesMixin<A, D>, CopyableNode {
        @Override
        default boolean isAugmenting() {
            return (flags() & EffectiveStatementWithFlags.FlagsBuilder.AUGMENTING) != 0;
        }
    }

    /**
     * Bridge between {@link EffectiveStatementWithFlags} and {@link DataSchemaNode}.
     *
     * @param <A> Argument type ({@link Void} if statement does not have argument.)
     * @param <D> Class representing declared version of this statement.
     */
    public interface DataSchemaNodeMixin<A, D extends DeclaredStatement<A>>
            extends DataSchemaNode, CopyableMixin<A, D>, SchemaNodeMixin<A, D>, WhenConditionMixin<A, D> {
        @Override
        default boolean isConfiguration() {
            return (flags() & EffectiveStatementWithFlags.FlagsBuilder.CONFIGURATION) != 0;
        }
    }

    /**
     * Bridge between {@link EffectiveStatementWithFlags} and {@link DocumentedNode}.
     *
     * @param <A> Argument type ({@link Void} if statement does not have argument.)
     * @param <D> Class representing declared version of this statement.
     */
    public interface DocumentedNodeMixin<A, D extends DeclaredStatement<A>> extends Mixin<A, D>, DocumentedNode {
        /**
         * Bridge between {@link EffectiveStatementWithFlags} and
         * {@link org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus}.
         *
         * @param <A> Argument type ({@link Void} if statement does not have argument.)
         * @param <D> Class representing declared version of this statement.
         */
        interface WithStatus<A, D extends DeclaredStatement<A>>
                extends EffectiveStatementWithFlags<A, D>, DocumentedNodeMixin<A, D>, DocumentedNode.WithStatus {
            @Override
            default Status getStatus() {
                final int status = flags() & EffectiveStatementWithFlags.FlagsBuilder.MASK_STATUS;
                switch (status) {
                    case EffectiveStatementWithFlags.FlagsBuilder.STATUS_CURRENT:
                        return Status.CURRENT;
                    case EffectiveStatementWithFlags.FlagsBuilder.STATUS_DEPRECATED:
                        return Status.DEPRECATED;
                    case EffectiveStatementWithFlags.FlagsBuilder.STATUS_OBSOLETE:
                        return Status.OBSOLETE;
                    default:
                        throw new IllegalStateException("Illegal status " + status);
                }
            }
        }

        @Override
        default Optional<String> getDescription() {
            return findFirstEffectiveSubstatementArgument(DescriptionEffectiveStatement.class);
        }

        @Override
        default Optional<String> getReference() {
            return findFirstEffectiveSubstatementArgument(ReferenceEffectiveStatement.class);
        }

        @Override
        default List<UnknownSchemaNode> getUnknownSchemaNodes() {
            return filterEffectiveStatements(UnknownSchemaNode.class).collect(ImmutableList.toImmutableList());
        }
    }

    /**
     * Bridge between {@link EffectiveStatementWithFlags} and {@link MandatoryAware}.
     *
     * @param <A> Argument type ({@link Void} if statement does not have argument.)
     * @param <D> Class representing declared version of this statement.
     */
    public interface MandatoryMixin<A, D extends DeclaredStatement<A>>
            extends EffectiveStatementWithFlags<A, D>, MandatoryAware {
        @Override
        default boolean isMandatory() {
            return (flags() & EffectiveStatementWithFlags.FlagsBuilder.MANDATORY) != 0;
        }
    }

    /**
     * Bridge between {@link EffectiveStatementWithFlags} and {@link SchemaNode}.
     *
     * @param <A> Argument type ({@link Void} if statement does not have argument.)
     * @param <D> Class representing declared version of this statement.
     */
    public interface SchemaNodeMixin<A, D extends DeclaredStatement<A>>
            extends DocumentedNodeMixin.WithStatus<A, D>, SchemaNode {
        @Override
        default QName getQName() {
            return getPath().getLastComponent();
        }
    }

    /**
     * Bridge between {@link EffectiveStatementWithFlags} and {@code ordered-by} statement.
     *
     * @param <A> Argument type ({@link Void} if statement does not have argument.)
     * @param <D> Class representing declared version of this statement.
     */
    public interface UserOrderedMixin<A, D extends DeclaredStatement<A>> extends EffectiveStatementWithFlags<A, D> {
        default boolean userOrdered() {
            return (flags() & EffectiveStatementWithFlags.FlagsBuilder.USER_ORDERED) != 0;
        }
    }

    /**
     * Helper used to locate the effective {@code when} statement and exposing its argument as per
     * {@link WhenConditionAware}.
     *
     * @param <A> Argument type ({@link Void} if statement does not have argument.)
     * @param <D> Class representing declared version of this statement.
     */
    public interface WhenConditionMixin<A, D extends DeclaredStatement<A>> extends Mixin<A, D>, WhenConditionAware {
        @Override
        default Optional<RevisionAwareXPath> getWhenCondition() {
            return findFirstEffectiveSubstatementArgument(WhenEffectiveStatement.class);
        }
    }

    /**
     * Support interface for various mixins. Implementations are required to store 32bits worth of flags, which are
     * globally assigned to sub-interfaces -- thus providing storage for many low-cardinality properties.
     *
     * @param <A> Argument type ({@link Void} if statement does not have argument.)
     * @param <D> Class representing declared version of this statement.
     */
    public interface EffectiveStatementWithFlags<A, D extends DeclaredStatement<A>> extends Mixin<A, D> {
        /**
         * Return flags assicated with this statements. Flags can be built using {@link FlagsBuilder}.
         *
         * @return Flag field value (32 bits).
         */
        int flags();

        @NonNullByDefault
        final class FlagsBuilder implements Mutable {
            // We still have 25 flags remaining
            static final int STATUS_CURRENT       = 0x0001;
            static final int STATUS_DEPRECATED    = 0x0002;
            static final int STATUS_OBSOLETE      = 0x0003;
            static final int MASK_STATUS          = 0x0003;

            static final int CONFIGURATION        = 0x0004;
            static final int MANDATORY            = 0x0008;

            static final int AUGMENTING           = 0x0010;
            static final int ADDED_BY_USES        = 0x0020;
            private static final int MASK_HISTORY = 0x0030;

            static final int USER_ORDERED         = 0x0040;

            private int flags;

            public FlagsBuilder setConfiguration(final boolean config) {
                if (config) {
                    flags |= CONFIGURATION;
                } else {
                    flags &= ~CONFIGURATION;
                }
                return this;
            }

            public FlagsBuilder setHistory(final CopyHistory history) {
                int bits;
                if (history.contains(CopyType.ADDED_BY_USES_AUGMENTATION)) {
                    bits = AUGMENTING | ADDED_BY_USES;
                } else {
                    bits = 0;
                    if (history.contains(CopyType.ADDED_BY_AUGMENTATION)) {
                        bits |= AUGMENTING;
                    }
                    if (history.contains(CopyType.ADDED_BY_USES)) {
                        bits |= ADDED_BY_USES;
                    }
                }

                flags = flags & ~MASK_HISTORY | bits;
                return this;
            }

            public FlagsBuilder setMandatory(final boolean mandatory) {
                if (mandatory) {
                    flags |= MANDATORY;
                } else {
                    flags &= ~MANDATORY;
                }
                return this;
            }

            public FlagsBuilder setStatus(final Status status) {
                final int bits;
                switch (status) {
                    case CURRENT:
                        bits = STATUS_CURRENT;
                        break;
                    case DEPRECATED:
                        bits = STATUS_DEPRECATED;
                        break;
                    case OBSOLETE:
                        bits = STATUS_DEPRECATED;
                        break;
                    default:
                        throw new IllegalStateException("Unhandled status " + status);
                }

                flags = flags & ~MASK_STATUS | bits;
                return this;
            }

            public FlagsBuilder setUserOrdered(final boolean userOrdered) {
                if (userOrdered) {
                    flags |= USER_ORDERED;
                } else {
                    flags &= ~USER_ORDERED;
                }
                return this;
            }

            public int toFlags() {
                return flags;
            }
        }
    }
}
