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
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.ChoiceNodeBaseParser;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.NodeParserDispatcher;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.DomUtils;
import org.w3c.dom.Element;

@Deprecated
final class ChoiceNodeDomParser extends ChoiceNodeBaseParser<Element> {

    private final NodeParserDispatcher<Element> dispatcher;

    ChoiceNodeDomParser(final NodeParserDispatcher<Element> dispatcher) {
        this.dispatcher = Preconditions.checkNotNull(dispatcher);
    }

    ChoiceNodeDomParser(final NodeParserDispatcher<Element> dispatcher, final BuildingStrategy<YangInstanceIdentifier.NodeIdentifier, ChoiceNode> buildingStrategy) {
        super(buildingStrategy);
        this.dispatcher = Preconditions.checkNotNull(dispatcher);
    }

    @Override
    protected LinkedListMultimap<QName, Element> mapChildElements(final Iterable<Element> xml) {
        return DomUtils.mapChildElements(xml);
    }

    @Override
    protected NodeParserDispatcher<Element> getDispatcher() {
        return dispatcher;
    }

}
