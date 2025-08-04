package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import static org.opendaylight.yangtools.yang.parser.rfc7950.repo.StatementSourceReferenceHandler.extractRef;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Referenced;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class YinSourceInfoExtr extends SourceInfoExtractor<Node> {
    @Override
    String extractRootType(final Node root) {
        return root.getFirstChild().getLocalName();
    }

    @Override
    Referenced<Unqualified> extractPrefix(Node root, SourceIdentifier rootId) {
        final ImmutableMap<String, Node> children = mapUniqueChildElementsOf(root);
        final Node node = children.get(PREFIX);
        if (node != null) {
            return readPrefix(node);
        }
        throw new IllegalArgumentException("No prefix statement in " + refOf(root));
    }

    @Override
    Referenced<XMLNamespace> extractNamespace(Node root, SourceIdentifier rootId) {
        final ImmutableMap<String, Node> children = mapUniqueChildElementsOf(root);
        final Node node = children.get(NAMESPACE);
        if (node != null) {
            return readNamespace(node);
        }
        throw new IllegalArgumentException("No namespace statement in " + refOf(root));
    }

    @Override
    SourceDependency.BelongsTo extractBelongsTo(Node root, SourceIdentifier rootId) {
        final ImmutableMap<String, Node> children = mapUniqueChildElementsOf(node);
        final Node belongsToNode = children.get(BELONGS_TO);
        if (belongsToNode != null) {
            return readBelongsTo(belongsToNode);
        }
        throw new IllegalArgumentException("No belongs-to statement in " + refOf(node));
    }

    @Override
    Referenced<Unqualified> extractName(Node root, SourceIdentifier rootId) {
        return new Referenced<>(rootId.name(), refOf(root.getFirstChild()));
    }

    @Override
    Referenced<YangVersion> extractYangVersion(Node root, SourceIdentifier rootId) {

    }

    @Override
    Referenced<String> extractContact(Node root, SourceIdentifier rootId) {
        return null;
    }

    @Override
    Referenced<String> extractOrganization(Node root, SourceIdentifier rootId) {
        return null;
    }

    @Override
    Referenced<String> extractDescription(Node root, SourceIdentifier rootId) {
        return null;
    }

    @Override
    Referenced<String> extractReference(Node root, SourceIdentifier rootId) {
        return null;
    }


    @Override
    void fillRevisions(SourceInfo.Builder<?, ?> builder, Node root, SourceIdentifier rootId) {

    }

    @Override
    void fillIncludes(SourceInfo.Builder<?, ?> builder, Node root, SourceIdentifier rootId) {

    }

    @Override
    void fillImports(SourceInfo.Builder<?, ?> builder, Node root, SourceIdentifier rootId) {
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

    private static StatementSourceReference refOf(final Node node) {
        return extractRef((Element) node);
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

    private static Referenced<XMLNamespace> readNamespace(final Node node) {
        return new Referenced<>(XMLNamespace.of(node.getAttributes().item(0).getNodeValue()), refOf(node));
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
}
