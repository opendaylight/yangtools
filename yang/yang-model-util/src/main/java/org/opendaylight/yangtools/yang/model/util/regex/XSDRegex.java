/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.regex;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.util.regex.regexParser.AtomContext;
import org.opendaylight.yangtools.yang.model.util.regex.regexParser.BranchContext;
import org.opendaylight.yangtools.yang.model.util.regex.regexParser.PieceContext;
import org.opendaylight.yangtools.yang.model.util.regex.regexParser.QuantifierContext;
import org.opendaylight.yangtools.yang.model.util.regex.regexParser.RegExpContext;

/**
 * XSD regular expression.
 */
@Beta
public final class XSDRegex {
    private final RegExpContext exp;

    private XSDRegex(final RegExpContext exp) {
        this.exp = requireNonNull(exp);
    }

    public static XSDRegex parse(final String regex) {
        final regexParser parser = new regexParser(new CommonTokenStream(new regexLexer(
            CharStreams.fromString(regex))));
        parser.removeErrorListeners();

        final List<IllegalArgumentException> errors = new ArrayList<>();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(final @Nullable Recognizer<?, ?> recognizer, final @Nullable Object offendingSymbol,
                    final int line, final int charPositionInLine, final @Nullable String msg,
                    final @Nullable RecognitionException cause) {
                final IllegalArgumentException ex = new IllegalArgumentException(msg);
                ex.initCause(cause);
                if (errors.isEmpty()) {
                    errors.add(ex);
                } else {
                    errors.get(0).addSuppressed(ex);
                }
            }
        });

        final RegExpContext exp = parser.regExp();
        if (!errors.isEmpty()) {
            throw errors.get(0);
        }

        return new XSDRegex(exp);
    }

    public Pattern toJavaPattern() {
        final StringBuilder sb = new StringBuilder().append('^');

        final Iterator<BranchContext> it = exp.branch().iterator();
        addBranch(sb, it.next());
        while (it.hasNext()) {
            sb.append('|');
            addBranch(sb, it.next());
        }

        final String regex = sb.append('$').toString();
        return Pattern.compile(regex);
    }

    private void addBranch(final StringBuilder sb, final BranchContext branch) {
        for (PieceContext piece : branch.piece()) {
            final AtomContext atom = piece.atom();

            final QuantifierContext quantifier = piece.quantifier();
            if (quantifier != null) {
                // FIXME: emit quantifier
            }
        }
    }
}
