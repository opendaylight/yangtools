/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.common.YangConstants.RFC6020_YIN_NAMESPACE_STRING;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.ExtractorException;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.ExtractorInvalidArgumentException;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.ExtractorInvalidRootException;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.ExtractorMissingArgumentException;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.ExtractorMissingStatementException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract sealed class YinSourceInfoExtractor implements SourceInfo.Extractor {
    private static final class ForModule extends YinSourceInfoExtractor {
        private static final @NonNull String NAMESPACE = YangStmtMapping.NAMESPACE.getStatementName().getLocalName();
        private static final @NonNull String NAMESPACE_ARG =
            YangStmtMapping.NAMESPACE.getArgumentDefinition().orElseThrow().argumentName().getLocalName();

        ForModule(final Element root, final SourceIdentifier rootIdentifier) {
            super(root, rootIdentifier);
        }

        @Override
        public SourceInfo.Module extractSourceInfo() throws ExtractorException {
            final var builder = SourceInfo.Module.builder();
            fillCommon(builder);
            return builder
                .setPrefix(extractPrefix(root))
                .setNamespace(extractNamespace())
                .build();
        }

        private @NonNull XMLNamespace extractNamespace() throws ExtractorException {
            final var arg = getFirstElementArgument(root, NAMESPACE, NAMESPACE_ARG);
            try {
                return XMLNamespace.of(arg);
            } catch (IllegalArgumentException e) {
                throw new ExtractorInvalidArgumentException(null, NAMESPACE, e);
            }
        }
    }

    private static final class ForSubmodule extends YinSourceInfoExtractor {
        ForSubmodule(final Element root, final SourceIdentifier rootIdentifier) {
            super(root, rootIdentifier);
        }

        @Override
        public SourceInfo.Submodule extractSourceInfo() throws ExtractorException {
            final var builder = SourceInfo.Submodule.builder();
            fillCommon(builder);
            return builder
                .setBelongsTo(extractBelongsTo())
                .build();
        }
   }

    private static final @NonNull String MODULE = "module";
    private static final @NonNull String SUBMODULE = "submodule";

    static {
        verify(MODULE.equals(YangStmtMapping.MODULE.getStatementName().getLocalName()));
        verify(SUBMODULE.equals(YangStmtMapping.SUBMODULE.getStatementName().getLocalName()));
    }

    private static final @NonNull String BELONGS_TO = YangStmtMapping.BELONGS_TO.getStatementName().getLocalName();
    private static final @NonNull String IMPORT = YangStmtMapping.IMPORT.getStatementName().getLocalName();
    private static final @NonNull String INCLUDE = YangStmtMapping.INCLUDE.getStatementName().getLocalName();
    private static final @NonNull String PREFIX = YangStmtMapping.PREFIX.getStatementName().getLocalName();
    private static final @NonNull String PREFIX_ARG =
        YangStmtMapping.PREFIX.getArgumentDefinition().orElseThrow().argumentName().getLocalName();
    private static final @NonNull String REVISION = YangStmtMapping.REVISION.getStatementName().getLocalName();
    private static final @NonNull String REVISION_DATE =
        YangStmtMapping.REVISION_DATE.getStatementName().getLocalName();
    private static final @NonNull String YANG_VERSION = YangStmtMapping.YANG_VERSION.getStatementName().getLocalName();

    private final ImmutableMap<String, Node> uniqueRootElements;
    private final @NonNull SourceIdentifier rootIdentifier;

    final @NonNull Element root;

    YinSourceInfoExtractor(final Element root, final SourceIdentifier rootIdentifier) {
        this.root = requireNonNull(root);
        this.rootIdentifier = requireNonNull(rootIdentifier);
        uniqueRootElements = mapUniqueChildElementsOf(root);
    }

    private static ImmutableMap<String, Node> mapUniqueChildElementsOf(final Node root) {
        final var childNodes = root.getChildNodes();
        final var childMap = new HashMap<String, Node>();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final var child = childNodes.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                childMap.putIfAbsent(child.getLocalName(), child);
            }
        }
        return ImmutableMap.copyOf(childMap);
    }

    public static SourceInfo.Extractor of(final Node root, final SourceIdentifier sourceId) throws ExtractorException {
        if (!(root instanceof Element element)) {
            throw new ExtractorInvalidRootException(null, "Root node is not an element");
        }
        if (!RFC6020_YIN_NAMESPACE_STRING.equals(element.getNamespaceURI())) {
            throw new ExtractorInvalidRootException(null, "Root element does not have YIN namespace");
        }
        return switch (element.getLocalName()) {
            case MODULE -> new ForModule(element, sourceId);
            case SUBMODULE -> new ForSubmodule(element, sourceId);
            default -> throw new ExtractorInvalidRootException(null, "Root element needs to be a module or submodule");
        };
    }

    final void fillCommon(final SourceInfo.Builder<?, ?> builder) {
        builder.setName(extractName());
        final var yangVersion = extractYangVersion();
        if (yangVersion != null) {
            builder.setYangVersion(yangVersion);
        }
        extractRevisions(builder);
        extractIncludes(builder);
        extractImports(builder);
    }

    final Unqualified extractModulePrefix() {
        final Node node = uniqueRootElements.get(PREFIX);
        if (node != null) {
            return readNodeQName(node);
        }
        throw new IllegalArgumentException("No prefix statement in " + refOf(root));
    }

    final SourceDependency.BelongsTo extractBelongsTo() {
        final Node belongsToNode = uniqueRootElements.get(BELONGS_TO);
        if (belongsToNode != null) {
            return readBelongsTo(belongsToNode);
        }
        throw new IllegalArgumentException("No belongs-to statement in " + refOf(root));
    }

    // FIXME: what is this?
    private Unqualified extractName() {
        return rootIdentifier.name();
    }

    private YangVersion extractYangVersion() {
        final Node child = uniqueRootElements.get(YANG_VERSION);
        return child != null ? readYangVersion(child) : null;
    }

    private void extractRevisions(final SourceInfo.Builder<?, ?> builder) {
        for (final Node revisionNode : getChildrenOfType(root, REVISION)) {
            builder.addRevision(extractRevision(revisionNode));
        }
    }

    private void extractIncludes(final SourceInfo.Builder<?, ?> builder) {
        for (final Node includeNode : getChildrenOfType(root, INCLUDE)) {
            builder.addInclude(extractInclude(includeNode));
        }
    }

    private void extractImports(final SourceInfo.Builder<?, ?> builder) {
        for (final Node importNode : getChildrenOfType(root, IMPORT)) {
            builder.addImport(extractImport(importNode));
        }
    }

    private static List<Node> getChildrenOfType(final Node parent, final String type) {
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

    private static StatementSourceReference refOf(final Node node) {
        return StatementSourceReferenceHandler.extractRef((Element) node);
    }

    private static Revision extractRevision(final Node revisionNode) {
        return Revision.of(revisionNode.getAttributes().item(0).getNodeValue());
    }

    private static SourceDependency.Include extractInclude(final Node includeNode) {
        final Unqualified name = readNodeQName(includeNode);
        Revision revision = null;

        final Node revisionNode = getChildNode(includeNode, REVISION_DATE);
        if (revisionNode != null) {
            revision = readRevisionDate(revisionNode);
        }
        return new SourceDependency.Include(name, revision);
    }

    private static Unqualified readNodeQName(final Node node) {
        return Unqualified.of(node.getAttributes().item(0).getNodeValue());
    }

    private static SourceDependency.Import extractImport(final Node importNode) {
        final Unqualified name = readNodeQName(importNode);
        Unqualified prefix = null;
        Revision revision = null;

        final Node prefixNode = getChildNode(importNode, PREFIX);
        if (prefixNode == null) {
            throw new IllegalArgumentException("No prefix statement in " + refOf(importNode));
        }
        prefix = readNodeQName(prefixNode);

        final Node revisionNode = getChildNode(importNode, REVISION_DATE);
        if (revisionNode != null) {
            revision = readRevisionDate(revisionNode);
        }

        return new SourceDependency.Import(name, prefix, revision);
    }

    private static Node getChildNode(final Node root, final String nodeName) {
        final var childNodes = root.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final var child = childNodes.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && child.getLocalName().equals(nodeName)) {
                return child;
            }
        }
        return null;
    }

    private static YangVersion readYangVersion(final Node node) {
        return YangVersion.forString(node.getAttributes().item(0).getNodeValue());
    }

    private static Revision readRevisionDate(final Node node) {
        return Revision.of(node.getAttributes().item(0).getNodeValue());
    }

    private static SourceDependency.BelongsTo readBelongsTo(final Node belongsToNode) {
        final Unqualified name = readNodeQName(belongsToNode);
        final Node prefixNode = getChildNode(belongsToNode, PREFIX);
        if (prefixNode != null) {
            return new SourceDependency.BelongsTo(name, readNodeQName(prefixNode));
        }
        throw new IllegalArgumentException("No prefix statement in " + refOf(belongsToNode));
    }

    @NonNullByDefault
    final Unqualified extractPrefix(final Element parent) throws ExtractorException {
        final var arg = getFirstElementArgument(parent, PREFIX, PREFIX_ARG);
        try {
            return Unqualified.of(arg);
        } catch (IllegalArgumentException e) {
            throw new ExtractorInvalidArgumentException(null, PREFIX, e);
        }
    }

    @NonNullByDefault
    final String getFirstElementArgument(final Element parent, final String keyword, final String argument)
            throws ExtractorException {
        final var childNodes = parent.getChildNodes();
        for (int i = 0, length = childNodes.getLength(); i < length; ++i) {
            if (childNodes.item(i) instanceof Element element && isYinElement(element, keyword)) {
                final var arg = element.getAttributeNodeNS(null, argument);
                if (arg != null && arg.getSpecified()) {
                    return arg.getValue();
                }

                throw new ExtractorMissingArgumentException(null, keyword);
            }
        }
        throw new ExtractorMissingStatementException(null, keyword);
    }

    private static boolean isYinElement(final Element element, final String keyword) {
        return RFC6020_YIN_NAMESPACE_STRING.equals(element.getNamespaceURI()) && keyword.equals(element.getLocalName());
    }
}
