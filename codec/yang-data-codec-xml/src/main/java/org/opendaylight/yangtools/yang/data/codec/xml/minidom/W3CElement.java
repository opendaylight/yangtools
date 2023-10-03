/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml.minidom;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.w3c.dom.Node;

@NonNullByDefault
sealed interface W3CElement extends W3CNode, Element permits W3CContainerElement, W3CTextElement {
    @Override
    default List<Attribute> attributes() {
        return new W3CAttributeList(node().getAttributes());
    }

    @Override
    default @Nullable Attribute attribute(final @Nullable String namespace, final String localName) {
        final var attr = node().getAttributeNodeNS(namespace, localName);
        return attr != null ? new W3CAttribute(attr) : null;
    }

    @Override
    default @Nullable String attributeValue(final @Nullable String namespace, final String localName) {
        // We need to distinguish attribute presence vs. empty attributes
        final var attr = node().getAttributeNodeNS(namespace, localName);
        return attr != null ? attr.getValue() : null;
    }

    @Override
    org.w3c.dom.Element node();

    static Element of(final org.w3c.dom.Element element) {
        final var first = element.getFirstChild();
        if (first == null) {
            return new W3CContainerElement(element);
        }

        final var firstType = first.getNodeType();
        switch (firstType) {
            case Node.CDATA_SECTION_NODE:
            case Node.TEXT_NODE:
                return new W3CTextElement(element);
            case Node.ELEMENT_NODE:
                return new W3CContainerElement(element);
            default:
                // slow path
                break;
        }

        final var children = element.getChildNodes();
        for (int i = 0; ; ++i) {
            final var child = children.item(i);
            if (child == null) {
                // i.e. empty
                return new W3CContainerElement(element);
            }

            switch (child.getNodeType()) {
                case Node.CDATA_SECTION_NODE:
                case Node.TEXT_NODE:
                    return new W3CTextElement(element);
                case Node.ELEMENT_NODE:
                    return new W3CTextElement(element);
                default:
                    // just continue
            }
        }
    }
}
