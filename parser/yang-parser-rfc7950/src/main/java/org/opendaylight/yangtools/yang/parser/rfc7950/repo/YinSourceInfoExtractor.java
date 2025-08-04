/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import static org.opendaylight.yangtools.yang.parser.rfc7950.repo.StatementSourceReferenceHandler.extractRef;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Referenced;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.DetailedRevision;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class YinSourceInfoExtractor {

    private static final String BELONGS_TO = YangStmtMapping.BELONGS_TO.getStatementName().getLocalName();
    private static final String IMPORT = YangStmtMapping.IMPORT.getStatementName().getLocalName();
    private static final String INCLUDE = YangStmtMapping.INCLUDE.getStatementName().getLocalName();
    private static final String MODULE = YangStmtMapping.MODULE.getStatementName().getLocalName();
    private static final String NAMESPACE = YangStmtMapping.NAMESPACE.getStatementName().getLocalName();
    private static final String PREFIX = YangStmtMapping.PREFIX.getStatementName().getLocalName();
    private static final String REVISION = YangStmtMapping.REVISION.getStatementName().getLocalName();
    private static final String REVISION_DATE = YangStmtMapping.REVISION_DATE.getStatementName().getLocalName();
    private static final String SUBMODULE = YangStmtMapping.SUBMODULE.getStatementName().getLocalName();
    private static final String YANG_VERSION = YangStmtMapping.YANG_VERSION.getStatementName().getLocalName();
    private static final String CONTACT = YangStmtMapping.CONTACT.getStatementName().getLocalName();
    private static final String ORGANIZATION = YangStmtMapping.ORGANIZATION.getStatementName().getLocalName();
    private static final String DESCRIPTION = YangStmtMapping.DESCRIPTION.getStatementName().getLocalName();
    private static final String REFERENCE = YangStmtMapping.REFERENCE.getStatementName().getLocalName();

    private YinSourceInfoExtractor() {}

    public static @NonNull SourceInfo forNode(final Node rootNode, final SourceIdentifier sourceIdentifier) {
        final String rootElement = rootNode.getFirstChild().getLocalName();
        if (rootElement.equals(MODULE)) {
            return extractModule(rootNode.getFirstChild(), sourceIdentifier);
        }

        if (rootElement.equals(SUBMODULE)) {
            return extractSubmodule(rootNode.getFirstChild(), sourceIdentifier);
        }
        throw new IllegalArgumentException("Root of YING must be either module or submodule");
    }

    private static @NonNull Referenced<XMLNamespace> getNamespace(final Node root) {
        final ImmutableMap<String, Node> children = mapUniqueChildElementsOf(root);
        final Node node = children.get(NAMESPACE);
        if (node != null) {
            return readNamespace(node);
        }
        throw new IllegalArgumentException("No namespace statement in " + refOf(root));
    }

    private static @NonNull Referenced<Unqualified> getPrefix(final Node root) {
        final ImmutableMap<String, Node> children = mapUniqueChildElementsOf(root);
        final Node node = children.get(PREFIX);
        if (node != null) {
            return readPrefix(node);
        }
        throw new IllegalArgumentException("No prefix statement in " + refOf(root));
    }

    private static void fillYangVersion(final SourceInfo.Builder<?, ?> builder,
        final ImmutableMap<String, Node> children) {
        final Node child = children.get(YANG_VERSION);
        if (child != null) {
            builder.setYangVersion(readYangVersion(child));
        }
    }

    private static StatementSourceReference refOf(final Node node) {
        return extractRef((Element) node);
    }

    private static Referenced<XMLNamespace> readNamespace(final Node node) {
        return new Referenced<>(XMLNamespace.of(node.getAttributes().item(0).getNodeValue()), refOf(node));
    }

    private static Referenced<Unqualified> readPrefix(final Node node) {
        return new Referenced<>(Unqualified.of(node.getAttributes().item(0).getNodeValue()), refOf(node));
    }

    private static Referenced<YangVersion> readYangVersion(final Node node) {
        return new Referenced<>(YangVersion.forString(node.getAttributes().item(0).getNodeValue()), refOf(node));
    }

    private static Referenced<Revision> readRevisionDate(final Node node) {
        return new Referenced<>(Revision.of(node.getAttributes().item(0).getNodeValue()), refOf(node));
    }

    private static Referenced<String> readStringStatement(final Node node) {
        return new Referenced<>(node.getAttributes().item(0).getNodeValue(), refOf(node));
    }

    private static ImmutableMap<String, Node> mapUniqueChildElementsOf(final Node root) {
        final NodeList childNodes = root.getChildNodes();
        final Map<String, Node> childMap = new HashMap<>();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node child = childNodes.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                childMap.putIfAbsent(child.getLocalName(), child);
            }
        }
        return ImmutableMap.copyOf(childMap);
    }

    private static void fillCommon(final SourceInfo.Builder<?, ?> builder, final Node root,
        final SourceIdentifier sourceId) {
        builder.setName(sourceId.name(), refOf(root.getFirstChild()));
        final ImmutableMap<String, Node> uniqueChildren = mapUniqueChildElementsOf(root);
        fillYangVersion(builder, uniqueChildren);
        fillContact(builder, uniqueChildren);
        fillOrganization(builder, uniqueChildren);
        fillDescription(builder, uniqueChildren);
        fillReference(builder, uniqueChildren);
        fillRevisions(builder, root);
        fillIncludes(builder, root);
        fillImports(builder, root);
    }

    private static void fillRevisions(final SourceInfo.Builder<?, ?> builder, final Node root) {
        final NodeList childNodes = root.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node child = childNodes.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                if (child.getLocalName().equals(REVISION)) {
                    builder.addRevision(getRevision(child));
                }
            }
        }
    }

    private static void fillIncludes(final SourceInfo.Builder<?, ?> builder, final Node root) {
        final NodeList childNodes = root.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node child = childNodes.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                if (child.getLocalName().equals(INCLUDE)) {
                    builder.addInclude(getInclude(child));
                }
            }
        }
    }

    private static DetailedRevision getRevision(final Node revisionNode) {
        final Referenced<Revision> revision = new Referenced<>(Revision.of(revisionNode.getAttributes().item(0)
            .getNodeValue()),refOf(revisionNode));
        Referenced<String> description = null;
        Referenced<String> reference = null;

        final ImmutableMap<String, Node> revisionChildren = mapUniqueChildElementsOf(revisionNode);

        final Node descriptionNode = revisionChildren.get(DESCRIPTION);
        if (descriptionNode != null) {
            description = readStringStatement(descriptionNode);
        }

        final Node referendeNode = revisionChildren.get(REFERENCE);
        if (referendeNode != null) {
            reference = readStringStatement(referendeNode);
        }
        return new DetailedRevision(revision, description, reference);
    }

    private static SourceDependency.Include getInclude(final Node includeNode) {
        final Referenced<Unqualified> name = new Referenced<>(Unqualified.of(includeNode.getAttributes().item(0)
            .getNodeValue()), refOf(includeNode));
        Referenced<Revision> revision = null;
        Referenced<String> description = null;
        Referenced<String> reference = null;


        final ImmutableMap<String, Node> includeChildren = mapUniqueChildElementsOf(includeNode);

        final Node revisionNode = includeChildren.get(REVISION_DATE);
        if (revisionNode != null) {
            revision = readRevisionDate(revisionNode);
        }

        final Node descriptionNode = includeChildren.get(DESCRIPTION);
        if (descriptionNode != null) {
            description = readStringStatement(descriptionNode);
        }

        final Node referendeNode = includeChildren.get(REFERENCE);
        if (referendeNode != null) {
            reference = readStringStatement(referendeNode);
        }

        return new SourceDependency.Include(name, revision, description, reference);
    }


    private static SourceDependency.Import getImport(final Node importNode) {
        final Referenced<Unqualified> name = new Referenced<>(Unqualified.of(importNode.getAttributes().item(0)
            .getNodeValue()), refOf(importNode));
        Referenced<Unqualified> prefix = null;
        Referenced<Revision> revision = null;
        Referenced<String> description = null;
        Referenced<String> reference = null;
        final ImmutableMap<String, Node> importChildren = mapUniqueChildElementsOf(importNode);

        final Node prefixNode = importChildren.get(PREFIX);
        if (prefixNode != null) {
            prefix = readPrefix(prefixNode);
        } else {
            throw new IllegalArgumentException("No prefix statement in " + refOf(importNode));
        }

        final Node revisionNode = importChildren.get(REVISION_DATE);
        if (revisionNode != null) {
            revision = readRevisionDate(revisionNode);
        }

        final Node descriptionNode = importChildren.get(DESCRIPTION);
        if (descriptionNode != null) {
            description = readStringStatement(descriptionNode);
        }

        final Node referendeNode = importChildren.get(REFERENCE);
        if (referendeNode != null) {
            reference = readStringStatement(referendeNode);
        }

        return new SourceDependency.Import(name, prefix, revision, description, reference);
    }

    private static void fillImports(final SourceInfo.Builder<?, ?> builder, final Node root) {
        final NodeList childNodes = root.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node child = childNodes.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                if (child.getLocalName().equals(IMPORT)) {
                    builder.addImport(getImport(child));
                }
            }
        }
    }

    private static void fillOrganization(final SourceInfo.Builder<?, ?> builder,
        final ImmutableMap<String, Node> children) {
        final Node child = children.get(ORGANIZATION);
        if (child != null) {
            builder.setOrganization(readStringStatement(child));
        }
    }

    private static void fillContact(final SourceInfo.Builder<?, ?> builder,
        final ImmutableMap<String, Node> children) {
        final Node child = children.get(CONTACT);
        if (child != null) {
            builder.setContact(readStringStatement(child));
        }
    }

    private static void fillDescription(final SourceInfo.Builder<?, ?> builder,
        final ImmutableMap<String, Node> children) {
        final Node child = children.get(DESCRIPTION);
        if (child != null) {
            builder.setDescription(readStringStatement(child));
        }
    }

    private static void fillReference(final SourceInfo.Builder<?, ?> builder,
        final ImmutableMap<String, Node> children) {
        final Node child = children.get(DESCRIPTION);
        if (child != null) {
            builder.setReference(readStringStatement(child));
        }
    }

    private static SourceDependency.BelongsTo readBelongsTo(final Node belongsToNode) {
        final Referenced<Unqualified> name = new Referenced<>(Unqualified.of(belongsToNode.getAttributes().item(0)
            .getNodeValue()),refOf(belongsToNode));
        final ImmutableMap<String, Node> belongsToChildren = mapUniqueChildElementsOf(belongsToNode);
        final Node prefixNode = belongsToChildren.get(PREFIX);
        if (prefixNode != null) {
            return new SourceDependency.BelongsTo(name, readPrefix(prefixNode));
        } else {
            throw new IllegalArgumentException("No prefix statement in " + refOf(belongsToNode));
        }
    }

    private static SourceDependency.BelongsTo getBelongsTo(final Node node) {
        final ImmutableMap<String, Node> children = mapUniqueChildElementsOf(node);
        final Node belongsToNode = children.get(BELONGS_TO);
        if (belongsToNode != null) {
            return readBelongsTo(belongsToNode);
        }
        throw new IllegalArgumentException("No belongs-to statement in " + refOf(node));
    }

    private static SourceInfo.@NonNull Module extractModule(final Node root, final SourceIdentifier sourceId) {
        final var builder = SourceInfo.Module.builder()
            .setNamespace(getNamespace(root))
            .setPrefix(getPrefix(root));
        fillCommon(builder, root, sourceId);
        return builder.build();
    }

    private static SourceInfo.@NonNull Submodule extractSubmodule(final Node root, final SourceIdentifier sourceId) {
        final var builder = SourceInfo.Submodule.builder()
            .setBelongsTo(getBelongsTo(root));
        fillCommon(builder, root, sourceId);
        return builder.build();
    }
}
