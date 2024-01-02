/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.repo;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class YangErrorListener extends BaseErrorListener {
    private static final Logger LOG = LoggerFactory.getLogger(YangErrorListener.class);

    private final List<YangSyntaxErrorException> exceptions = new ArrayList<>();
    private final SourceIdentifier sourceId;

    YangErrorListener(final SourceIdentifier sourceId) {
        this.sourceId = requireNonNull(sourceId);
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line,
            final int charPositionInLine, final String msg, final RecognitionException e) {
        LOG.debug("Syntax error in {} at {}:{}: {}", sourceId, line, charPositionInLine, msg, e);
        exceptions.add(new YangSyntaxErrorException(sourceId, line, charPositionInLine, msg, e));
    }

    void validate() throws YangSyntaxErrorException {
        if (exceptions.isEmpty()) {
            return;
        }

        // Single exception: just throw it
        if (exceptions.size() == 1) {
            throw exceptions.get(0);
        }

        final var sb = new StringBuilder();
        boolean first = true;
        for (var ex : exceptions) {
            if (first) {
                first = false;
            } else {
                sb.append('\n');
            }

            sb.append(ex.getFormattedMessage());
        }

        throw new YangSyntaxErrorException(sourceId, 0, 0, sb.toString());
    }
}
