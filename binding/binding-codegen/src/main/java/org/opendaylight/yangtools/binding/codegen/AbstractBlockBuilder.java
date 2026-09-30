/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.base.VerifyException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Reference {@link Builder} implementation.
 *
 * @since 16.1.1
 */
abstract sealed class AbstractBlockBuilder<T extends AbstractBlockBuilder<T>> extends Block.Builder
        permits BlockBuilder {
    // The idea is that we start with an empty StringBuilder and as we receive events we decide what to do next.
    // Typically this will be just a simple append, but we also need to track indentation.
    //
    // Overall, the core state should look something like:
    //
    //    // indent + block content
    //    sealed interface Blk {
    //
    //        int indent();
    //    }
    //
    //    List<Blk> blocks; // completed blocks, with optional coalescence when indent matches

    // current block, containing newline-separated lines
    private final @NonNull StringBuilder buf = new StringBuilder();
    // offset of the start of the current line, i.e. one past the last known newline in current block
    private int currentLine = 0;
    // offset of the start of the second line, i.e. the one past the first newline in current block
    private int secondLine = -1;

    // current indentation we are using
    private int currentIndent = 0;
    // the indentation that is currently missing
    private int needIndent = 0;

    AbstractBlockBuilder() {
        // nothing else
    }

    abstract @NonNull T self();

    @Override
    public final void appendTo(final Block.Builder bb) {
        final var length = buf.length();
        if (length == 0) {
            return;
        }
        if (secondLine == -1) {
            bb.str(buf.toString());
            return;
        }
        bb.txt(buf.substring(0, currentLine));
        if (currentLine != length) {
            bb.str(buf.substring(currentLine));
        }
    }

    @Override
    final T blk(final Block blk) {
        verifyEmptyLine();
        if (blk != null) {
            blk.appendTo(this);
        }
        return self();
    }

    @Override
    final T nl() {
        newLine();
        return self();
    }

    @Override
    void newLine() {
        markNl(buf.append('\n'));
    }

    final void markNl(final StringBuilder sb) {
        final var nextLine = sb.length();
        if (secondLine == -1) {
            secondLine = nextLine;
        }
        currentLine = nextLine;
        needIndent = currentIndent;
    }

    @Override
    final T gt() {
        buf().append('>');
        return self();
    }

    @Override
    final T lt() {
        buf().append('<');
        return self();
    }

    @Override
    final T cs() {
        buf().append(", ");
        return self();
    }

    @Override
    final T str(final String str) {
        strImpl(str);
        return self();
    }

    @NonNullByDefault
    private void strImpl(final String str) {
        buf().append(verifyStr(str));
    }

    @Override
    final T txt(final String text) {
        verifyEmptyLine();
        final var verified = verifyTxt(text);
        return currentIndent == 0 ? txtFast(verified) : txtSlow(verified);
    }

    @NonNullByDefault
    private T txtFast(final String text) {
        if (secondLine == -1) {
            secondLine = buf.length() + text.indexOf('\n') + 1;
        }
        buf.append(text);
        currentLine = buf.length();
        return self();
    }

    @NonNullByDefault
    private T txtSlow(final String text) {
        new BlockN(text.substring(0, text.length() - 1)).appendTo(this);
        return self();
    }

    @Override
    final T eol(final String content) {
        return str(content).nl();
    }

    @Override
    final T eol(final String str, final int beginIndex, final int endIndex) {
        return eol(str.substring(beginIndex, endIndex));
    }

    @Override
    final Block build() {
        final var length = buf.length();
        if (length == 0) {
            throw new VerifyException("empty block");
        }
        return build(length);
    }

    @NonNullByDefault
    private Block build(final int length) {
        if (length != currentLine) {
            throw new VerifyException("unterminated line " + buf.substring(currentLine));
        }
        if (currentIndent != 0) {
            throw new VerifyException("leftover indentation depth " + currentIndent);
        }

        if (length == secondLine) {
            // "\n" or "foo\n"
            return length == 1 ? Block1.EMPTY : new Block1(buf.substring(0, length - 1));
        }
        final var end = length - 1;
        // "\n\n"
        return end == secondLine ? Block2.EMPTY
            // everything else
            : new BlockN(buf.substring(0, end));
    }

    @Override
    final Block toBlock() {
        final var length = buf.length();
        return length == 0 ? null : build(length);
    }

    @Override
    public final String toRawString() {
        return verifyNotNull(buf.toString());
    }


    // Prepare the buffer to receive some content
    @NonNullByDefault
    final StringBuilder buf() {
        return needIndent == 0 ? buf : applyIndent();
    }

    @NonNullByDefault
    final StringBuilder applyIndent() {
        needIndent = 0;
        return buf.repeat("    ", currentIndent);
    }

    @NonNullByDefault
    final StringBuilder incrementIndent(final StringBuilder sb) {
        if (++currentIndent < 1) {
            // FIXME: split out to verifier
            throw new VerifyException("indent overflow");
        }
        return sb;
    }

    @NonNullByDefault
    final StringBuilder decrementIndent() {
        if (currentIndent-- == 0) {
            // FIXME: split out to verifier
            throw new VerifyException("indent underflow");
        }
        return buf();
    }

    final void verifyEmptyLine() {
        if (currentLine != buf.length()) {
            throw new VerifyException("trailing content '" + buf.substring(currentLine) + "'");
        }
    }
}
