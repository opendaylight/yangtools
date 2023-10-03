/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * A minimalistic model of an XML DOM tree, similar to {@link org.w3c.dom.Document}. Unlike the W3C DOM, this model is:
 * <ul>
 *   <li>immutable, i.e. there is no way to modify it structurally</li>
 *   <li>is a directed acyclic graph, i.e. there are no parent references and therefore no equivalent of
 *       {@link org.w3c.dom.Element#lookupNamespaceURI(String)} or {@link org.w3c.dom.Element#lookupPrefix(String)}</li>
 *   <li>there is no support for anything besides XML {@link attributes Attribute}, {@link elements Element} and text
 *       embedded therein</li>
 *   <li>XML {@link elements Element} cannot contain mixed text and elements. Each element is either:
 *     <ol>
 *       <li>a {@link ContainerElement}, containing other (or no) elements</li>
 *       <li>a {@link TextElement}, containing some mix of XML text and XML CDATA children</li>
 *     </ol>
 *   </li>
 * </ul>
 *
 * <p>
 * This model is sufficient to accurately express minimum part of
 * <a href="https://en.wikipedia.org/wiki/XML_Information_Set">XML Infoset</a> needed to contain any valid YANG-modeled
 * piece of data, such as contents of a YANG {@code anydata} node.
 */
package org.opendaylight.yangtools.yang.data.codec.xml.minidom;
