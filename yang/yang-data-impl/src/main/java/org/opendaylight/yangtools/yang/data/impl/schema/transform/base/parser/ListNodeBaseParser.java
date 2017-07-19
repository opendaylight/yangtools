/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser;

import java.util.Collections;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.ToNormalizedNodeParser;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

/**
 * Abstract(base) parser for ListNodes (MapNode, UnkeyedListNode), parses elements of type E.
 *
 * @param <E>
 *            type of elements to be parsed
 * @deprecated Use yang-data-codec-xml instead.
 */
@Deprecated
public abstract class ListNodeBaseParser<E, N extends NormalizedNode<?, ?>, O extends NormalizedNode<YangInstanceIdentifier.NodeIdentifier, ?>, S extends ListSchemaNode>
        implements ExtensibleParser<YangInstanceIdentifier.NodeIdentifier, E, O, S> {

    private final BuildingStrategy<YangInstanceIdentifier.NodeIdentifier, O> buildingStrategy;

    public ListNodeBaseParser() {
        buildingStrategy = new SimpleListNodeBuildingStrategy<>();
    }

    public ListNodeBaseParser(final BuildingStrategy<YangInstanceIdentifier.NodeIdentifier, O> buildingStrategy) {
        this.buildingStrategy = buildingStrategy;
    }

    @Override
    public O parse(final Iterable<E> childNodes, final S schema) {
        CollectionNodeBuilder<N, O> listBuilder = provideBuilder(schema);

        buildingStrategy.prepareAttributes(Collections.emptyMap(), listBuilder);

        for (E childNode : childNodes) {
            N listChild = getListEntryNodeParser().parse(Collections.singletonList(childNode), schema);
            if (listChild != null) {
                listBuilder.withChild(listChild);
            }
        }

        return buildingStrategy.build(listBuilder);
    }

    /**
     *
     * @return parser for inner ListEntryNodes used to parse every entry of ListNode, might be the same instance in case
     *         its immutable
     */
    protected abstract ToNormalizedNodeParser<E, N, S> getListEntryNodeParser();

    /**
     *
     * @param schema schema for list node
     * @return prepares builder which will contain entries of list according to concrete list type
     */
    protected abstract CollectionNodeBuilder<N, O> provideBuilder(S schema);

    @Override
    public BuildingStrategy<YangInstanceIdentifier.NodeIdentifier, O> getBuildingStrategy() {
        return buildingStrategy;
    }

    public static class SimpleListNodeBuildingStrategy<O extends NormalizedNode<YangInstanceIdentifier.NodeIdentifier, ?>> implements BuildingStrategy<YangInstanceIdentifier.NodeIdentifier, O> {
        @Override
        public O build(final NormalizedNodeBuilder<YangInstanceIdentifier.NodeIdentifier, ?, O> builder) {
            return builder.build();
        }

        @Override
        public void prepareAttributes(final Map<QName, String> attributes, final NormalizedNodeBuilder<YangInstanceIdentifier.NodeIdentifier, ?, O> containerBuilder) {
            // NOOP
        }
    }
}
