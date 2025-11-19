/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import com.google.common.annotations.Beta;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.YangDataName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

/**
 * The contents of a {@code yang-data} template instance, as defined in
 * <a href="https://www.rfc-editor.org/rfc/rfc8040#page-80">RFC8040</a>'s {@code ietf-restconf} module.
 */
@NonNullByDefault
public non-sealed interface NormalizedYangData extends DataContainer {
    @Override
    default Class<NormalizedYangData> contract() {
        return NormalizedYangData.class;
    }

    @Override
    YangDataName name();

    /**
     * {@return the single DataContainerChild}
     *
     * @since 15.0.0
     */
    DataContainerChild child();

    @Override
    default List<DataContainerChild> body() {
        return List.of(child());
    }

    @Override
    default @Nullable DataContainerChild childByArg(final NodeIdentifier key) {
        final var child = child();
        return key.equals(child.name()) ? child : null;
    }

    /**
     * A builder of {@link NormalizedYangData}s.
     *
     * @since 14.0.21
     */
    @Beta
    interface Builder extends Mutable {
        /**
         * Set the {@code container data node}.
         *
         * @param containerDataNode the container data node
         * @return this builder
         */
        Builder setContainerDataNode(ChoiceNode containerDataNode);

        /**
         * Set the {@code container data node}.
         *
         * @param containerDataNode the container data node
         * @return this builder
         */
        Builder setContainerDataNode(ContainerNode containerDataNode);

        /**
         * Set the {@code container data node}.
         *
         * @param containerDataNode the container data node
         * @return this builder
         */
        Builder setContainerDataNode(ForeignDataNode<?> containerDataNode);

        /**
         * Set the {@code container data node}.
         *
         * @param containerDataNode the container data node
         * @return this builder
         */
        Builder setContainerDataNode(MapNode containerDataNode);

        /**
         * Set the {@code container data node}.
         *
         * @param containerDataNode the container data node
         * @return this builder
         */
        Builder setContainerDataNode(UnkeyedListNode containerDataNode);

        /**
         * {@return a built {@link NormalizedYangData}}
         * @throws IllegalStateException if this builder does not have sufficient state
         */
        NormalizedYangData build();
    }

    /**
     * A factory for concrete {@link Builder}s.
     *
     * @since 14.0.21
     */
    @Beta
    interface BuilderFactory {
        /**
         * Return a new {@link Builder} for specified {@link YangDataName}.
         *
         * @apiNote
         *   We choose to require {@link YangDataName} here and not set it separately in order to support schema-aware
         *   factories: the name is enough to establish the context in which to validate the instance.
         *
         * @param name the {@link YangDataName}
         * @return A new {@link Builder}
         */
        Builder newYangDataBuilder(YangDataName name);
    }
}
