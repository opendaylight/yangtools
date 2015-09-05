/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree.spi;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.opendaylight.yangtools.util.MutableOffsetMap;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

final class LazyContainerNode extends ContainerNode {
    protected LazyContainerNode(final NormalizedNode<?, ?> data, final Version version) {
        super(data, version, version);
    }

    @Override
    public Optional<TreeNode> getChild(final PathArgument key) {
        // We do not cache the instantiated node as it is dirt cheap
        final Optional<NormalizedNode<?, ?>> child = castData().getChild(key);
        if (child.isPresent()) {
            return Optional.of(TreeNodeFactory.createTreeNode(child.get(), getVersion()));
        }

        return Optional.absent();
    }

    private Map<PathArgument, TreeNode> fillChildren(final Map<PathArgument, TreeNode> children) {
        for (NormalizedNode<?, ?> child : castData().getValue()) {
            PathArgument id = child.getIdentifier();
            children.put(id, TreeNodeFactory.createTreeNode(child, getVersion()));
        }

        return children;
    }

    @Override
    public MutableTreeNode mutable() {
        /*
         * We are creating a mutable view of the data, which means that the version
         * is going to probably change -- and we need to make sure any unmodified
         * children retain it.
         *
         * The simplest thing to do is to just flush the amortized work and be done
         * with it.
         */
        return new Mutable(this, fillChildren(new HashMap<PathArgument, TreeNode>()));
    }

    private static final LoadingCache<DataNodeContainer, Map<PathArgument, Integer>> OFFSET_CACHE =
            CacheBuilder.newBuilder().weakValues()
            .build(new CacheLoader<DataNodeContainer, Map<PathArgument, Integer>>() {

                @Override
                public Map<PathArgument, Integer> load(final DataNodeContainer key) {
                    final Collection<DataSchemaNode> childSchemas = key.getChildNodes();
                    final Collection<PathArgument> childKeys = new ArrayList<>(childSchemas.size());
                    for (DataSchemaNode c : childSchemas) {
                        childKeys.add(NodeIdentifier.create(c.getQName()));
                    }

                    return OffsetMapCache.offsetsFor(childKeys);
                }
            });

    public MutableTreeNode mutable(final DataNodeContainer schema) {
        /*
         * Lists typically have an unbounded number of elements. One exception is a keyed list with leaves having
         * low value type cardinality.
         *
         * TODO: eventhough a list keyed by an enum or boolean is probably not very prevalent, we should take a look
         *       and optimize those lists, too.
         */
        if (schema instanceof ListSchemaNode) {
            return mutable();
        }

        final Map<PathArgument, Integer> offsetMap = OFFSET_CACHE.getUnchecked(schema);
        return new Mutable(this, fillChildren(new MutableOffsetMap<PathArgument, TreeNode>(offsetMap)));
    }

    @SuppressWarnings("unchecked")
    private NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>> castData() {
        return (NormalizedNodeContainer<?, PathArgument, NormalizedNode<?, ?>>) getData();
    }
}
