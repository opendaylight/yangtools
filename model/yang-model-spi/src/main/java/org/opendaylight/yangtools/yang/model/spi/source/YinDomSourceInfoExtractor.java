/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import java.time.format.DateTimeParseException;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.source.SourceDependency;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.ExtractorException;
import org.w3c.dom.Element;

/**
 * Utility class for extract {@link SourceInfo} from a {@link YinDomSource}.
 */
@NonNullByDefault
abstract sealed class YinDomSourceInfoExtractor implements SourceInfo.Extractor {
    static final class ForModule extends YinDomSourceInfoExtractor {
        private static final String NAMESPACE = YangStmtMapping.NAMESPACE.getStatementName().getLocalName();
        private static final String NAMESPACE_ARG =
            YangStmtMapping.NAMESPACE.getArgumentDefinition().orElseThrow().argumentName().getLocalName();

        ForModule(final Element root, final Function<Element, @Nullable StatementSourceReference> elementToRef) {
            super(root, elementToRef);
        }

        @Override
        public SourceInfo.Module extractSourceInfo() throws ExtractorException {
            final var builder = SourceInfo.Module.builder();
            fillCommon(builder);

            final var nsElement = getFirstElement(root, NAMESPACE);
            final var nsArg = getElementArgumentString(nsElement, NAMESPACE_ARG);
            final XMLNamespace namespace;
            try {
                namespace = XMLNamespace.of(nsArg);
            } catch (IllegalArgumentException e) {
                throw newInvalidArgument(nsElement, e);
            }

            return builder
                .setPrefix(extractPrefix(root))
                .setNamespace(namespace)
                .build();
        }
    }

    static final class ForSubmodule extends YinDomSourceInfoExtractor {
        private static final String BELONGS_TO = YangStmtMapping.BELONGS_TO.getStatementName().getLocalName();
        private static final String BELONGS_TO_ARG =
            YangStmtMapping.BELONGS_TO.getArgumentDefinition().orElseThrow().argumentName().getLocalName();

        ForSubmodule(final Element root, final Function<Element, @Nullable StatementSourceReference> elementToRef) {
            super(root, elementToRef);
        }

        @Override
        public SourceInfo.Submodule extractSourceInfo() throws ExtractorException {
            final var builder = SourceInfo.Submodule.builder();
            fillCommon(builder);

            final var belongsTo = getFirstElement(root, BELONGS_TO);
            return builder
                .setBelongsTo(new SourceDependency.BelongsTo(getElementArgumentIdentifier(belongsTo, BELONGS_TO_ARG),
                    extractPrefix(belongsTo)))
                .build();
        }
    }

    private static final String INCLUDE = "include";
    private static final String INCLUDE_ARG =
        YangStmtMapping.INCLUDE.getArgumentDefinition().orElseThrow().argumentName().getLocalName();
    private static final String IMPORT = "import";
    private static final String IMPORT_ARG =
        YangStmtMapping.IMPORT.getArgumentDefinition().orElseThrow().argumentName().getLocalName();
    private static final String REVISION = "revision";
    private static final String REVISION_ARG =
        YangStmtMapping.REVISION.getArgumentDefinition().orElseThrow().argumentName().getLocalName();

    // module, submodule and their common argument name
    private static final String MODULE = "module";
    private static final String SUBMODULE = "submodule";
    private static final String NAME = "name";

    static {
        verify(IMPORT.equals(YangStmtMapping.IMPORT.getStatementName().getLocalName()));
        verify(INCLUDE.equals(YangStmtMapping.INCLUDE.getStatementName().getLocalName()));
        verify(REVISION.equals(YangStmtMapping.REVISION.getStatementName().getLocalName()));
        verify(MODULE.equals(YangStmtMapping.MODULE.getStatementName().getLocalName()));
        verify(SUBMODULE.equals(YangStmtMapping.SUBMODULE.getStatementName().getLocalName()));
        verify(NAME.equals(YangStmtMapping.MODULE.getArgumentDefinition().orElseThrow().argumentName().getLocalName()));
        verify(NAME.equals(YangStmtMapping.SUBMODULE.getArgumentDefinition().orElseThrow().argumentName()
            .getLocalName()));
    }

    private static final String PREFIX = YangStmtMapping.PREFIX.getStatementName().getLocalName();
    private static final String PREFIX_ARG =
        YangStmtMapping.PREFIX.getArgumentDefinition().orElseThrow().argumentName().getLocalName();
    private static final String REVISION_DATE =
        YangStmtMapping.REVISION_DATE.getStatementName().getLocalName();
    private static final String REVISION_DATE_ARG =
        YangStmtMapping.REVISION_DATE.getArgumentDefinition().orElseThrow().argumentName().getLocalName();
    private static final String YANG_VERSION = YangStmtMapping.YANG_VERSION.getStatementName().getLocalName();
    private static final String YANG_VERSION_ARG =
        YangStmtMapping.YANG_VERSION.getArgumentDefinition().orElseThrow().argumentName().getLocalName();

    private final Function<Element, @Nullable StatementSourceReference> elementToRef;
    final Element root;

    YinDomSourceInfoExtractor(final Element root,
            final Function<Element, @Nullable StatementSourceReference> elementToRef) {
        this.root = requireNonNull(root);
        this.elementToRef = requireNonNull(elementToRef);
    }

    final void fillCommon(final SourceInfo.Builder<?, ?> builder) throws ExtractorException {
        builder.setName(getElementArgumentIdentifier(root, NAME)).setYangVersion(extractYangVersion());

        final var childNodes = root.getChildNodes();
        for (int i = 0, length = childNodes.getLength(); i < length; ++i) {
            if (childNodes.item(i) instanceof Element element && isYinElement(element)) {
                switch (element.getLocalName()) {
                    case IMPORT -> builder.addImport(new SourceDependency.Import(
                            getElementArgumentIdentifier(element, IMPORT_ARG),
                            extractPrefix(element), extractRevisionDate(element)));
                    case INCLUDE -> builder.addInclude(new SourceDependency.Include(
                            getElementArgumentIdentifier(element, INCLUDE_ARG),
                            extractRevisionDate(element)));
                    case REVISION -> builder.addRevision(getElementArgumentRevision(element, REVISION_ARG));
                    case null, default -> {
                        // No-op
                    }
                }
            }
        }
    }

    final Unqualified extractPrefix(final Element parent) throws ExtractorException {
        return getElementArgumentIdentifier(getFirstElement(parent, PREFIX), PREFIX_ARG);
    }

    private @Nullable Revision extractRevisionDate(final Element parent) throws ExtractorException {
        final var element = firstElement(parent, REVISION_DATE);
        return element == null ? null : getElementArgumentRevision(element, REVISION_DATE_ARG);
    }

    private YangVersion extractYangVersion() throws ExtractorException {
        final var element = firstElement(root, YANG_VERSION);
        if (element == null) {
            return YangVersion.VERSION_1;
        }

        final var arg = getElementArgumentString(element, YANG_VERSION_ARG);
        try {
            return YangVersion.ofString(arg);
        } catch (IllegalArgumentException e) {
            throw newInvalidArgument(element, e);
        }
    }

    private static @Nullable Element firstElement(final Element parent, final String keyword) {
        final var childNodes = parent.getChildNodes();
        for (int i = 0, length = childNodes.getLength(); i < length; ++i) {
            if (childNodes.item(i) instanceof Element element && isYinElement(element, keyword)) {
                return element;
            }
        }
        return null;
    }

    final Element getFirstElement(final Element parent, final String keyword) throws ExtractorException {
        final var element = firstElement(parent, keyword);
        if (element == null) {
            throw newMissingSubstatement(parent, keyword);
        }
        return element;
    }

    final Unqualified getElementArgumentIdentifier(final Element element, final String argument)
            throws ExtractorException {
        final var arg = getElementArgumentString(element, argument);
        try {
            return Unqualified.of(arg);
        } catch (IllegalArgumentException e) {
            throw newInvalidArgument(element, e);
        }
    }

    private Revision getElementArgumentRevision(final Element element, final String argument)
            throws ExtractorException {
        final var arg = getElementArgumentString(element, argument);
        try {
            return Revision.of(arg);
        } catch (DateTimeParseException e) {
            throw newInvalidArgument(element, e);
        }
    }

    final String getElementArgumentString(final Element element, final String argument) throws ExtractorException {
        final var arg = element.getAttributeNodeNS(null, argument);
        if (arg == null || !arg.getSpecified()) {
            throw new ExtractorException("Missing argument to " + element.getLocalName(), refOf(element));
        }
        return arg.getValue();
    }

    final ExtractorException newInvalidArgument(final Element stmt, final Exception cause) {
        return new ExtractorException(
            "Invalid argument to " + stmt.getLocalName() + ": " + cause.getMessage(), cause, refOf(stmt));
    }

    private ExtractorException newMissingSubstatement(final Element parent, final String keyword) {
        return new ExtractorException("Missing " + keyword + " substatement", refOf(parent));
    }

    private @Nullable StatementSourceReference refOf(final Element element) {
        return elementToRef.apply(element);
    }

    static boolean isYinElement(final Element element) {
        return YangConstants.RFC6020_YIN_NAMESPACE_STRING.equals(element.getNamespaceURI());
    }

    private static boolean isYinElement(final Element element, final String keyword) {
        return isYinElement(element) && keyword.equals(element.getLocalName());
    }
}
