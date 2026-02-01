/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.VerifyException;
import java.util.NoSuchElementException;
import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.SourceSyntaxException;
import org.opendaylight.yangtools.yang.model.api.source.YinSourceRepresentation;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Utility {@link YinSourceRepresentation} exposing a W3C {@link DOMSource} representation of YIN model.
 */
@NonNullByDefault
public abstract sealed class YinDOMSource
        implements YinSourceRepresentation, MaterializedSourceRepresentation<YinDOMSource, Element>
        permits YinDOMModuleSource, YinDOMSubmoduleSource {
    /**
     * Interface for extracting {@link StatementSourceReference} from an {@link Element}.
     *
     * @since 14.0.22
     */
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

    private static final String MODULE_ARG = ModuleStatement.DEF.getArgumentDefinition().simpleName();
    private static final String MODULE = "module";
    private static final String SUBMODULE = "submodule";

    static {
        verify(MODULE.equals(ModuleStatement.DEF.simpleName()));
        verify(SUBMODULE.equals(SubmoduleStatement.DEF.simpleName()));
    }

    private final SourceRefProvider refProvider;
    private final SourceIdentifier sourceId;
    private final DOMSource domSource;
    private final @Nullable String symbolicName;

    YinDOMSource(final SourceIdentifier sourceId, final DOMSource domSource, final SourceRefProvider refProvider,
            final @Nullable String symbolicName) {
        this.sourceId = requireNonNull(sourceId);
        this.domSource = requireNonNull(domSource);
        this.refProvider = requireNonNull(refProvider);
        this.symbolicName = symbolicName;
    }

    /**
     * Create a new {@link YinDOMSource} using an identifier and a source.
     *
     * @param identifier Schema source identifier
     * @param domSource W3C DOM source
     * @param refProvider the {@link SourceRefProvider}
     * @param symbolicName Source symbolic name
     * @return A new {@link YinDOMSource} instance
     * @throws SourceSyntaxException if the {@code domSource} is not a valid root
     * @since 14.0.22
     */
    public static final YinDOMSource of(final SourceIdentifier identifier, final DOMSource domSource,
            final SourceRefProvider refProvider, final @Nullable String symbolicName) throws SourceSyntaxException {
        final var element = documentElementOf(domSource);
        final var rootNs = element.getNamespaceURI();
        if (!YangConstants.RFC6020_YIN_NAMESPACE_STRING.equals(rootNs)) {
            throw new SourceSyntaxException(
                "Root node namepsace " + rootNs + " does not match " + YangConstants.RFC6020_YIN_NAMESPACE_STRING,
                refProvider.refOf(element));
        }

        final var rootName = element.getLocalName();
        final var nameAttr = element.getAttributeNode(MODULE_ARG);
        if (nameAttr == null) {
            throw new SourceSyntaxException("No " + MODULE_ARG + " name argument found in " + rootName,
                refProvider.refOf(element));
        }

        return switch (rootName) {
            case MODULE -> new YinDOMModuleSource(identifier, domSource, refProvider, symbolicName);
            case SUBMODULE -> new YinDOMSubmoduleSource(identifier, domSource, refProvider, symbolicName);
            default -> throw new SourceSyntaxException("Root element " + rootName + " is not a module nor a submodule",
                refProvider.refOf(element));
        };
    }

    @Override
    public final Class<YinDOMSource> getType() {
        return YinDOMSource.class;
    }

    @Override
    public final SourceIdentifier sourceId() {
        return sourceId;
    }

    @Override
    public final @Nullable String symbolicName() {
        return symbolicName;
    }

    @Override
    public final Element statement() {
        return documentElementOf(domSource);
    }

    /**
     * {@return the underlying {@link DOMSource}}
     */
    public final DOMSource domSource() {
        return domSource;
    }

    /**
     * {@return the {@link SourceRefProvider} attached to this source}
     * @since 14.0.22
     */
    public final SourceRefProvider refProvider() {
        return refProvider;
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("sourceId", sourceId()).add("domSource", domSource).toString();
    }

    private static Element documentElementOf(final DOMSource domSource) {
        final var node = domSource.getNode();
        if (node instanceof Document document) {
            return document.getDocumentElement();
        }
        throw new VerifyException("Unexpected root " + node);
    }
}
