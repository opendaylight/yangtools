/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.source;

import static com.google.common.base.Verify.verify;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.ir.IRKeyword;
import org.opendaylight.yangtools.yang.ir.IRStatement;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YangSourceRepresentation;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.Extractor;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.ExtractorException;

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
            throw new ExtractorException(YangIRSource.refOf(sourceId, stmt), "Invalid root statement " + keyword);
        }

        return switch (keyword.identifier()) {
            case MODULE -> new YangIRSourceInfoExtractor.ForModule(sourceId, stmt);
            case SUBMODULE -> new YangIRSourceInfoExtractor.ForSubmodule(sourceId, stmt);
            default -> throw new ExtractorException(YangIRSource.refOf(sourceId, stmt),
                "Root of parsed AST must be either module or submodule");
        };
    }
}
