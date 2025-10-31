/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.antlr;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.ArrayList;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.ir.IRStatement;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.grammar.YangStatementLexer;
import org.opendaylight.yangtools.yang.parser.grammar.YangStatementParser;
import org.opendaylight.yangtools.yang.parser.grammar.YangStatementParser.FileContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A parser for YANG text files.
 */
public final class YangTextParser extends BaseErrorListener {
    private static final Logger LOG = LoggerFactory.getLogger(YangTextParser.class);

    private final ArrayList<YangSyntaxErrorException> exceptions = new ArrayList<>();
    private final @NonNull SourceIdentifier sourceId;

    @NonNullByDefault
    private YangTextParser(final SourceIdentifier sourceId) {
        this.sourceId = requireNonNull(sourceId);
    }

    /**
     * Create an {@link IRStatement} by parsing the content of a {@link YangTextSource}.
     *
     * @param source the {@link YangTextSource}
     * @return an IRStatement of top-level statement
     * @throws IOException if an I/O error occurs
     * @throws YangSyntaxErrorException if the content is not a valid YANG file
     */
    @NonNullByDefault
    public static IRStatement parseToIR(final YangTextSource source) throws IOException, YangSyntaxErrorException {
        return IRSupport.createStatement(new YangTextParser(source.sourceId()).parseToTree(source));
    }

    private FileContext parseToTree(final YangTextSource source) throws IOException, YangSyntaxErrorException {
        final FileContext file;
        try (var reader = source.openStream()) {
            final var lexer = new YangStatementLexer(CharStreams.fromReader(reader));
            // YANG files are potentially huge, make sure we use as small tokens as possible.
            lexer.setTokenFactory(CompactTokenFactory.INSTANCE);
            final var parser = new YangStatementParser(new CommonTokenStream(lexer));

            // disconnect from console and hook ourselves
            lexer.removeErrorListeners();
            lexer.addErrorListener(this);
            parser.removeErrorListeners();
            parser.addErrorListener(this);

            file = parser.file();
        }

        if (exceptions.isEmpty()) {
            return file;
        }

        // Single exception: just throw it
        if (exceptions.size() == 1) {
            throw exceptions.getFirst();
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

    @Override
    public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line,
            final int charPositionInLine, final String msg, final RecognitionException cause) {
        LOG.debug("Syntax error in {} at {}:{}: {}", sourceId, line, charPositionInLine, msg, cause);
        exceptions.add(new YangSyntaxErrorException(sourceId, line, charPositionInLine, msg, cause));
    }
}
