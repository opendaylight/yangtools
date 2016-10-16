/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.xsd.regex;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.util.BitSet;
import java.util.regex.Pattern;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.opendaylight.yangtools.xsd.regex.impl.XSDRegExpLexer;
import org.opendaylight.yangtools.xsd.regex.impl.XSDRegExpParser;
import org.opendaylight.yangtools.xsd.regex.impl.XSDRegExpParser.RegExpContext;

/**
 * A XSD regular expression.
 *
 * @author Robert Varga
 */
@Beta
public final class RegularExpression {
    private static final ANTLRErrorListener ERROR_LISTENER = new ANTLRErrorListener() {
        @Override
        public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line,
                final int charPositionInLine, final String msg, final RecognitionException e) {
            throw new IllegalArgumentException(String.format("%s [at %s]", msg, charPositionInLine), e);
        }

        @Override
        public void reportContextSensitivity(final Parser recognizer, final DFA dfa, final int startIndex,
                final int stopIndex, final int prediction, final ATNConfigSet configs) {
        }

        @Override
        public void reportAttemptingFullContext(final Parser recognizer, final DFA dfa, final int startIndex,
                final int stopIndex, final BitSet conflictingAlts, final ATNConfigSet configs) {
        }

        @Override
        public void reportAmbiguity(final Parser recognizer, final DFA dfa, final int startIndex,
                final int stopIndex, final boolean exact, final BitSet ambigAlts, final ATNConfigSet configs) {
        }
    };

    private final RegExpContext context;

    private RegularExpression(final RegExpContext regExp) {
        this.context = Preconditions.checkNotNull(regExp);
    }

    /**
     * Construct a new regular expression from a string.
     *
     * @param regex Regular expression to parse
     * @return A RegularExpression object
     * @throws IllegalArgumentException when the supplied string is not valid
     */
    public static RegularExpression parse(final String regex) {
        final XSDRegExpLexer lexer = new XSDRegExpLexer(new ANTLRInputStream(regex));
        lexer.removeErrorListeners();
        lexer.addErrorListener(ERROR_LISTENER);

        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final XSDRegExpParser parser = new XSDRegExpParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(ERROR_LISTENER);

        return new RegularExpression(parser.regExp());
    }

    public Pattern toPattern() {
        return Pattern.compile(context.accept(new ToPatternVisitor()).toString());
    }

    @Override
    public String toString() {
        return context.accept(new ToStringVisitor()).toString();
    }
}
