/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import java.io.IOException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.ir.IRStatement;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.ExtractorException;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.ExtractorMalformedArgumentException;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfoExtractors;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.parser.antlr.YangTextParser;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * Utility class for extract {@link SourceInfo} from a {@link YangIRSource}.
 */
public final class YangIRSourceInfoExtractor {
    @NonNullByDefault
    private YangIRSourceInfoExtractor(final SourceIdentifier sourceId, final IRStatement root) {
        // Hidden on purpose
    }

    /**
     * Extracts {@link SourceInfo} from an intermediate representation root statement of a YANG model.
     *
     * @param source Schema source
     * @return {@link SourceInfo}
     * @throws IllegalArgumentException If the root statement is not a valid YANG module/submodule
     */
    public static @NonNull SourceInfo forIR(final YangIRSource source) {
        return forIR(source.statement(), source.sourceId());
    }

    /**
     * Extracts {@link SourceInfo} from an intermediate representation root statement of a YANG model.
     *
     * @param sourceId Source identifier, perhaps guessed from input name
     * @param rootStatement root statement
     * @return {@link SourceInfo}
     * @throws IllegalArgumentException If the root statement is not a valid YANG module/submodule
     */
    public static @NonNull SourceInfo forIR(final IRStatement rootStatement, final SourceIdentifier sourceId) {
        try {
            return SourceInfoExtractors.forIR(rootStatement, sourceId).extractSourceInfo();
        } catch (ExtractorMalformedArgumentException e) {
            throw new SourceException(e);
        } catch (ExtractorException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    /**
     * Extracts {@link SourceInfo} from a {@link YangTextSource}. This parsing does not validate full YANG module, only
     * parses header up to the revisions and imports.
     *
     * @param yangText {@link YangTextSource}
     * @return {@link SourceInfo}
     * @throws IOException When the resource cannot be read
     * @throws YangSyntaxErrorException If the resource does not pass syntactic analysis
     */
    public static SourceInfo forYangText(final YangTextSource yangText) throws IOException, YangSyntaxErrorException {
        final var sourceId = yangText.sourceId();
        return forIR(YangTextParser.parseToIR(yangText), sourceId);
    }
}
