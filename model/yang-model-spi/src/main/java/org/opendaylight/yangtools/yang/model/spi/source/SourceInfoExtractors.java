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

import com.google.common.annotations.Beta;
import java.util.function.Function;
import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.ir.IRKeyword;
import org.opendaylight.yangtools.yang.ir.IRStatement;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YangSourceRepresentation;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.Extractor;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.ExtractorException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Centralized class for acquiring {@link Extractor}s related to {@link YangSourceRepresentation} defined in this
 * package.
 *
 * @since 14.0.22
 */
@Beta
@NonNullByDefault
public final class SourceInfoExtractors {
    private static final String MODULE = "module";
    private static final String SUBMODULE = "submodule";

    static {
        verify(MODULE.equals(YangStmtMapping.MODULE.getStatementName().getLocalName()));
        verify(SUBMODULE.equals(YangStmtMapping.SUBMODULE.getStatementName().getLocalName()));
    }

    private SourceInfoExtractors() {
        // Hidden on purpose
    }

    /**
     * Returns an {@link Extractor} for specified {@link IRStatement} in declared {@link SourceIdentifier}.
     *
     * @param stmt the root {@link IRStatement}
     * @param sourceId the {@link SourceIdentifier}
     * @return an {@link Extractor}
     * @throws ExtractorException If the root statement is not a valid YANG module/submodule
     */
    public static Extractor forIR(final IRStatement stmt, final SourceIdentifier sourceId) throws ExtractorException {
        final var keyword = stmt.keyword();
        if (!(keyword instanceof IRKeyword.Unqualified)) {
            throw new ExtractorException("Invalid root statement " + keyword, YangIRSource.refOf(sourceId, stmt));
        }

        return switch (keyword.identifier()) {
            case MODULE -> new YangIRSourceInfoExtractor.ForModule(sourceId, stmt);
            case SUBMODULE -> new YangIRSourceInfoExtractor.ForSubmodule(sourceId, stmt);
            default -> throw new ExtractorException("Root of parsed AST must be either module or submodule",
                YangIRSource.refOf(sourceId, stmt));
        };
    }

    @Beta
    public static Extractor forYin(final YinDomSource source,
            final Function<Element, @Nullable StatementSourceReference> refExtractor) throws ExtractorException {
        return forYin(source.getSource(), refExtractor);
    }

    @Beta
    public static Extractor forYin(final DOMSource source,
            final Function<Element, @Nullable StatementSourceReference> refExtractor) throws ExtractorException {
        return forYin(source.getNode(), refExtractor);
    }

    @Beta
    public static Extractor forYin(final Node root,
            final Function<Element, @Nullable StatementSourceReference> refExtractor) throws ExtractorException {
        if (!(root instanceof Element element)) {
            throw new ExtractorException("Root node is not an element");
        }
        return forYin(element, refExtractor);
    }

    @Beta
    public static Extractor forYin(final Element root,
            final Function<Element, @Nullable StatementSourceReference> refExtractor) throws ExtractorException {
        final var element = requireNonNull(root);
        final var elementToRef = requireNonNull(refExtractor);
        if (!YinDomSourceInfoExtractor.isYinElement(element)) {
            throw new ExtractorException("Root element does not have YIN namespace", elementToRef.apply(element));
        }
        return switch (element.getLocalName()) {
            case MODULE -> new YinDomSourceInfoExtractor.ForModule(element, elementToRef);
            case SUBMODULE -> new YinDomSourceInfoExtractor.ForSubmodule(element, elementToRef);
            default -> throw new ExtractorException("Root element needs to be a module or submodule",
                elementToRef.apply(element));
        };
    }
}
