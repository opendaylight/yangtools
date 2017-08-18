/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Joiner;
import com.google.common.base.Verify;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.io.BaseEncoding;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import org.jaxen.DefaultNavigator;
import org.jaxen.NamedAccessNavigator;
import org.jaxen.Navigator;
import org.jaxen.UnsupportedAxisException;
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
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * A {@link Navigator} implementation for YANG XPaths instantiated on a particular root {@link NormalizedNode}.
 */
final class NormalizedNodeNavigator extends DefaultNavigator implements NamedAccessNavigator {
    private static final long serialVersionUID = 1L;
    private static final Joiner JOINER = Joiner.on(" ").skipNulls();
    private final ConverterNamespaceContext namespaceContext;
    private final JaxenDocument document;

    NormalizedNodeNavigator(final ConverterNamespaceContext context, final JaxenDocument document) {
        this.namespaceContext = requireNonNull(context);
        this.document = document;
    }

    private static NormalizedNodeContext cast(final Object context) {
        Verify.verify(context instanceof NormalizedNodeContext, "Unhandled context node %s", context);
        return (NormalizedNodeContext) context;
    }

    private static NormalizedNode<?, ?> contextNode(final Object context) {
        return cast(context).getNode();
    }

    private QName resolveQName(final NormalizedNode<?, ?> node, final String prefix, final String localName) {
        final QNameModule module;
        if (prefix.isEmpty()) {
            module = node.getNodeType().getModule();
        } else {
            module = namespaceContext.convert(prefix);
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
        return contextNode(element).getNodeType().getNamespace().toString();
    }

    @Override
    public String getElementName(final Object element) {
        return contextNode(element).getNodeType().getLocalName();
    }

    @Override
    public String getElementQName(final Object element) {
        return namespaceContext.jaxenQName(contextNode(element).getNodeType());
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
    public NormalizedNodeContext getDocumentNode(final Object contextNode) {
        NormalizedNodeContext ctx = cast(contextNode);
        while (ctx.getParent() != null) {
            ctx = ctx.getParent();
        }

        return ctx;
    }

    @Override
    public boolean isDocument(final Object object) {
        return cast(object).getParent() == null;
    }

    @Override
    public boolean isElement(final Object object) {
        return object instanceof NormalizedNodeContext;
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
        return object instanceof String;
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
        final NormalizedNode<?, ?> node = contextNode(element);
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
        return text.toString();
    }

    @Override
    public String getNamespacePrefix(final Object ns) {
        throw new UnsupportedOperationException();
    }

    @Override
    public XPath parseXPath(final String xpath) throws SAXPathException {
        // FIXME: need to bind YangXPath probably
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<NormalizedNodeContext> getChildAxisIterator(final Object contextNode) {
        final NormalizedNodeContext ctx = cast(contextNode);
        final NormalizedNode<?, ?> node = ctx.getNode();
        if (node instanceof DataContainerNode) {
            return Iterators.transform(((DataContainerNode<?>) node).getValue().iterator(), ctx);
        }

        return null;
    }

    @Override
    public Iterator<NormalizedNodeContext> getChildAxisIterator(final Object contextNode, final String localName,
            final String namespacePrefix, final String namespaceURI) {
        final NormalizedNodeContext ctx = cast(contextNode);
        final NormalizedNode<?, ?> node = ctx.getNode();
        if (!(node instanceof DataContainerNode)) {
            return null;
        }

        final QName qname = resolveQName(node, namespacePrefix, localName);
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final Optional<NormalizedNode<?, ?>> maybeChild = ((DataContainerNode)node).getChild(new NodeIdentifier(qname));
        if (!maybeChild.isPresent()) {
            return null;
        }

        // The child may be a structural node
        final NormalizedNode<?, ?> child = maybeChild.get();
        if (child instanceof MapNode) {
            return Iterators.transform(((MapNode)child).getValue().iterator(), ctx);
        }
        if (child instanceof LeafSetNode) {
            return Iterators.transform(((LeafSetNode<?>)child).getValue().iterator(), ctx);
        }

        return Iterators.singletonIterator(ctx.apply(child));
    }

    @Override
    public Iterator<? extends Entry<?, ?>> getAttributeAxisIterator(final Object contextNode) {
        final NormalizedNode<?, ?> node = contextNode(contextNode);
        if (node instanceof AttributesContainer) {
            final Map<QName, String> attributes = ((AttributesContainer) node).getAttributes();
            if (attributes.isEmpty()) {
                return null;
            }

            return attributes.entrySet().iterator();
        }

        return null;
    }

    @Override
    public Iterator<? extends Entry<?, ?>> getAttributeAxisIterator(final Object contextNode, final String localName,
            final String namespacePrefix, final String namespaceURI) {
        final NormalizedNode<?, ?> node = contextNode(contextNode);
        if (node instanceof AttributesContainer) {
            final Map<QName, String> attributes = ((AttributesContainer) node).getAttributes();
            if (attributes.isEmpty()) {
                return null;
            }

            final QName qname = resolveQName(node, namespacePrefix, localName);
            final String value = attributes.get(qname);
            return value == null ? null : Iterators.singletonIterator(new SimpleImmutableEntry<>(qname, value));
        }

        return null;
    }

    @Override
    public Iterator<NormalizedNodeContext> getParentAxisIterator(final Object contextNode) {
        final NormalizedNodeContext parent = cast(contextNode).getParent();
        return parent == null ? null : Iterators.singletonIterator(parent);
    }

    @Override
    public Iterator<NormalizedNodeContext> getAncestorAxisIterator(final Object contextNode)
            throws UnsupportedAxisException {
        final NormalizedNodeContext parent = cast(contextNode).getParent();
        return parent == null ? null : new NormalizedNodeContextIterator(parent);
    }

    @Override
    public Iterator<NormalizedNodeContext> getSelfAxisIterator(final Object contextNode)
            throws UnsupportedAxisException {
        return Iterators.singletonIterator(cast(contextNode));
    }

    @Override
    public Iterator<NormalizedNodeContext> getAncestorOrSelfAxisIterator(final Object contextNode)
            throws UnsupportedAxisException {
        return new NormalizedNodeContextIterator(cast(contextNode));
    }

    @Override
    public NormalizedNodeContext getParentNode(final Object contextNode) throws UnsupportedAxisException {
        return cast(contextNode).getParent();
    }

    NormalizedNode<?, ?> getRootNode() {
        return document.getRootNode();
    }

    @Nonnull
    SchemaContext getSchemaContext() {
        return document.getSchemaContext();
    }

    private static final class NormalizedNodeContextIterator extends UnmodifiableIterator<NormalizedNodeContext> {
        private NormalizedNodeContext next;

        NormalizedNodeContextIterator(final NormalizedNodeContext initial) {
            this.next = requireNonNull(initial);
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public NormalizedNodeContext next() {
            if (next == null) {
                throw new NoSuchElementException();
            }

            final NormalizedNodeContext ret = next;
            next = next.getParent();
            return ret;
        }
    }
}
