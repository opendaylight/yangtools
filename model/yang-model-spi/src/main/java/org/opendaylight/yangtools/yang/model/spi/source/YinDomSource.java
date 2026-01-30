/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.VerifyException;
import java.util.NoSuchElementException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YinSourceRepresentation;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.ExtractorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Utility {@link YinSourceRepresentation} exposing a W3C {@link DOMSource} representation of YIN model.
 */
public final class YinDomSource implements YinSourceRepresentation, SourceInfo.Extractor {
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

    private static final Logger LOG = LoggerFactory.getLogger(YinDomSource.class);
    private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();
    private static final QName REVISION_STMT = RevisionStatement.DEF.statementName();
    private static final String MODULE_ARG = ModuleStatement.DEF.getArgumentDefinition().simpleName();
    private static final String REVISION_ARG = RevisionStatement.DEF.getArgumentDefinition().simpleName();
    private static final String MODULE = "module";
    private static final String SUBMODULE = "submodule";

    static {
        verify(MODULE.equals(ModuleStatement.DEF.simpleName()));
        verify(SUBMODULE.equals(SubmoduleStatement.DEF.simpleName()));
    }

    private final @NonNull SourceRefProvider refProvider;
    private final @NonNull SourceIdentifier sourceId;
    private final @NonNull DOMSource domSource;
    private final String symbolicName;

    @NonNullByDefault
    YinDomSource(final SourceIdentifier sourceId, final DOMSource source, final SourceRefProvider refProvider,
            final @Nullable String symbolicName) {
        this.sourceId = requireNonNull(sourceId);
        domSource = requireNonNull(source);
        this.refProvider = requireNonNull(refProvider);
        this.symbolicName = symbolicName;
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
        final var element = documentElementOf(source);
        final var rootNs = element.getNamespaceURI();
        if (rootNs == null) {
            // Let whoever is using this deal with this
            return new YinDomSource(identifier, source, refProvider, symbolicName);
        }

        if (!YangConstants.RFC6020_YIN_NAMESPACE_STRING.equals(rootNs)) {
            throw new IllegalArgumentException("Root node namepsace " + rootNs + " does not match "
                + YangConstants.RFC6020_YIN_NAMESPACE_STRING);
        }

        final var rootName = element.getLocalName();
        switch (rootName) {
            case MODULE, SUBMODULE -> {
                // No-op
            }
            default -> throw new IllegalArgumentException(
                "Root element " + rootName + " is not a module nor a submodule");
        }

        final var nameAttr = element.getAttributeNode(MODULE_ARG);
        checkArgument(nameAttr != null, "No %s name argument found in %s", MODULE_ARG, element.getLocalName());

        final var revisions = element.getElementsByTagNameNS(REVISION_STMT.getNamespace().toString(),
            REVISION_STMT.getLocalName());
        if (revisions.getLength() == 0) {
            // FIXME: is module name important (as that may have changed)
            return new YinDomSource(identifier, source, refProvider, symbolicName);
        }

        // FIXME: better check
        final var revisionStmt = (Element) revisions.item(0);
        final var dateAttr = revisionStmt.getAttributeNode(REVISION_ARG);
        checkArgument(dateAttr != null, "No revision statement argument found in %s", revisionStmt);

        final var parsedId = new SourceIdentifier(nameAttr.getValue(), dateAttr.getValue());
        final SourceIdentifier id;
        if (!parsedId.equals(identifier)) {
            LOG.debug("Changed identifier from {} to {}", identifier, parsedId);
            id = parsedId;
        } else {
            id = identifier;
        }

        return new YinDomSource(id, source, refProvider, symbolicName);
    }

    @Override
    public Class<YinDomSource> getType() {
        return YinDomSource.class;
    }

    @Override
    public SourceIdentifier sourceId() {
        return sourceId;
    }

    @Override
    public String symbolicName() {
        return symbolicName;
    }

    /**
     * {@return the underlying {@link DOMSource}}
     */
    public @NonNull DOMSource domSource() {
        return domSource;
    }

    /**
     * {@return the {@link SourceRefProvider} attached to this source}
     * @since 14.0.22
     */
    public @NonNull SourceRefProvider refProvider() {
        return refProvider;
    }

    @Override
    public SourceInfo extractSourceInfo() throws ExtractorException {
        final var element = documentElementOf(domSource());
        if (!YinDomSourceInfoExtractor.isYinElement(element)) {
            throw new ExtractorException("Root element does not have YIN namespace", refProvider.refOf(element));
        }

        final var extractor = switch (element.getLocalName()) {
            case MODULE -> new YinDomSourceInfoExtractor.ForModule(element, refProvider);
            case SUBMODULE -> new YinDomSourceInfoExtractor.ForSubmodule(element, refProvider);
            default -> throw new ExtractorException("Root element needs to be a module or submodule",
                refProvider.refOf(element));
        };
        return extractor.extractSourceInfo();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("sourceId", sourceId()).add("domSource", domSource).toString();
    }

    @NonNullByDefault
    private static Element documentElementOf(final DOMSource source) {
        final var node = source.getNode();
        if (node instanceof Document document) {
            return document.getDocumentElement();
        }
        throw new VerifyException("Unexpected root " + node);
    }
}
