/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import static com.google.common.base.Verify.verify;
import static org.opendaylight.yangtools.yang.parser.rfc7950.repo.StatementSourceReferenceHandler.extractRef;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class YinSourceInfoExtractor extends SourceInfoExtractor<Node> {

    private final ImmutableMap<String, Node> uniqueRootElements;

    private YinSourceInfoExtractor(final Node root, final SourceIdentifier rootIdentifier) {
        super(root, rootIdentifier);
        uniqueRootElements = mapUniqueChildElementsOf(root);
    }

    public static YinSourceInfoExtractor of(final Node root, final SourceIdentifier rootIdentifier) {
        verify(root.getNodeType() == Node.ELEMENT_NODE, "Node is not an Element node");
        return new YinSourceInfoExtractor(root, rootIdentifier);
    }

    @Override
    String extractRootType() {
        return root().getLocalName();
    }

    @Override
    Unqualified extractModulePrefix() {
        final Node node = uniqueRootElements.get(PREFIX);
        if (node != null) {
            return readNodeQName(node);
        }
        throw new IllegalArgumentException("No prefix statement in " + refOf(root()));
    }

    @Override
    XMLNamespace extractNamespace() {
        final Node node = uniqueRootElements.get(NAMESPACE);
        if (node != null) {
            return readNamespace(node);
        }
        throw new IllegalArgumentException("No namespace statement in " + refOf(root()));
    }

    @Override
    SourceDependency.BelongsTo extractBelongsTo() {
        final Node belongsToNode = uniqueRootElements.get(BELONGS_TO);
        if (belongsToNode != null) {
            return readBelongsTo(belongsToNode);
        }
        throw new IllegalArgumentException("No belongs-to statement in " + refOf(root()));
    }

    @Override
    Unqualified extractName() {
        return rootId().name();
    }

    @Override
    YangVersion extractYangVersion() {
        final Node child = uniqueRootElements.get(YANG_VERSION);
        return child != null ? readYangVersion(child) : null;
    }

    @Override
    void extractRevisions(SourceInfo.Builder<?, ?> builder) {
        for (final Node revisionNode : getChildrenOfType(root(), REVISION)) {
            builder.addRevision(extractRevision(revisionNode));
        }
    }

    @Override
    void extractIncludes(SourceInfo.Builder<?, ?> builder) {
        for (final Node includeNode : getChildrenOfType(root(), INCLUDE)) {
            builder.addInclude(extractInclude(includeNode));
        }
    }

    @Override
    void extractImports(SourceInfo.Builder<?, ?> builder) {
        for (final Node importNode : getChildrenOfType(root(), IMPORT)) {
            builder.addImport(extractImport(importNode));
        }
    }

    private List<Node> getChildrenOfType(final Node parent, final String type) {
        final NodeList childNodes = parent.getChildNodes();
        final LinkedList<Node> children = new LinkedList<>();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node child = childNodes.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if (child.getLocalName().equals(type)) {
                    children.add(child);
                }
            }
        }
        return children;
    }

    private StatementSourceReference refOf(final Node node) {
        return extractRef((Element) node);
    }

    private Revision extractRevision(final Node revisionNode) {
        return Revision.of(revisionNode.getAttributes().item(0).getNodeValue());

    }

    private SourceDependency.Include extractInclude(final Node includeNode) {
        final Unqualified name = readNodeQName(includeNode);
        Revision revision = null;

        final Node revisionNode = getChildNode(includeNode, REVISION_DATE);
        if (revisionNode != null) {
            revision = readRevisionDate(revisionNode);
        }
        return new SourceDependency.Include(name, revision);
    }

    private Unqualified readNodeQName(final Node node) {
        return Unqualified.of(node.getAttributes().item(0).getNodeValue());
    }

    private SourceDependency.Import extractImport(final Node importNode) {
        final Unqualified name = readNodeQName(importNode);
        Unqualified prefix = null;
        Revision revision = null;

        final Node prefixNode = getChildNode(importNode, PREFIX);
        if (prefixNode != null) {
            prefix = readNodeQName(prefixNode);
        } else {
            throw new IllegalArgumentException("No prefix statement in " + refOf(importNode));
        }

        final Node revisionNode = getChildNode(importNode, REVISION_DATE);
        if (revisionNode != null) {
            revision = readRevisionDate(revisionNode);
        }

        return new SourceDependency.Import(name, prefix, revision);
    }

    private ImmutableMap<String, Node> mapUniqueChildElementsOf(final Node root) {
        final NodeList childNodes = root.getChildNodes();
        final Map<String, Node> childMap = new HashMap<>();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node child = childNodes.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                childMap.putIfAbsent(child.getLocalName(), child);
            }
        }
        return ImmutableMap.copyOf(childMap);
    }

    private Node getChildNode(final Node root, final String nodeName) {
        final NodeList childNodes = root.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node child = childNodes.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(nodeName)) {
                return child;
            }
        }
        return null;
    }

    private YangVersion readYangVersion(final Node node) {
        return YangVersion.forString(node.getAttributes().item(0).getNodeValue());
    }

    private Revision readRevisionDate(final Node node) {
        return Revision.of(node.getAttributes().item(0).getNodeValue());
    }

    private XMLNamespace readNamespace(final Node node) {
        return XMLNamespace.of(node.getAttributes().item(0).getNodeValue());
    }

    private SourceDependency.BelongsTo readBelongsTo(final Node belongsToNode) {
        final Unqualified name = readNodeQName(belongsToNode);
        final Node prefixNode = getChildNode(belongsToNode, PREFIX);
        if (prefixNode != null) {
            return new SourceDependency.BelongsTo(name, readNodeQName(prefixNode));
        } else {
            throw new IllegalArgumentException("No prefix statement in " + refOf(belongsToNode));
        }
    }
}
