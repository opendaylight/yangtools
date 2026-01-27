/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.source.ir;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.ArrayList;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.model.spi.meta.StatementDeclarations;
import org.opendaylight.yangtools.yang.model.spi.source.SourceSyntaxException;
import org.opendaylight.yangtools.yang.source.ir.antlr.YangStatementLexer;
import org.opendaylight.yangtools.yang.source.ir.antlr.YangStatementParser;
import org.opendaylight.yangtools.yang.source.ir.antlr.YangStatementParser.FileContext;

/**
 * A parser for YANG text files.
 */
final class YangTextParser extends BaseErrorListener {
    private final ArrayList<SourceSyntaxException> exceptions = new ArrayList<>();
    private final @NonNull String fileName;

    private YangTextParser(final String fileName) {
        this.fileName = requireNonNull(fileName);
    }

    static FileContext parseSource(final YangTextSource source) throws SourceSyntaxException {
        final var symbolicName = source.symbolicName();
        return new YangTextParser(symbolicName != null ? symbolicName : source.sourceId().toYangFilename())
            .parseToTree(source);
    }

    private FileContext parseToTree(final YangTextSource source) throws SourceSyntaxException {
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
        } catch (IOException e) {
            throw new SourceSyntaxException("Failed to read source text", e);
        }

        final var it = exceptions.iterator();
        if (!it.hasNext()) {
            return file;
        }

        final var first = it.next();
        if (!it.hasNext()) {
            // Single exception: just throw it
            throw first;
        }

        final var sb = new StringBuilder().append(first.getMessage());
        it.forEachRemaining(next -> sb.append('\n').append(next.getMessage()));
        throw new SourceSyntaxException(sb.toString());
    }

    @Override
    public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line,
            final int charPositionInLine, final String msg, final RecognitionException cause) {
        exceptions.add(new SourceSyntaxException(msg, cause,
            StatementDeclarations.inText(fileName, line, charPositionInLine)));
    }
}
