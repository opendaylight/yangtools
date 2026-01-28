/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import com.google.common.annotations.Beta;
import java.io.IOException;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.YangIRSource;
import org.opendaylight.yangtools.yang.parser.antlr.YangTextParser;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;
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
     * @return A new {@link YangStatementStreamSource}
     * @throws IOException When we fail to read the source
     * @throws YangSyntaxErrorException If the source fails basic parsing
     */
    public static YangIRStatementStreamSource create(final YangTextSource source)
            throws IOException, YangSyntaxErrorException {
        final var statement = YangTextParser.parseToIR(source);
        return new YangIRStatementStreamSource(YangIRSource.of(source.sourceId(), statement, source.symbolicName()));
    }
}
