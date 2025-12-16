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

import com.google.common.annotations.Beta;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import javax.xml.transform.TransformerException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.AbstractSimpleIdentifiable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.source.YinDomSource;
import org.opendaylight.yangtools.yang.model.spi.source.YinDomSource.SourceRefProvider;
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
import org.w3c.dom.Node;

/**
 * A {@link StatementStreamSource} based on a {@link YinXmlSource}. Internal implementation works on top
 * of {@link YinDomSource} and its DOM document.
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
    private final @NonNull Node root;
    private final @NonNull SourceRefProvider refProvider;

    private YinStatementStreamSource(final SourceIdentifier sourceId, final Node root,
            final SourceRefProvider refProvider) {
        super(sourceId);
        this.root = requireNonNull(root);
        this.refProvider = requireNonNull(refProvider);
    }

    public static StatementStreamSource create(final YinXmlSource source) throws TransformerException {
        return create(YinDomSource.transform(source));
    }

    public static StatementStreamSource create(final YinDomSource source) {
        return new YinStatementStreamSource(source.sourceId(), source.getSource().getNode(), source.refProvider());
    }

    private static StatementDefinition getValidDefinition(final Node node, final StatementWriter writer,
            final QNameToStatementDefinition stmtDef, final StatementSourceReference ref) {
        final var uri = NS_CACHE.getUnchecked(node.getNamespaceURI());
        final var def = stmtDef.getByNamespaceAndLocalName(uri, node.getLocalName());
        if (def == null && writer.getPhase().equals(ModelProcessingPhase.FULL_DECLARATION)) {
            throw new SourceException(ref, "%s is not a YIN statement or use of extension.", node.getLocalName());
        }
        return def;
    }

    private static boolean processAttribute(final int childId, final Attr attr, final StatementWriter writer,
            final QNameToStatementDefinition stmtDef, final StatementSourceReference ref) {
        final var resumed = writer.resumeStatement(childId);
        if (resumed != null) {
            checkState(resumed.isFullyDefined(), "Statement %s is not fully defined", resumed);
            return true;
        }

        final var def = getValidDefinition(attr, writer, stmtDef, ref);
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
            final var children = element.getElementsByTagNameNS(argName.getNamespace().toString(),
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

    private boolean processElement(final int childId, final Element element, final StatementWriter writer,
            final QNameToStatementDefinition stmtDef) {

        final var resumed = writer.resumeStatement(childId);
        final StatementSourceReference ref;
        final QName argName;
        final boolean allAttrs;
        final boolean allElements;
        if (resumed != null) {
            if (resumed.isFullyDefined()) {
                return true;
            }

            final var def = resumed.getDefinition();
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
            ref = refProvider.getRefOf(element);
            final var def = getValidDefinition(element, writer, stmtDef, ref);
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
                if (argValue == null) {
                    throw new SourceException(ref, "Statement %s is missing mandatory argument %s",
                        def.getStatementName(), argName);
                }
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
        final var attributes = element.getAttributes();
        if (attributes != null) {
            for (int i = 0, len = attributes.getLength(); i < len; ++i) {
                final var attr = (Attr) attributes.item(i);
                if ((allAttrs || !isArgument(argName, attr))
                        && !processAttribute(childCounter++, attr, writer, stmtDef, ref)) {
                    fullyDefined = false;
                }
            }
        }

        // Now process child elements, if present
        final var children = element.getChildNodes();
        for (int i = 0, len = children.getLength(); i < len; ++i) {
            if (children.item(i) instanceof Element child && (allElements || !isArgument(argName, child))
                    && !processElement(childCounter++, child, writer, stmtDef)) {
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
        final var children = root.getChildNodes();

        int childCounter = 0;
        for (int i = 0, len = children.getLength(); i < len; ++i) {
            if (children.item(i) instanceof Element child) {
                processElement(childCounter++, child, writer, stmtDef);
            }
        }
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
