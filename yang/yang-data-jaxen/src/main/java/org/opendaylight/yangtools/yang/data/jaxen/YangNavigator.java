/*
 * Copyright (c) 2014 Robert Varga.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import java.util.Iterator;
import org.jaxen.DefaultNavigator;
import org.jaxen.FunctionContext;
import org.jaxen.NamedAccessNavigator;
import org.jaxen.NamespaceContext;
import org.jaxen.Navigator;
import org.jaxen.UnsupportedAxisException;
import org.jaxen.XPath;
import org.jaxen.saxpath.SAXPathException;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodeContainer;

/**
 * A {@link Navigator} implementation for {@link YangXPath}s instantiated
 * at a particular node.
 */
final class YangNavigator extends DefaultNavigator implements NamedAccessNavigator {
    private static final long serialVersionUID = 1L;
    private final YangNamespaceContext namespaceContext;
    private final YangFunctionContext functionContext;

    YangNavigator(final YangNamespaceContext namespaceContext, final YangFunctionContext functionContext) {
        this.namespaceContext = Preconditions.checkNotNull(namespaceContext);
        this.functionContext = Preconditions.checkNotNull(functionContext);
    }

    FunctionContext createFunctionContext() {
        return functionContext;
    }

    NamespaceContext createNamespaceContext() {
        return namespaceContext;
    }

    private static final String getName(final Object obj) {
        final NormalizedNode<?, ?> node = (NormalizedNode<?, ?>) obj;
        return node.getNodeType().getLocalName();
    }

    private static final String getNamespace(final Object obj) {
        final NormalizedNode<?, ?> node = (NormalizedNode<?, ?>) obj;
        return node.getNodeType().getNamespace().toString();
    }

    private final String getQName(final Object obj) {
        final NormalizedNode<?, ?> node = (NormalizedNode<?, ?>) obj;
        final QName type = node.getNodeType();
        return namespaceContext.getPrefix(type.getModule()) + ':' + type.getLocalName();
    }

    @Override
    public String getElementNamespaceUri(final Object element) {
        return getNamespace(element);
    }

    @Override
    public String getElementName(final Object element) {
        return getName(element);
    }

    @Override
    public String getElementQName(final Object element) {
        return getQName(element);
    }

    @Override
    public String getAttributeNamespaceUri(final Object attr) {
        return getNamespace(attr);
    }

    @Override
    public String getAttributeName(final Object attr) {
        return getName(attr);
    }

    @Override
    public String getAttributeQName(final Object attr) {
        return getQName(attr);
    }

    @Override
    public boolean isDocument(final Object object) {
        return false;
    }

    @Override
    public boolean isElement(final Object object) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isAttribute(final Object object) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isNamespace(final Object object) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isComment(final Object object) {
        return false;
    }

    @Override
    public boolean isText(final Object object) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isProcessingInstruction(final Object object) {
        return false;
    }

    @Override
    public String getCommentStringValue(final Object comment) {
        return "";
    }

    @Override
    public String getElementStringValue(final Object element) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getAttributeStringValue(final Object attr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getNamespaceStringValue(final Object ns) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getTextStringValue(final Object text) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getNamespacePrefix(final Object ns) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public XPath parseXPath(final String xpath) throws SAXPathException {
        return new YangXPath(xpath, this);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Iterator getChildAxisIterator(final Object contextNode, final String localName, final String namespacePrefix,
            final String namespaceURI) throws UnsupportedAxisException {
        if (contextNode instanceof NormalizedNodeContainer) {
            final QName qname = QName.create(namespaceContext.getModule(namespacePrefix), localName);
            final Optional<NormalizedNode<?, ?>> child = ((NormalizedNodeContainer)contextNode).getChild(new NodeIdentifier(qname));

            // Caller is known to allow null returns and checks them first
            if (child.isPresent()) {
                return Iterators.singletonIterator(child.get());
            }
        }
        return null;
    }

    @Override
    public Iterator<NormalizedNode<?, ?>> getAttributeAxisIterator(final Object contextNode, final String localName, final String namespacePrefix,
            final String namespaceURI) throws UnsupportedAxisException {
        throw new UnsupportedAxisException("attribute");
    }
}
