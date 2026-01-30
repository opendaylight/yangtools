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

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.VerifyException;
import java.util.NoSuchElementException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.ExtractorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utility {@link YinXmlSource} exposing a W3C {@link DOMSource} representation of YIN model.
 */
public abstract sealed class YinDomSource implements YinXmlSource, SourceInfo.Extractor {
    /**
     * Interface for extracting {@link StatementSourceReference} from an {@link Element}.
     *
     * @since 14.0.22
     */
    @Beta
    @NonNullByDefault
    @FunctionalInterface
    public interface SourceRefProvider {
        /**
         * Returns an {@link Element}'s {@link StatementSourceReference}, if available.
         *
         * @param element the {@link Element}
         * @return the {@link StatementSourceReference} or {@code null} if not available
         */
        @Nullable StatementSourceReference refOf(Element element);

        /**
         * Returns an {@link Element}'s {@link StatementSourceReference}.
         *
         * @param element the {@link Element}
         * @return the {@link StatementSourceReference}
         * @throws NoSuchElementException if
         */
        default StatementSourceReference getRefOf(final Element element) {
            final var ref = refOf(element);
            if (ref == null) {
                throw new NoSuchElementException("No reference available for element " + element.getNodeName());
            }
            return ref;
        }
    }

    private static final class Simple extends YinDomSource {
        private final @NonNull SourceIdentifier sourceId;
        private final @NonNull DOMSource source;
        private final String symbolicName;

        @NonNullByDefault
        Simple(final SourceIdentifier sourceId, final DOMSource source, final SourceRefProvider refProvider,
                final @Nullable String symbolicName) {
            super(refProvider);
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
        public String symbolicName() {
            return symbolicName;
        }

        @Override
        protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
            return toStringHelper.add("source", source);
        }
    }

    private static final class Transforming extends YinDomSource {
        private final YinXmlSource xmlSchemaSource;

        private volatile DOMSource source;

        Transforming(final YinXmlSource xmlSchemaSource) {
            super(element -> null);
            this.xmlSchemaSource = requireNonNull(xmlSchemaSource);
        }

        @Override
        public SourceIdentifier sourceId() {
            return xmlSchemaSource.sourceId();
        }

        @Override
        public String symbolicName() {
            return xmlSchemaSource.symbolicName();
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


    private static final Logger LOG = LoggerFactory.getLogger(YinDomSource.class);
    private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();
    private static final QName REVISION_STMT = RevisionStatement.DEF.statementName();
    private static final String MODULE_ARG = ModuleStatement.DEF.getArgumentDefinition().simpleName();
    private static final String REVISION_ARG = RevisionStatement.DEF.getArgumentDefinition().simpleName();

    private final @NonNull SourceRefProvider refProvider;

    @NonNullByDefault
    YinDomSource(final SourceRefProvider refProvider) {
        this.refProvider = requireNonNull(refProvider);
    }

    /**
     * Create a new {@link YinDomSource} using an identifier and a source.
     *
     * @param identifier Schema source identifier
     * @param source W3C DOM source
     * @param symbolicName Source symbolic name
     * @return A new {@link YinDomSource} instance.
     * @deprecated Use {@link #of(SourceIdentifier, DOMSource, SourceRefProvider, String)} instead
     */
    @Deprecated
    @NonNullByDefault
    public static YinDomSource create(final SourceIdentifier identifier, final DOMSource source,
            final @Nullable String symbolicName) {
        return of(identifier, source, element -> null, symbolicName);
    }

    /**
     * Create a new {@link YinDomSource} using an identifier and a source.
     *
     * @param identifier Schema source identifier
     * @param source W3C DOM source
     * @param refProvider the {@link SourceRefProvider}
     * @param symbolicName Source symbolic name
     * @return A new {@link YinDomSource} instance.
     * @since 14.0.22
     */
    @NonNullByDefault
    public static YinDomSource of(final SourceIdentifier identifier, final DOMSource source,
            final SourceRefProvider refProvider, final @Nullable String symbolicName) {
        final Node root = source.getNode().getFirstChild();
        final String rootNs = root.getNamespaceURI();
        if (rootNs == null) {
            // Let whoever is using this deal with this
            return new Simple(identifier, source, refProvider, symbolicName);
        }

        if (!YangConstants.RFC6020_YIN_NAMESPACE_STRING.equals(rootNs)) {
            throw new IllegalArgumentException("Root node namepsace " + rootNs + " does not match "
                + YangConstants.RFC6020_YIN_NAMESPACE_STRING);
        }

        final var rootName = root.getLocalName();
        if (!rootName.equals(ModuleStatement.DEF.simpleName())
            && !rootName.equals(SubmoduleStatement.DEF.simpleName())) {
            throw new IllegalArgumentException("Root element " + rootName + " is not a module nor a submodule");
        }

        checkArgument(root instanceof Element, "Root node %s is not an element", root);
        final Element element = (Element)root;

        final Attr nameAttr = element.getAttributeNode(MODULE_ARG);
        checkArgument(nameAttr != null, "No %s name argument found in %s", MODULE_ARG, element.getLocalName());

        final NodeList revisions = element.getElementsByTagNameNS(REVISION_STMT.getNamespace().toString(),
            REVISION_STMT.getLocalName());
        if (revisions.getLength() == 0) {
            // FIXME: is module name important (as that may have changed)
            return new Simple(identifier, source, refProvider, symbolicName);
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

        return new Simple(id, source, refProvider, symbolicName);
    }

    /**
     * Create a {@link YinDomSource} from a {@link YinXmlSource}. If the argument is already a
     * YinDomSchemaSource, this method returns the same instance. The source will be translated on first access,
     * at which point an {@link IllegalStateException} may be raised.
     *
     * @param xmlSchemaSource Backing schema source
     * @return A {@link YinDomSource} instance
     */
    public static @NonNull YinDomSource lazyTransform(final YinXmlSource xmlSchemaSource) {
        final var cast = castSchemaSource(xmlSchemaSource);
        return cast != null ? cast : new Transforming(xmlSchemaSource);
    }

    /**
     * Create a {@link YinDomSource} from a {@link YinXmlSource}. If the argument is already a
     * YinDomSchemaSource, this method returns the same instance. The source will be translated immediately.
     *
     * @param xmlSchemaSource Backing schema source
     * @return A {@link YinDomSource} instance
     * @throws TransformerException when the provided source fails to transform
     */
    public static @NonNull YinDomSource transform(final YinXmlSource xmlSchemaSource)
            throws TransformerException {
        final var cast = castSchemaSource(xmlSchemaSource);
        return cast != null ? cast :
            create(xmlSchemaSource.sourceId(), transformSource(xmlSchemaSource.getSource()),
                xmlSchemaSource.symbolicName());
    }

    @Override
    public final Class<YinDomSource> getType() {
        return YinDomSource.class;
    }

    @Override
    public abstract DOMSource getSource();

    /**
     * {@return the {@link SourceRefProvider} attached to this source}
     * @since 14.0.22
     */
    public final @NonNull SourceRefProvider refProvider() {
        return refProvider;
    }

    @Override
    public final SourceInfo extractSourceInfo() throws ExtractorException {
        final var root = getSource().getNode();
        if (!(root instanceof Document document)) {
            throw new VerifyException("Unexpected root " + root);
        }

        final var element = document.getDocumentElement();
        if (!YinDomSourceInfoExtractor.isYinElement(element)) {
            throw new ExtractorException("Root element does not have YIN namespace", refProvider.refOf(element));
        }

        final var extractor = switch (element.getLocalName()) {
            case SourceInfoExtractors.MODULE -> new YinDomSourceInfoExtractor.ForModule(element, refProvider);
            case SourceInfoExtractors.SUBMODULE -> new YinDomSourceInfoExtractor.ForSubmodule(element, refProvider);
            default -> throw new ExtractorException("Root element needs to be a module or submodule",
                refProvider.refOf(element));
        };
        return extractor.extractSourceInfo();
    }

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

    private static @Nullable YinDomSource castSchemaSource(final YinXmlSource xmlSchemaSource) {
        if (xmlSchemaSource instanceof YinDomSource yinDom) {
            return yinDom;
        }

        final var source = xmlSchemaSource.getSource();
        if (source instanceof DOMSource dom) {
            return create(xmlSchemaSource.sourceId(), dom, xmlSchemaSource.symbolicName());
        }
        return null;
    }
}
