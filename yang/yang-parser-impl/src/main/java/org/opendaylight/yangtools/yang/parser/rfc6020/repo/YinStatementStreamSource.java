/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc6020.repo;

import static org.opendaylight.yangtools.yang.parser.rfc6020.repo.StatementSourceReferenceHandler.extractRef;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.net.URI;
import java.net.URISyntaxException;
import javax.annotation.Nonnull;
import javax.xml.transform.TransformerException;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YinDomSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.api.YinXmlSchemaSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.spi.source.StatementSourceReference;
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
 * A {@link StatementStreamSource} based on a {@link YinXmlSchemaSource}. Internal implementation works on top
 * of {@link YinDomSchemaSource} and its DOM document.
 *
 * @author Robert Varga
 */
@Beta
public final class YinStatementStreamSource implements Identifiable<SourceIdentifier>, StatementStreamSource {
    private static final Logger LOG = LoggerFactory.getLogger(YinStatementStreamSource.class);
    private static final LoadingCache<String, URI> URI_CACHE = CacheBuilder.newBuilder().weakValues().build(
        new CacheLoader<String, URI>() {
            @Override
            public URI load(@Nonnull final String key) throws URISyntaxException {
                return new URI(key);
            }
    });
    private final SourceIdentifier identifier;
    private final Node root;

    private YinStatementStreamSource(final SourceIdentifier identifier, final Node root) {
        this.identifier = Preconditions.checkNotNull(identifier);
        this.root = Preconditions.checkNotNull(root);
    }

    public static StatementStreamSource create(final YinXmlSchemaSource source) throws TransformerException {
        return create(YinDomSchemaSource.transform(source));
    }

    public static StatementStreamSource create(final YinDomSchemaSource source) {
        return new YinStatementStreamSource(source.getIdentifier(), source.getSource().getNode());
    }

    @Override
    public SourceIdentifier getIdentifier() {
        return identifier;
    }

    private static StatementDefinition getValidDefinition(final Node node, final StatementWriter writer,
            final QNameToStatementDefinition stmtDef, final StatementSourceReference ref) {
        final URI uri = URI_CACHE.getUnchecked(node.getNamespaceURI());
        final StatementDefinition def = stmtDef.getByNamespaceAndLocalName(uri, node.getLocalName());

        if (def == null) {
            SourceException.throwIf(writer.getPhase().equals(ModelProcessingPhase.FULL_DECLARATION), ref,
                "%s is not a YIN statement or use of extension.", node.getLocalName());
        }
        return def;
    }

    private static void processAttribute(final int childId, final Attr attr, final StatementWriter writer,
            final QNameToStatementDefinition stmtDef, final StatementSourceReference ref) {
        final StatementDefinition def = getValidDefinition(attr, writer, stmtDef, ref);
        if (def == null) {
            return;
        }

        final String value = attr.getValue();
        writer.startStatement(childId, def.getStatementName(), value.isEmpty() ? null : value, ref);
        writer.endStatement(ref);
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

    private static void processElement(final int childId, final Element element, final StatementWriter writer,
            final QNameToStatementDefinition stmtDef) {
        final StatementSourceReference ref = extractRef(element);
        final StatementDefinition def = getValidDefinition(element, writer, stmtDef, ref);
        if (def == null) {
            LOG.debug("Skipping element {}", element);
            return;
        }

        final QName argName = def.getArgumentName();
        final String argValue;
        final boolean allAttrs;
        final boolean allElements;
        if (argName != null) {
            allAttrs = def.isArgumentYinElement();
            allElements = !allAttrs;

            argValue = getArgValue(element, argName, allAttrs);
            SourceException.throwIfNull(argValue, ref, "Statement {} is missing mandatory argument %s",
                def.getStatementName(), argName);
        } else {
            argValue = null;
            allAttrs = false;
            allElements = false;
        }

        writer.startStatement(childId, def.getStatementName(), argValue, ref);

        // Child counter
        int childCounter = 0;

        // First process any statements defined as attributes. We need to skip argument, if present
        final NamedNodeMap attributes = element.getAttributes();
        if (attributes != null) {
            for (int i = 0, len = attributes.getLength(); i < len; ++i) {
                final Attr attr = (Attr) attributes.item(i);
                if (allAttrs || !isArgument(argName, attr)) {
                    processAttribute(childCounter++, attr, writer, stmtDef, ref);
                }
            }
        }

        // Now process child elements, if present
        final NodeList children = element.getChildNodes();
        for (int i = 0, len = children.getLength(); i < len; ++i) {
            final Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if (allElements || !isArgument(argName, child)) {
                    processElement(childCounter++, (Element) child, writer, stmtDef);
                }
            }
        }

        writer.endStatement(ref);
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
    public void writePreLinkage(final StatementWriter writer, final QNameToStatementDefinition stmtDef) {
        walkTree(writer, stmtDef);
    }

    @Override
    public void writeLinkage(final StatementWriter writer, final QNameToStatementDefinition stmtDef,
            final PrefixToModule preLinkagePrefixes) {
        walkTree(writer, stmtDef);
    }

    @Override
    public void writeLinkageAndStatementDefinitions(final StatementWriter writer,
            final QNameToStatementDefinition stmtDef, final PrefixToModule prefixes) {
        walkTree(writer, stmtDef);
    }

    @Override
    public void writeFull(final StatementWriter writer, final QNameToStatementDefinition stmtDef,
            final PrefixToModule prefixes) {
        walkTree(writer, stmtDef);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("identifier", getIdentifier()).toString();
    }
}
