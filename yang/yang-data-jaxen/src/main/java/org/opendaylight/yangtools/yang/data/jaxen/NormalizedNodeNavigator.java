/*
 * Copyright (c) 2014 Robert Varga.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.Iterators;
import com.google.common.io.BaseEncoding;
import java.util.AbstractMap.SimpleEntry;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.jaxen.DefaultNavigator;
import org.jaxen.NamedAccessNavigator;
import org.jaxen.NamespaceContext;
import org.jaxen.Navigator;
import org.jaxen.XPath;
import org.jaxen.saxpath.SAXPathException;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.AttributesContainer;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * A {@link Navigator} implementation for {@link YangXPath}s instantiated
 * on a particular root {@link NormalizedNode}.
 */
final class NormalizedNodeNavigator extends DefaultNavigator implements NamedAccessNavigator {
    private static final long serialVersionUID = 1L;
    private static final Joiner JOINER = Joiner.on(" ").skipNulls();
    private final YangNamespaceContext namespaceContext;

    NormalizedNodeNavigator(final YangNamespaceContext namespaceContext) {
        this.namespaceContext = Preconditions.checkNotNull(namespaceContext);
    }

    NamespaceContext getNamespaceContext() {
        return namespaceContext;
    }

    private static NormalizedNode<?, ?> elementNode(final Object obj) {
        Verify.verify(obj instanceof NormalizedNode, "Unhandled element %s", obj);
        return ((NormalizedNode<?, ?>) obj);
    }

    private static NormalizedNode<?, ?> contextNode(final Object context) {
        Verify.verify(context instanceof NormalizedNodeContext, "Unhandled context node %s", context);
        return ((NormalizedNodeContext) context).getNormalizedNode();
    }

    private QName resolveQName(final NormalizedNode<?, ?> node, final String prefix, final String localName) {
        final QNameModule module;
        if (prefix.isEmpty()) {
            module = node.getNodeType().getModule();
        } else {
            module = namespaceContext.getModule(prefix);
        }

        return QName.create(module, localName);
    }

    @SuppressWarnings("unchecked")
    private static Entry<QName, String> attribute(final Object attr) {
        Verify.verify(attr instanceof Entry, "Unhandled attribute %s", attr);
        return (Entry<QName, String>) attr;
    }

    @Override
    public String getElementNamespaceUri(final Object element) {
        return elementNode(element).getNodeType().getNamespace().toString();
    }

    @Override
    public String getElementName(final Object element) {
        return elementNode(element).getNodeType().getLocalName();
    }

    @Override
    public String getElementQName(final Object element) {
        return namespaceContext.jaxenQName(elementNode(element).getNodeType());
    }

    @Override
    public String getAttributeNamespaceUri(final Object attr) {
        return attribute(attr).getKey().getNamespace().toString();
    }

    @Override
    public String getAttributeName(final Object attr) {
        return attribute(attr).getKey().getLocalName();
    }

    @Override
    public String getAttributeQName(final Object attr) {
        return namespaceContext.jaxenQName(attribute(attr).getKey());
    }

    @Override
    public boolean isDocument(final Object object) {
        if (!(object instanceof NormalizedNode)) {
            return false;
        }

        // Assumes root node does not have an identifier
        final NormalizedNode<?, ?> node = (NormalizedNode<?, ?>) object;
        return node.getIdentifier() == null;
    }

    @Override
    public boolean isElement(final Object object) {
        return object instanceof NormalizedNode;
    }

    @Override
    public boolean isAttribute(final Object object) {
        return object instanceof Entry;
    }

    @Override
    public boolean isNamespace(final Object object) {
        return false;
    }

    @Override
    public boolean isComment(final Object object) {
        return false;
    }

    @Override
    public boolean isText(final Object object) {
        return false;
    }

    @Override
    public boolean isProcessingInstruction(final Object object) {
        return false;
    }

    @Override
    public String getCommentStringValue(final Object comment) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getElementStringValue(final Object element) {
        final NormalizedNode<?, ?> node = elementNode(element);
        if (node instanceof LeafNode || node instanceof LeafSetEntryNode) {
            final Object value = node.getValue();

            // TODO: This is a rather poor approximation of what the codec infrastructure, but it should be sufficient
            //       to work for now. Tracking SchemaPath will mean we will need to wrap each NormalizedNode with a
            //       corresponding SchemaPath. That in turn would complicate this class and result in a lot of object
            //       allocations.
            if (value instanceof byte[]) {
                // Binary
                return BaseEncoding.base64().encode((byte[]) value);
            }
            if (value instanceof Set) {
                // Bits
                return JOINER.join((Set<?>)value);
            }
            if (value != null) {
                // Everything else...
                return String.valueOf(value);
            }
        }

        return "";
    }

    @Override
    public String getAttributeStringValue(final Object attr) {
        return attribute(attr).getValue();
    }

    @Override
    public String getNamespaceStringValue(final Object ns) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getTextStringValue(final Object text) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getNamespacePrefix(final Object ns) {
        throw new UnsupportedOperationException();
    }

    @Override
    public XPath parseXPath(final String xpath) throws SAXPathException {
        return new YangXPath(xpath, this);
    }

    @Override
    public Iterator<?> getChildAxisIterator(final Object contextNode, final String localName, final String namespacePrefix,
            final String namespaceURI) {

        final NormalizedNode<?, ?> node = contextNode(contextNode);
        if (!(node instanceof DataContainerNode)) {
            return null;
        }

        final QName qname = resolveQName(node, namespacePrefix, localName);
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final Optional<NormalizedNode<?, ?>> maybeChild = ((DataContainerNode)contextNode).getChild(new NodeIdentifier(qname));
        if (!maybeChild.isPresent()) {
            return null;
        }

        // The child may be a structural node
        final NormalizedNode<?, ?> child = maybeChild.get();
        if (child instanceof MapNode) {
            return ((MapNode)child).getValue().iterator();
        }
        if (child instanceof LeafSetNode) {
            return ((LeafSetNode<?>)child).getValue().iterator();
        }

        return Iterators.singletonIterator(child);
    }

    @Override
    public Iterator<?> getAttributeAxisIterator(final Object contextNode, final String localName, final String namespacePrefix,
            final String namespaceURI) {
        final NormalizedNode<?, ?> node = contextNode(contextNode);
        if (node instanceof AttributesContainer) {
            final Map<QName, String> attributes = ((AttributesContainer) node).getAttributes();
            if (attributes.isEmpty()) {
                return null;
            }

            final QName qname = resolveQName(node, namespacePrefix, localName);
            final String value = attributes.get(qname);
            return value == null ? null : Iterators.singletonIterator(new SimpleEntry<>(qname, value));
        }

        return null;
    }
}
