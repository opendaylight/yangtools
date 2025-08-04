/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.parser.rfc7950.repo.StatementSourceReferenceHandler.extractRef;

import com.google.common.annotations.Beta;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;
import javax.xml.transform.TransformerException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.AbstractSimpleIdentifiable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.BelongsTo;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Import;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Include;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency.Referenced;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.DetailedRevision;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.YinDomSource;
import org.opendaylight.yangtools.yang.model.spi.source.YinXmlSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixResolver;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A {@link StatementStreamSource} based on a {@link YinXmlSource}. Internal implementation works on top
 * of {@link YinDomSource} and its DOM document.
 *
 * @author Robert Varga
 */
@Beta
public final class YinStatementStreamSource extends AbstractSimpleIdentifiable<SourceIdentifier>
        implements StatementStreamSource {
    private static final Logger LOG = LoggerFactory.getLogger(YinStatementStreamSource.class);
    private static final LoadingCache<String, XMLNamespace> NS_CACHE = CacheBuilder.newBuilder().weakValues().build(
        new CacheLoader<String, XMLNamespace>() {
            @Override
            public XMLNamespace load(final String key) {
                return XMLNamespace.of(key).intern();
            }
        });
    private final Node root;

    private YinStatementStreamSource(final SourceIdentifier sourceId, final Node root) {
        super(sourceId);
        this.root = requireNonNull(root);
    }

    public static StatementStreamSource create(final YinXmlSource source) throws TransformerException {
        return create(YinDomSource.transform(source));
    }

    public static StatementStreamSource create(final YinDomSource source) {
        return new YinStatementStreamSource(source.sourceId(), source.getSource().getNode());
    }

    private static StatementDefinition getValidDefinition(final Node node, final StatementWriter writer,
            final QNameToStatementDefinition stmtDef, final StatementSourceReference ref) {
        final XMLNamespace uri = NS_CACHE.getUnchecked(node.getNamespaceURI());
        final StatementDefinition def = stmtDef.getByNamespaceAndLocalName(uri, node.getLocalName());

        if (def == null) {
            SourceException.throwIf(writer.getPhase().equals(ModelProcessingPhase.FULL_DECLARATION), ref,
                "%s is not a YIN statement or use of extension.", node.getLocalName());
        }
        return def;
    }

    private static boolean processAttribute(final int childId, final Attr attr, final StatementWriter writer,
            final QNameToStatementDefinition stmtDef, final StatementSourceReference ref) {
        final var optResumed = writer.resumeStatement(childId);
        if (optResumed.isPresent()) {
            final var resumed = optResumed.orElseThrow();
            checkState(resumed.isFullyDefined(), "Statement %s is not fully defined", resumed);
            return true;
        }

        final StatementDefinition def = getValidDefinition(attr, writer, stmtDef, ref);
        if (def == null) {
            return false;
        }

        final String value = attr.getValue();
        writer.startStatement(childId, def.getStatementName(), value.isEmpty() ? null : value, ref);
        writer.storeStatement(0, true);
        writer.endStatement(ref);
        return true;
    }

    private static String getArgValue(final Element element, final QName argName, final boolean yinElement) {
        if (yinElement) {
            final NodeList children = element.getElementsByTagNameNS(argName.getNamespace().toString(),
                argName.getLocalName());
            if (children.getLength() == 0) {
                return null;
            }
            return children.item(0).getTextContent();
        }

        final Attr attr = element.getAttributeNode(argName.getLocalName());
        if (attr == null) {
            return null;
        }

        return attr.getValue();
    }

    private static boolean processElement(final int childId, final Element element, final StatementWriter writer,
            final QNameToStatementDefinition stmtDef) {

        final var optResumed = writer.resumeStatement(childId);
        final StatementSourceReference ref;
        final QName argName;
        final boolean allAttrs;
        final boolean allElements;
        if (optResumed.isPresent()) {
            final var resumed = optResumed.orElseThrow();
            if (resumed.isFullyDefined()) {
                return true;
            }

            final StatementDefinition def = resumed.getDefinition();
            ref = resumed.getSourceReference();
            final var optArgDef = def.getArgumentDefinition();
            if (optArgDef.isPresent()) {
                final var argDef = optArgDef.orElseThrow();
                argName = argDef.argumentName();
                allAttrs = argDef.isYinElement();
                allElements = !allAttrs;
            } else {
                argName = null;
                allAttrs = false;
                allElements = false;
            }
        } else {
            ref = extractRef(element);
            final StatementDefinition def = getValidDefinition(element, writer, stmtDef, ref);
            if (def == null) {
                LOG.debug("Skipping element {}", element);
                return false;
            }

            final String argValue;
            final var optArgDef = def.getArgumentDefinition();
            if (optArgDef.isPresent()) {
                final var argDef = optArgDef.orElseThrow();
                argName = argDef.argumentName();
                allAttrs = argDef.isYinElement();
                allElements = !allAttrs;

                argValue = getArgValue(element, argName, allAttrs);
                SourceException.throwIfNull(argValue, ref, "Statement {} is missing mandatory argument %s",
                    def.getStatementName(), argName);
            } else {
                argName = null;
                argValue = null;
                allAttrs = false;
                allElements = false;
            }

            writer.startStatement(childId, def.getStatementName(), argValue, ref);
        }

        // Child counter
        int childCounter = 0;
        boolean fullyDefined = true;

        // First process any statements defined as attributes. We need to skip argument, if present
        final NamedNodeMap attributes = element.getAttributes();
        if (attributes != null) {
            for (int i = 0, len = attributes.getLength(); i < len; ++i) {
                final Attr attr = (Attr) attributes.item(i);
                if ((allAttrs || !isArgument(argName, attr))
                        && !processAttribute(childCounter++, attr, writer, stmtDef, ref)) {
                    fullyDefined = false;
                }
            }
        }

        // Now process child elements, if present
        final NodeList children = element.getChildNodes();
        for (int i = 0, len = children.getLength(); i < len; ++i) {
            final Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && (allElements || !isArgument(argName, child))
                    && !processElement(childCounter++, (Element) child, writer, stmtDef)) {
                fullyDefined = false;
            }
        }

        writer.storeStatement(childCounter, fullyDefined);
        writer.endStatement(ref);
        return fullyDefined;
    }

    private static boolean isArgument(final QName argName, final Node node) {
        return argName != null && argName.getLocalName().equals(node.getLocalName()) && node.getPrefix() == null;
    }

    private void walkTree(final StatementWriter writer, final QNameToStatementDefinition stmtDef) {
        final NodeList children = root.getChildNodes();

        int childCounter = 0;
        for (int i = 0, len = children.getLength(); i < len; ++i) {
            final Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                processElement(childCounter++, (Element) child, writer, stmtDef);
            }
        }
    }

    @Override
    public @NonNull SourceInfo getSourceInfo() {
        final NodeList childNodes = root.getFirstChild().getChildNodes();
        final Referenced<SourceIdentifier> sourceId = new Referenced<>(getIdentifier(),
            extractRef(((Element) root.getFirstChild())));
        Referenced<YangVersion> yangVersion = new Referenced<>(YangVersion.VERSION_1, null);
        Referenced<XMLNamespace> namespace = null;
        Referenced<Unqualified> prefix = null;
        BelongsTo belongsTo = null;
        Referenced<String> contact = null;
        Referenced<String> organization = null;
        Referenced<String> description = null;
        Referenced<String> reference = null;

        Set<DetailedRevision> revisions = new HashSet<>();
        Set<Import> imports = new HashSet<>();
        Set<Include> includes = new HashSet<>();

        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node child = childNodes.item(i);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                switch (child.getLocalName()) {
                    case "yang-version":
                        yangVersion = getYangVersion(child);
                        break;
                    case "namespace":
                        namespace = getNamespace(child);
                        break;
                    case "prefix":
                        prefix = getPrefix(child);
                        break;
                    case "revision":
                        revisions.add(getRevision(child));
                        break;
                    case "import":
                        imports.add(getImport(child));
                        break;
                    case "include":
                        includes.add(getInclude(child));
                        break;
                    case "belongs-to":
                        belongsTo = getBelongsTo(child);
                        break;
                    case "contact":
                        contact = getSimpleAttribute(child);
                        break;
                    case "organization":
                        organization = getSimpleAttribute(child);
                        break;
                    case "description":
                        description = getSimpleAttribute(child);
                        break;
                    case "reference":
                        reference = getSimpleAttribute(child);
                        break;
                }
            }
        }


        if (root.getFirstChild().getLocalName().equals("module")) {
            return new SourceInfo.Module(sourceId, yangVersion, namespace, prefix,
                ImmutableSet.copyOf(revisions), ImmutableSet.copyOf(imports), ImmutableSet.copyOf(includes),
                contact, organization, description, reference);

        } else {
            return new SourceInfo.Submodule(sourceId, yangVersion, belongsTo,
                ImmutableSet.copyOf(revisions), ImmutableSet.copyOf(imports), ImmutableSet.copyOf(includes),
                contact, organization, description, reference);
        }
    }

    private Referenced<Unqualified> getPrefix(final Node node) {
        return new Referenced<>(Unqualified.of(node.getAttributes().item(0).getNodeValue()),
            extractRef((Element)node));
    }

    private Referenced<String> getSimpleAttribute(final Node node) {
        return new Referenced<>(node.getAttributes().item(0).getNodeValue(),
            extractRef((Element)node));
    }

    private DetailedRevision getRevision(final Node node) {
        final Referenced<Revision> revision = new Referenced<>(Revision.of(node.getAttributes().item(0)
            .getNodeValue()), extractRef((Element) node));
        Referenced<String> description = null;
        Referenced<String> reference = null;
        final NodeList childNodes = node.getChildNodes();
        for (int childOffset = 0; childOffset < childNodes.getLength(); childOffset++) {
            final Node child = childNodes.item(childOffset);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                switch (child.getLocalName()) {
                    case "description":
                        description = getSimpleAttribute(child);
                        break;
                    case "reference":
                        reference = getSimpleAttribute(child);
                        break;
                }
            }
        }
        return new DetailedRevision(revision, description, reference);
    }

    private Referenced<YangVersion> getYangVersion(final Node node) {
        return new Referenced<>(YangVersion.forString(node.getAttributes().item(0).getNodeValue()),
            extractRef((Element)node));
    }

    private Referenced<XMLNamespace> getNamespace(final Node node) {
        return new Referenced<>(XMLNamespace.of(node.getAttributes().item(0).getNodeValue()),
            extractRef((Element)node));
    }

    private Import getImport(final Node node) {
        final Referenced<Unqualified> name = new Referenced<>(Unqualified.of(node.getAttributes().item(0)
            .getNodeValue()), extractRef((Element) node));
        Referenced<Unqualified> prefix = null;
        Referenced<Revision> revision = null;
        Referenced<String> description = null;
        Referenced<String> reference = null;
        final NodeList importChildNodes = node.getChildNodes();
        for (int impOffset = 0; impOffset < importChildNodes.getLength(); impOffset++) {
            final Node importChild = importChildNodes.item(impOffset);
            if (importChild.getNodeType() != Node.ELEMENT_NODE) {
                switch (importChild.getLocalName()) {
                    case "prefix":
                        prefix = getPrefix(importChild);
                        break;
                    case "revision-date":
                        revision = getRevisionDate(importChild);
                        break;
                    case "description":
                        description = getSimpleAttribute(importChild);
                        break;
                    case "reference":
                        reference = getSimpleAttribute(importChild);
                        break;
                }
            }
        }
        return new Import(name, prefix, revision, description, reference );
    }

    private Referenced<Revision> getRevisionDate(Node node) {
        return new Referenced<>(Revision.of(node.getAttributes().item(0).getNodeValue()),
            extractRef((Element)node));
    }

    private Include getInclude(final Node node) {
        final Referenced<Unqualified> name = new Referenced<>(Unqualified.of(node.getAttributes().item(0)
            .getNodeValue()), extractRef((Element) node));
        Referenced<Revision> revision = null;
        Referenced<String> description = null;
        Referenced<String> reference = null;
        final NodeList childNodes = node.getChildNodes();
        for (int childOffset = 0; childOffset < childNodes.getLength(); childOffset++) {
            final Node child = childNodes.item(childOffset);
            if (child.getNodeType() != Node.ELEMENT_NODE) {
                switch (child.getLocalName()) {
                    case "revision-date":
                        revision = getRevisionDate(child);
                        break;
                    case "description":
                        description = getSimpleAttribute(child);
                        break;
                    case "reference":
                        reference = getSimpleAttribute(child);
                        break;
                }
            }
        }
        return new Include(name, revision, description, reference);
    }

    private BelongsTo getBelongsTo(final Node node) {
        final Referenced<Unqualified> name = new Referenced<>(Unqualified.of(node.getAttributes().item(0)
            .getNodeValue()), extractRef((Element) node));
        Referenced<Unqualified> prefix = null;
        final NodeList belongsToChildNodes = node.getChildNodes();
        for (int childOffset = 0; childOffset < belongsToChildNodes.getLength(); childOffset++) {
            final Node belongsToChild = belongsToChildNodes.item(childOffset);
            if (belongsToChild.getNodeType() != Node.ELEMENT_NODE) {
                switch (belongsToChild.getLocalName()) {
                    case "prefix":
                        prefix = getPrefix(belongsToChild);
                        break;
                }
            }
        }
        return new BelongsTo(name, prefix);
    }


    @Override
    public void writePreLinkage(final StatementWriter writer, final QNameToStatementDefinition stmtDef) {
        walkTree(writer, stmtDef);
    }

    @Override
    public void writeLinkage(final StatementWriter writer, final QNameToStatementDefinition stmtDef,
            final PrefixResolver preLinkagePrefixes, final YangVersion yangVersion) {
        walkTree(writer, stmtDef);
    }

    @Override
    public void writeLinkageAndStatementDefinitions(final StatementWriter writer,
            final QNameToStatementDefinition stmtDef, final PrefixResolver prefixes, final YangVersion yangVersion) {
        walkTree(writer, stmtDef);
    }

    @Override
    public void writeFull(final StatementWriter writer, final QNameToStatementDefinition stmtDef,
            final PrefixResolver prefixes, final YangVersion yangVersion) {
        walkTree(writer, stmtDef);
    }
}
