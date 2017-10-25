/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc6020.repo;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class YangErrorListener extends BaseErrorListener {
    private static final Logger LOG = LoggerFactory.getLogger(YangErrorListener.class);

    private final List<YangSyntaxErrorException> exceptions = new ArrayList<>();
    private final SourceIdentifier source;

    public YangErrorListener(final SourceIdentifier source) {
        this.source = requireNonNull(source);
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line,
            final int charPositionInLine, final String msg, final RecognitionException e) {
        LOG.debug("Syntax error in {} at {}:{}: {}", source, line, charPositionInLine, msg, e);
        exceptions.add(new YangSyntaxErrorException(source, line, charPositionInLine, msg, e));
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    private static String getModuleName(final Recognizer<?, ?> recognizer) {
        if (!(recognizer instanceof Parser)) {
            return null;
        }

        final Parser parser = (Parser) recognizer;
        try {
            String model = parser.getInputStream().getTokenSource().getInputStream().toString();
            model = model.substring(0, model.indexOf('\n'));
            model = model.substring(model.indexOf("module") + 6);
            model = model.substring(0, model.indexOf('{'));
            model = model.trim();
            return model;
        } catch (Exception e) {
            LOG.debug("Failed to extract module name from parser {}", parser, e);
            return null;
        }
    }

    public void validate() throws YangSyntaxErrorException {
        if (exceptions.isEmpty()) {
            return;
        }

        // Single exception: just throw it
        if (exceptions.size() == 1) {
            throw exceptions.get(0);
        }

        final StringBuilder sb = new StringBuilder();
        SourceIdentifier source = null;
        boolean first = true;
        for (YangSyntaxErrorException e : exceptions) {
            if (source == null) {
                source = e.getSource().orElse(null);
            }
            if (first) {
                first = false;
            } else {
                sb.append('\n');
            }

            sb.append(e.getFormattedMessage());
        }

        throw new YangSyntaxErrorException(source, 0, 0, sb.toString());
    }
}
