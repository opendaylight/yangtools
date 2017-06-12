/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import static org.opendaylight.yangtools.yang.common.YangConstants.RFC6020_YIN_MODULE;
import static org.opendaylight.yangtools.yang.model.api.YangStmtMapping.MODULE;
import static org.opendaylight.yangtools.yang.model.api.YangStmtMapping.REVISION;
import static org.opendaylight.yangtools.yang.model.api.YangStmtMapping.SUBMODULE;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
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
public abstract class YinDomSchemaSource implements YinXmlSchemaSource {
    private static final Logger LOG = LoggerFactory.getLogger(YinDomSchemaSource.class);
    private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();
    private static final QName REVISION_STMT = REVISION.getStatementName();

    YinDomSchemaSource() {
        // Prevent outside instantiation
    }

    /**
     * Create a new {@link YinDomSchemaSource} using an identifier and a source.
     *
     * @param identifier Schema source identifier
     * @param source W3C DOM source
     * @return A new {@link YinDomSchemaSource} instance.
     */
    public static @Nonnull YinDomSchemaSource create(@Nonnull final SourceIdentifier identifier,
            final @Nonnull DOMSource source) {

        final Node root = source.getNode().getFirstChild();
        final String rootNs = root.getNamespaceURI();
        if (rootNs == null) {
            // Let whoever is using this deal with this
            return new Simple(identifier, source);
        }

        final QName qname = QName.create(rootNs, root.getLocalName());
        Preconditions.checkArgument(RFC6020_YIN_MODULE.equals(qname.getModule()),
            "Root node namepsace %s does not match %s", rootNs, YangConstants.RFC6020_YIN_NAMESPACE);
        Preconditions.checkArgument(MODULE.getStatementName().equals(qname)
            || SUBMODULE.getStatementName().equals(qname), "Root element %s is not a module nor a submodule", qname);

        Preconditions.checkArgument(root instanceof Element, "Root node %s is not an element", root);
        final Element element = (Element)root;

        final Attr nameAttr = element.getAttributeNode(MODULE.getArgumentName().getLocalName());
        Preconditions.checkArgument(nameAttr != null, "No %s name argument found in %s", element.getLocalName());

        final NodeList revisions = element.getElementsByTagNameNS(REVISION_STMT.getNamespace().toString(),
            REVISION_STMT.getLocalName());
        if (revisions.getLength() == 0) {
            // FIXME: is module name important (as that may have changed)
            return new Simple(identifier, source);
        }

        final Element revisionStmt = (Element) revisions.item(0);
        final Attr dateAttr = revisionStmt.getAttributeNode(REVISION.getArgumentName().getLocalName());
        Preconditions.checkArgument(dateAttr != null, "No revision statement argument found in %s", revisionStmt);

        final SourceIdentifier parsedId = RevisionSourceIdentifier.create(nameAttr.getValue(),
            Optional.of(dateAttr.getValue()));
        final SourceIdentifier id;
        if (!parsedId.equals(identifier)) {
            LOG.debug("Changed identifier from {} to {}", identifier, parsedId);
            id = parsedId;
        } else {
            id = identifier;
        }

        return new Simple(id, source);
    }

    /**
     * Create a {@link YinDomSchemaSource} from a {@link YinXmlSchemaSource}. If the argument is already a
     * YinDomSchemaSource, this method returns the same instance. The source will be translated on first access,
     * at which point an {@link IllegalStateException} may be raised.
     *
     * @param xmlSchemaSource Backing schema source
     * @return A {@link YinDomSchemaSource} instance
     */
    @Nonnull public static YinDomSchemaSource lazyTransform(final YinXmlSchemaSource xmlSchemaSource) {
        final YinDomSchemaSource cast = castSchemaSource(xmlSchemaSource);
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
    @Nonnull public static YinDomSchemaSource transform(final YinXmlSchemaSource xmlSchemaSource)
            throws TransformerException {
        final YinDomSchemaSource cast = castSchemaSource(xmlSchemaSource);
        return cast != null ? cast :
            create(xmlSchemaSource.getIdentifier(), transformSource(xmlSchemaSource.getSource()));
    }

    @Override
    @Nonnull public abstract DOMSource getSource();

    @Override
    public final Class<? extends YinXmlSchemaSource> getType() {
        return YinDomSchemaSource.class;
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).add("identifier", getIdentifier())).toString();
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

    static DOMSource transformSource(final Source source) throws TransformerException {
        final DOMResult result = new DOMResult();
        TRANSFORMER_FACTORY.newTransformer().transform(source, result);

        return new DOMSource(result.getNode(), result.getSystemId());
    }

    private static YinDomSchemaSource castSchemaSource(final YinXmlSchemaSource xmlSchemaSource) {
        if (xmlSchemaSource instanceof YinDomSchemaSource) {
            return (YinDomSchemaSource) xmlSchemaSource;
        }

        final Source source = xmlSchemaSource.getSource();
        if (source instanceof DOMSource) {
            return create(xmlSchemaSource.getIdentifier(), (DOMSource) source);
        }

        return null;
    }

    private static final class Simple extends YinDomSchemaSource {
        private final SourceIdentifier identifier;
        private final DOMSource source;

        Simple(@Nonnull final SourceIdentifier identifier, @Nonnull final DOMSource source) {
            this.identifier = Preconditions.checkNotNull(identifier);
            this.source = Preconditions.checkNotNull(source);
        }

        @Nonnull
        @Override
        public DOMSource getSource() {
            return source;
        }

        @Override
        public SourceIdentifier getIdentifier() {
            return identifier;
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
            this.xmlSchemaSource = Preconditions.checkNotNull(xmlSchemaSource);
        }

        @Nonnull
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
        public SourceIdentifier getIdentifier() {
            return xmlSchemaSource.getIdentifier();
        }

        @Override
        protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
            return toStringHelper.add("xmlSchemaSource", xmlSchemaSource);
        }
    }
}
