/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.CollectionNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.ToNormalizedNodeParser;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

/**
 * Abstract(base) parser for ListNodes (MapNode, UnkeyedListNode), parses elements of type E.
 *
 * @param <E>
 *            type of elements to be parsed
 */
public abstract class ListNodeBaseParser<E, N extends NormalizedNode<?, ?>, O extends NormalizedNode<YangInstanceIdentifier.NodeIdentifier, ?>, S extends ListSchemaNode>
        implements ToNormalizedNodeParser<E, O, S> {

    @Override
    public O parse(Iterable<E> childNodes, S schema) {
        CollectionNodeBuilder<N, O> listBuilder = provideBuilder(schema);
        getParsingStrategy().addListIdentifier(schema.getQName());
        for (E childNode : childNodes) {
            N listChild = getListEntryNodeParser().parse(Collections.singletonList(childNode), schema);
            if (listChild != null) {
                listBuilder.withChild(listChild);
            }
        }
        getParsingStrategy().popListIdentifier();

        return listBuilder.build();
    }



    /**
     *
     * @return parser for inner ListEntryNodes used to parse every entry of ListNode, might be the same instance in case
     *         its immutable
     */
    protected abstract ToNormalizedNodeParser<E, N, S> getListEntryNodeParser();

    /**
     *
     * @return prepares builder which will contain entries of list according to concrete list type
     */
    protected abstract CollectionNodeBuilder<N, O> provideBuilder(S schema);

    protected abstract ParsingStrategy getParsingStrategy();
}
