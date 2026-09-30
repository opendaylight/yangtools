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
import com.google.errorprone.annotations.CheckReturnValue;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Abstract base class for {@linkplain Block.Builder} implementations.
 */
@NonNullByDefault
abstract sealed class AbstractBlockBuilder<B extends AbstractBlockBuilder<B, F>, F extends Block.Fragment<B, F>>
        implements Block.Builder
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
    private final StringBuilder buf = new StringBuilder();
    // offset of the start of the current line, i.e. one past the last known newline in current block
    private int currentLine = 0;
    // offset of the start of the second line, i.e. the one past the first newline in current block
    private int secondLine = -1;

    // current indentation we are using
    private int currentIndent = 0;
    // the indentation that is currently missing
    private int needIndent = 0;

    /**
     * Default constructor.
     */
    AbstractBlockBuilder() {
        // nothing else
    }

    /**
     * {@return {@code this}}
     */
    protected abstract @NonNull B self();

    @Override
    public final B blk(final @Nullable Block blk) {
        verifyEmptyLine();
        if (blk != null) {
            blk.appendTo(this);
        }
        return self();
    }

    /**
     * Append the contents of a {@link Block.Fragment} to this instance if it is not {@code null}.
     *
     * @param fragment optional {@link Block.Fragment}
     * @return this instance
     */
    public final B frg(final @Nullable F fragment) {
        final var self = self();
        if (fragment != null) {
            fragment.appendTo(self);
        }
        return self;
    }

    @Override
    public final B nl() {
        newLine();
        return self();
    }

    @Override
    public final void newLine() {
        markNl(buf.append('\n'));
    }

    protected final void markNl(final StringBuilder sb) {
        final var nextLine = sb.length();
        if (secondLine == -1) {
            secondLine = nextLine;
        }
        currentLine = nextLine;
        needIndent = currentIndent;
    }

    @Override
    public final B gt() {
        buf().append('>');
        return self();
    }

    @Override
    public final B lt() {
        buf().append('<');
        return self();
    }

    @Override
    public final B cs() {
        buf().append(", ");
        return self();
    }

    @Override
    public final B sp() {
        buf().append(' ');
        return self();
    }

    @Override
    public final B str(final String str) {
        strImpl(str);
        return self();
    }

    private void strImpl(final String str) {
        buf().append(verifyStr(str));
    }

    @Override
    public final B txt(final String text) {
        verifyEmptyLine();
        final var verified = verifyTxt(text);
        return currentIndent == 0 ? txtFast(verified) : txtSlow(verified);
    }

    private B txtFast(final String text) {
        if (secondLine == -1) {
            secondLine = buf.length() + text.indexOf('\n') + 1;
        }
        buf.append(text);
        currentLine = buf.length();
        return self();
    }

    private B txtSlow(final String text) {
        new BlockN(text.substring(0, text.length() - 1)).appendTo(this);
        return self();
    }

    @Override
    public final B eol(final String content) {
        return str(content).nl();
    }

    @Override
    public final B eol(final String str, final int beginIndex, final int endIndex) {
        return eol(str.substring(beginIndex, endIndex));
    }

    /**
     * {@return {@code true} if the current buffer is empty or contains only indentation}
     */
    protected final boolean isEmpty() {
        return buf.isEmpty();
    }

    /**
     * {@return buffer prepared to received some content}
     */
    protected final StringBuilder buf() {
        return needIndent == 0 ? buf : applyIndent();
    }

    private StringBuilder applyIndent() {
        needIndent = 0;
        return buf.repeat("    ", currentIndent);
    }

    protected final StringBuilder incrementIndent(final StringBuilder sb) {
        if (++currentIndent < 1) {
            // FIXME: split out to verifier
            throw new VerifyException("indent overflow");
        }
        return sb;
    }

    protected final StringBuilder decrementIndent() {
        if (currentIndent-- == 0) {
            // FIXME: split out to verifier
            throw new VerifyException("indent underflow");
        }
        return buf();
    }

    @Override
    public final Block build() {
        final var length = buf.length();
        if (length == 0) {
            throw new VerifyException("empty block");
        }
        return build(length);
    }

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
    public final @Nullable Block toBlock() {
        final var length = buf.length();
        return length == 0 ? null : build(length);
    }

    @Override
    public final String toRawString() {
        return verifyNotNull(buf.toString());
    }

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return super.equals(obj);
    }

    @Override
    @Deprecated(forRemoval = true)
    @SuppressWarnings("removal")
    public final String toString() {
        return toRawString();
    }

    protected final void verifyEmptyLine() {
        if (currentLine != buf.length()) {
            throw new VerifyException("trailing content '" + buf.substring(currentLine) + "'");
        }
    }

    //
    // Bridge methods to ArgumentVerifier. Kept here to keep callers as simple as possible.
    //
    @CheckReturnValue
    protected static final String verifyStr(final String arg) {
        return ArgumentVerifier.INSTANCE.verifyStr(arg);
    }

    @CheckReturnValue
    protected static final String verifyTxt(final String arg) {
        return ArgumentVerifier.INSTANCE.verifyTxt(arg);
    }
}
