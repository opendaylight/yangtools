/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.sal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.IdentifiableItem;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

/**
 *
 * Location specific context for schema nodes, which contains codec specific
 * information to properly serialize / deserialize from Java YANG Binding data
 * to NormalizedNode data.
 *
 * Two core subtypes of codec context are available:
 * <ul>
 * <li>{@link LeafNodeCodecContext} - Context for nodes, which does not contain
 * any nested YANG modeled substructures.</li>
 * <li>{@link DataObjectCodecContext} - Context for nodes, which does contain
 * nested YANG modeled substructures. This context nodes contains context
 * for children nodes.</li>
 * </ul>
 *
 */
abstract class NodeCodecContext {

    /**
     * Returns Yang Instance Identifier Path Argument of current node
     *
     * @return DOM Path Argument of node
     */
    protected abstract YangInstanceIdentifier.PathArgument getDomPathArgument();

    /**
     *
     * Immutable factory, which provides access to runtime context,
     * create leaf nodes and provides path argument codecs.
     * <p>
     * During lifetime of factory all calls for same arguments to method must return
     * equal result (not necessary same instance of result).
     *
     */
    protected interface CodecContextFactory {

        /**
         * Returns immutable runtime context associated with this factory.
         * @return runtime context
         */
        BindingRuntimeContext getRuntimeContext();

        /**
         * Returns leaf nodes for supplied data container and parent class.
         *
         * @param type Binding type for which leaves should be loaded.
         * @param schema  Instantiated schema of binding type.
         * @return Map of local name to leaf node context.
         */
        ImmutableMap<String, LeafNodeCodecContext> getLeafNodes(Class<?> type, DataNodeContainer schema);

        /**
         * Returns Path argument codec for list item
         *
         * @param type Type of list item
         * @param schema Schema of list item
         * @return Path argument codec for supplied list item.
         */
        Codec<NodeIdentifierWithPredicates, IdentifiableItem<?, ?>> getPathArgumentCodec(Class<?> type,
                ListSchemaNode schema);

    }

    /**
     *
     * Serializes supplied Binding Path Argument
     * and adds all necessary YANG instance identifiers to supplied list.
     *
     * @param arg Bidning Path Argument
     * @param builder DOM Path argument.
     */
    protected void addYangPathArgument(final InstanceIdentifier.PathArgument arg,
            final List<YangInstanceIdentifier.PathArgument> builder) {
        if (builder != null) {
            builder.add(getDomPathArgument());
        }
    }

}
