/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo.ExtractorException;
import org.opendaylight.yangtools.yang.model.spi.source.SourceSyntaxException;
import org.opendaylight.yangtools.yang.model.spi.source.YangTextToIRSourceTransformer;
import org.opendaylight.yangtools.yang.parser.spi.source.YangIRStatementStreamSource;

/**
 * This class represents implementation of StatementStreamSource in order to emit YANG statements using supplied
 * StatementWriter.
 */
@Beta
public final class YangStatementStreamSource {
    private YangStatementStreamSource() {
        // Hidden on purpose
    }

    /**
     * Create a {@link YangStatementStreamSource} for a {@link YangTextSource}.
     *
     * @param source YangTextSchemaSource, must not be null
     * @return A new {@link YangIRStatementStreamSource}
     * @throws ExtractorException When we fail to extract source dependency information
     * @throws SourceSyntaxException If the source fails basic parsing
     */
    @NonNullByDefault
    public static YangIRStatementStreamSource create(final YangTextToIRSourceTransformer sourceTransformer,
                final YangTextSource source) throws ExtractorException, SourceSyntaxException {
        return new YangIRStatementStreamSource(sourceTransformer.transformSource(source));
    }
}
