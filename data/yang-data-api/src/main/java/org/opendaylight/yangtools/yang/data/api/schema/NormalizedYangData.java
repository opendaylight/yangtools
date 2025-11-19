/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.YangDataName;

/**
 * The contents of a {@code yang-data} template instance, as defined in
 * <a href="https://www.rfc-editor.org/rfc/rfc8040#page-80">RFC8040</a>'s {@code ietf-restconf} module.
 */
// FIXME: YANGTOOLS-1744: add child() and default DataContainer method implementations
@NonNullByDefault
public non-sealed interface NormalizedYangData extends DataContainer {
    @Override
    default Class<NormalizedYangData> contract() {
        return NormalizedYangData.class;
    }

    @Override
    YangDataName name();

    /**
     * A builder of {@link NormalizedYangData}s.
     *
     * @since 14.0.21
     */
    @Beta
    interface Builder extends Mutable {
        /**
         * Set the single {@code container data node} child to specified {@link AnydataNode}.
         *
         * @param child the {@link AnydataNode}
         * @return this builder
         */
        Builder setChild(AnydataNode<?> child);

        /**
         * Set the single {@code container data node} child to specified {@link AnyxmlNode}.
         *
         * @param child the {@link AnyxmlNode}
         * @return this builder
         */
        Builder setChild(AnyxmlNode<?> child);

        /**
         * Set the single {@code container data node} child to specified {@link ChoiceNode}.
         *
         * @param child the {@link ChoiceNode}
         * @return this builder
         */
        Builder setChild(ChoiceNode child);

        /**
         * Set the single {@code container data node} child to specified {@link ContainerNode}.
         *
         * @param child the {@link ContainerNode}
         * @return this builder
         */
        Builder setChild(ContainerNode child);

        /**
         * Set the single {@code container data node} child to specified {@link MapNode}.
         *
         * @param child the {@link MapNode}
         * @return this builder
         */
        Builder setChild(MapNode child);

        /**
         * Set the single {@code container data node} child to specified {@link UnkeyedListNode}.
         *
         * @param child the {@link UnkeyedListNode}
         * @return this builder
         */
        Builder setChild(UnkeyedListNode child);

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
         *     We choose to require {@link YangDataName} here and not set it separately in order to support schema-aware
         *     factories: the name is enough to establish the context in which to validate the instance.
         *
         * @param name the {@link YangDataName}
         * @return A new {@link Builder}
         */
        Builder newYangDataBuilder(YangDataName name);
    }
}
