/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.common.YangConstants.RFC6020_YIN_MODULE;
import static org.opendaylight.yangtools.yang.model.api.YangStmtMapping.MODULE;
import static org.opendaylight.yangtools.yang.model.api.YangStmtMapping.REVISION;
import static org.opendaylight.yangtools.yang.model.api.YangStmtMapping.SUBMODULE;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Optional;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utility {@link YinXmlSchemaSource} exposing a W3C {@link DOMSource} representation of YIN model.
 */
public abstract sealed class YinDomSchemaSource implements YinXmlSchemaSource {
    private static final Logger LOG = LoggerFactory.getLogger(YinDomSchemaSource.class);
    private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();
    private static final QName REVISION_STMT = REVISION.getStatementName();
    private static final String MODULE_ARG = MODULE.getArgumentDefinition().orElseThrow()
        .argumentName().getLocalName();
    private static final String REVISION_ARG = REVISION.getArgumentDefinition().orElseThrow()
        .argumentName().getLocalName();

    /**
     * Create a new {@link YinDomSchemaSource} using an identifier and a source.
     *
     * @param identifier Schema source identifier
     * @param source W3C DOM source
     * @param symbolicName Source symbolic name
     * @return A new {@link YinDomSchemaSource} instance.
     */
    public static @NonNull YinDomSchemaSource create(final @NonNull SourceIdentifier identifier,
            final @NonNull DOMSource source, final @Nullable String symbolicName) {

        final Node root = source.getNode().getFirstChild();
        final String rootNs = root.getNamespaceURI();
        if (rootNs == null) {
            // Let whoever is using this deal with this
            return new Simple(identifier, source, symbolicName);
        }

        final QName qname = QName.create(rootNs, root.getLocalName());
        checkArgument(RFC6020_YIN_MODULE.equals(qname.getModule()),
            "Root node namepsace %s does not match %s", rootNs, YangConstants.RFC6020_YIN_NAMESPACE);
        checkArgument(MODULE.getStatementName().equals(qname)
            || SUBMODULE.getStatementName().equals(qname), "Root element %s is not a module nor a submodule", qname);

        checkArgument(root instanceof Element, "Root node %s is not an element", root);
        final Element element = (Element)root;

        final Attr nameAttr = element.getAttributeNode(MODULE_ARG);
        checkArgument(nameAttr != null, "No %s name argument found in %s", element.getLocalName());

        final NodeList revisions = element.getElementsByTagNameNS(REVISION_STMT.getNamespace().toString(),
            REVISION_STMT.getLocalName());
        if (revisions.getLength() == 0) {
            // FIXME: is module name important (as that may have changed)
            return new Simple(identifier, source, symbolicName);
        }

        final Element revisionStmt = (Element) revisions.item(0);
        final Attr dateAttr = revisionStmt.getAttributeNode(REVISION_ARG);
        checkArgument(dateAttr != null, "No revision statement argument found in %s", revisionStmt);

        final var parsedId = new SourceIdentifier(nameAttr.getValue(), dateAttr.getValue());
        final SourceIdentifier id;
        if (!parsedId.equals(identifier)) {
            LOG.debug("Changed identifier from {} to {}", identifier, parsedId);
            id = parsedId;
        } else {
            id = identifier;
        }

        return new Simple(id, source, symbolicName);
    }

    /**
     * Create a {@link YinDomSchemaSource} from a {@link YinXmlSchemaSource}. If the argument is already a
     * YinDomSchemaSource, this method returns the same instance. The source will be translated on first access,
     * at which point an {@link IllegalStateException} may be raised.
     *
     * @param xmlSchemaSource Backing schema source
     * @return A {@link YinDomSchemaSource} instance
     */
    public static @NonNull YinDomSchemaSource lazyTransform(final YinXmlSchemaSource xmlSchemaSource) {
        final var cast = castSchemaSource(xmlSchemaSource);
        return cast != null ? cast : new Transforming(xmlSchemaSource);
    }

    /**
     * Create a {@link YinDomSchemaSource} from a {@link YinXmlSchemaSource}. If the argument is already a
     * YinDomSchemaSource, this method returns the same instance. The source will be translated immediately.
     *
     * @param xmlSchemaSource Backing schema source
     * @return A {@link YinDomSchemaSource} instance
     * @throws TransformerException when the provided source fails to transform
     */
    public static @NonNull YinDomSchemaSource transform(final YinXmlSchemaSource xmlSchemaSource)
            throws TransformerException {
        final var cast = castSchemaSource(xmlSchemaSource);
        return cast != null ? cast :
            create(xmlSchemaSource.sourceId(), transformSource(xmlSchemaSource.getSource()),
                xmlSchemaSource.getSymbolicName().orElse(null));
    }

    @Override
    public final Class<YinDomSchemaSource> getType() {
        return YinDomSchemaSource.class;
    }

    @Override
    public abstract DOMSource getSource();

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).add("sourceId", sourceId())).toString();
    }

    /**
     * Add subclass-specific attributes to the output {@link #toString()} output. Since
     * subclasses are prevented from overriding {@link #toString()} for consistency
     * reasons, they can add their specific attributes to the resulting string by attaching
     * attributes to the supplied {@link ToStringHelper}.
     *
     * @param toStringHelper ToStringHelper onto the attributes can be added
     * @return ToStringHelper supplied as input argument.
     */
    protected abstract ToStringHelper addToStringAttributes(ToStringHelper toStringHelper);

    static @NonNull DOMSource transformSource(final Source source) throws TransformerException {
        final DOMResult result = new DOMResult();
        TRANSFORMER_FACTORY.newTransformer().transform(source, result);

        return new DOMSource(result.getNode(), result.getSystemId());
    }

    private static @Nullable YinDomSchemaSource castSchemaSource(final YinXmlSchemaSource xmlSchemaSource) {
        if (xmlSchemaSource instanceof YinDomSchemaSource yinDom) {
            return yinDom;
        }

        final var source = xmlSchemaSource.getSource();
        if (source instanceof DOMSource dom) {
            return create(xmlSchemaSource.sourceId(), dom, xmlSchemaSource.getSymbolicName().orElse(null));
        }
        return null;
    }

    private static final class Simple extends YinDomSchemaSource {
        private final @NonNull SourceIdentifier sourceId;
        private final @NonNull DOMSource source;
        private final String symbolicName;

        Simple(final @NonNull SourceIdentifier sourceId, final @NonNull DOMSource source,
                final @Nullable String symbolicName) {
            this.sourceId = requireNonNull(sourceId);
            this.source = requireNonNull(source);
            this.symbolicName = symbolicName;
        }

        @Override
        public SourceIdentifier sourceId() {
            return sourceId;
        }

        @Override
        public DOMSource getSource() {
            return source;
        }

        @Override
        public Optional<String> getSymbolicName() {
            return Optional.ofNullable(symbolicName);
        }

        @Override
        protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
            return toStringHelper.add("source", source);
        }
    }

    private static final class Transforming extends YinDomSchemaSource {
        private final YinXmlSchemaSource xmlSchemaSource;

        private volatile DOMSource source;

        Transforming(final YinXmlSchemaSource xmlSchemaSource) {
            this.xmlSchemaSource = requireNonNull(xmlSchemaSource);
        }

        @Override
        public SourceIdentifier sourceId() {
            return xmlSchemaSource.sourceId();
        }

        @Override
        public Optional<String> getSymbolicName() {
            return xmlSchemaSource.getSymbolicName();
        }

        @Override
        public DOMSource getSource() {
            DOMSource ret = source;
            if (ret == null) {
                synchronized (this) {
                    ret = source;
                    if (ret == null) {
                        try {
                            ret = transformSource(xmlSchemaSource.getSource());
                        } catch (TransformerException e) {
                            throw new IllegalStateException("Failed to transform schema source " + xmlSchemaSource, e);
                        }
                        source = ret;
                    }
                }
            }

            return ret;
        }

        @Override
        protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
            return toStringHelper.add("xmlSchemaSource", xmlSchemaSource);
        }
    }
}
