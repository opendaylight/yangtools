/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.parser;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.ListEntryNodeBaseParser;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.NodeParserDispatcher;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.DomUtils;
import org.w3c.dom.Element;

@Deprecated
abstract class ListEntryNodeDomParser<P extends YangInstanceIdentifier.PathArgument, N extends DataContainerNode<P>> extends ListEntryNodeBaseParser<P, Element, N> {

    private final NodeParserDispatcher<Element> dispatcher;

    ListEntryNodeDomParser(final NodeParserDispatcher<Element> dispatcher) {
        this.dispatcher = Preconditions.checkNotNull(dispatcher);
    }

    ListEntryNodeDomParser(final BuildingStrategy<P, N> buildingStrategy, final NodeParserDispatcher<Element> dispatcher) {
        super(buildingStrategy);
        this.dispatcher = dispatcher;
    }

    @Override
    protected LinkedListMultimap<QName, Element> mapChildElements(final Iterable<Element> elements) {
        return DomUtils.mapChildElementsForSingletonNode(elements.iterator().next());
    }

    @Override
    protected NodeParserDispatcher<Element> getDispatcher() {
        return dispatcher;
    }

    @Override
    protected Map<QName, String> getAttributes(final Element element) {
        return DomUtils.toAttributes(element.getAttributes());
    }
}
