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
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AddedByUsesAware;
import org.opendaylight.yangtools.yang.model.api.CopyableNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.MandatoryAware;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyHistory;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;

@Beta
public interface EffectiveStatementFlags {

    int flags();

    interface AddedByUsesMixin extends EffectiveStatementFlags, AddedByUsesAware {
        @Override
        default boolean isAddedByUses() {
            return (flags() & Builder.ADDED_BY_USES) != 0;
        }
    }

    interface CopyableMixin extends AddedByUsesMixin, CopyableNode {
        @Override
        default boolean isAugmenting() {
            return (flags() & Builder.AUGMENTING) != 0;
        }
    }

    interface ConfigurationMixin extends DataSchemaNode, CopyableMixin {
        @Override
        default boolean isConfiguration() {
            return (flags() & Builder.CONFIGURATION) != 0;
        }
    }

    interface MandatoryMixin extends EffectiveStatementFlags, MandatoryAware {
        @Override
        default boolean isMandatory() {
            return (flags() & Builder.MANDATORY) != 0;
        }
    }

    interface StatusMixin extends EffectiveStatementFlags, DocumentedNode.WithStatus {
        @Override
        default Status getStatus() {
            final int status = flags() & Builder.MASK_STATUS;
            switch (status) {
                case Builder.STATUS_CURRENT:
                    return Status.CURRENT;
                case Builder.STATUS_DEPRECATED:
                    return Status.DEPRECATED;
                case Builder.STATUS_OBSOLETE:
                    return Status.OBSOLETE;
                default:
                    throw new IllegalStateException("Illegal status " + status);
            }
        }
    }

    interface DocumentedNodeMixin<A, D extends DeclaredStatement<A>> extends EffectiveStatement<A, D>,
            DocumentedNode, StatusMixin {
        @Override
        default Optional<String> getDescription() {
            return findFirstEffectiveSubstatementArgument(DescriptionEffectiveStatement.class);
        }

        @Override
        default Optional<String> getReference() {
            return findFirstEffectiveSubstatementArgument(ReferenceEffectiveStatement.class);
        }

        @Override
        default ImmutableList<UnknownSchemaNode> getUnknownSchemaNodes() {
            return effectiveSubstatements().stream()
                    .filter(UnknownSchemaNode.class::isInstance)
                    .map(UnknownSchemaNode.class::cast)
                    .collect(ImmutableList.toImmutableList());
        }
    }

    interface SchemaNodeMixin<A, D extends DeclaredStatement<A>> extends DocumentedNodeMixin<A, D>, SchemaNode {
        @Override
        default QName getQName() {
            return getPath().getLastComponent();
        }
    }

    interface UserOrderedMixin extends EffectiveStatementFlags {
        default boolean userOrdered() {
            return (flags() & Builder.USER_ORDERED) != 0;
        }
    }

    @NonNullByDefault
    final class Builder implements Mutable {
        // We still have 25 flags remaining
        static final int STATUS_CURRENT       = 0x0001;
        static final int STATUS_DEPRECATED    = 0x0002;
        static final int STATUS_OBSOLETE      = 0x0003;
        static final int MASK_STATUS  = 0x0003;

        static final int CONFIGURATION        = 0x0004;
        static final int MANDATORY            = 0x0008;

        static final int AUGMENTING           = 0x0010;
        static final int ADDED_BY_USES        = 0x0020;
        private static final int MASK_HISTORY = 0x0030;

        static final int USER_ORDERED         = 0x0040;

        private int flags;

        public Builder setConfiguration(final boolean config) {
            if (config) {
                flags |= CONFIGURATION;
            } else {
                flags &= ~CONFIGURATION;
            }
            return this;
        }

        public Builder setHistory(final CopyHistory history) {
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

        public Builder setMandatory(final boolean mandatory) {
            if (mandatory) {
                flags |= MANDATORY;
            } else {
                flags &= ~MANDATORY;
            }
            return this;
        }

        public Builder setStatus(final Status status) {
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

        public Builder setUserOrdered(final boolean userOrdered) {
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
