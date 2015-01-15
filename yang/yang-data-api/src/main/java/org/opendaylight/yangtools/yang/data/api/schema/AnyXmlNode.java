/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.opendaylight.yangtools.yang.data.api.AttributesContainer;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.Node;

/**
 * Anyxml normalizedNode.
 *
 * <p>
 * This node contains values for anyxml as either SimpleNode or CompositeNode.
 * The concrete value depends on the current value of anyxml node.
 * </p>
 *
 * <p>
 * For yang node: anyxml foo;
 *
 * <ul>
 * <li>
 * with xml value:
 * <pre>
 * {@code <foo>justSomeString</foo>}
 * </pre>
 * this AnyXmlNode returns SimpleNode with QName{namespace=someNamespace, revision=someRevision, localName=foo} and value="justSomeString"
 * </li>
 *
 * <li>
 * but with xml value:
 * <pre>
 * {@code <foo><bar>stringInXml</bar></foo>}
 * </pre>
 * this AnyXmlNode returns CompositeNode with QName{}namespace=someNamespace, revision=someRevision, localName=foo}
 * and values [SimpleNode with QName{}namespace=someNamespace, revision=someRevision, localName=bar} and value="stringInXml"]
 * </li>
 *
 * </ul>
 */
public interface AnyXmlNode extends AttributesContainer, DataContainerChild<NodeIdentifier, Node<?>> {

    @Override
    NodeIdentifier getIdentifier();

    /**
     * @return anyxml node value represented as SimpleNode or CompositeNode.
     * Returned node contains top level element that duplicates the anyxml node.
     */
    @Override
    Node<?> getValue();
}
