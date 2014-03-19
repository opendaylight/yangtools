/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.parser;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.ContainerNodeBaseParser;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser.NodeParserDispatcher;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.DomUtils;
import org.w3c.dom.Element;

import com.google.common.base.Preconditions;
import com.google.common.collect.LinkedListMultimap;

public final class ContainerNodeDomParser extends ContainerNodeBaseParser<Element> {

    private final XmlCodecProvider codecProvider;

    public ContainerNodeDomParser(XmlCodecProvider codecProvider) {
        this.codecProvider = Preconditions.checkNotNull(codecProvider);
    }

    @Override
    protected NodeParserDispatcher<Element> getDispatcher() {
        return DomNodeDispatcher.getInstance(codecProvider);
    }

    @Override
    protected LinkedListMultimap<QName, Element> mapChildElements(Iterable<Element> elements) {
        return DomUtils.mapChildElementsForSingletonNode(elements.iterator().next());
    }

}
