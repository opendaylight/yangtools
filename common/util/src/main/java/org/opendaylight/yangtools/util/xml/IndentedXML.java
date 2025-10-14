/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util.xml;

import com.google.common.base.MoreObjects;
import java.util.Arrays;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Utility class for creating neatly indented XML documents. This class should only be used when the produced
 * document can only contain two types of elements:
 * <ol>
 *   <li>containment elements, which contain only nested markup or insignificant whitespace including newlines</li>
 *   <li>content elements, which do not contain markup, where text content is significant</li>
 * </ol>
 *
 * <p>This class is {@link Immutable} and therefore safe to use from multiple threads.
 */
@NonNullByDefault
public final class IndentedXML implements Immutable {
    /**
     * The string used to delineate output.
     */
    static final String NEWLINE = "\n";

    // Run-time constants
    private static final char[] NEWLINE_CHARS = NEWLINE.toCharArray();
    private static final int NEWLINE_SIZE = NEWLINE_CHARS.length;

    /**
     * The default indentation count. Separated out for documentation purposes.
     */
    private static final int DEFAULT_INDENT_SIZE = 2;
    /**
     * The instance performing {@value #DEFAULT_INDENT_SIZE} indentation.
     */
    private static final IndentedXML DEFAULT_INDENT = new IndentedXML(DEFAULT_INDENT_SIZE);

    /**
     * The depth of indentation available in {@link #indentChars}.
     */
    private static final int CHARS_DEPTH = 16;

    /**
     * Maximum XML element nesting depth we support. This provides a safeguard against a potential DoS by incurred by
     * excessive memory allocation leading to a {@link OutOfMemoryError}. This limit might seem low when compared
     * to what the JVM limits are around our implementation, but we expected to have many concurrent users, hence we
     * size here defensively.
     *
     * <p>We currently cap the depth to 1 million nested elements, which should incur less than a MiB of memory per
     * active instance.
     *
     * <p>This limit is expected to be between 1 and 2147483647, so that doubling it is guaranteed
     * to result in a positive {@code int} value without needing to check for overflow.
     */
    // TODO: allow control with a property?
    static final int MAX_NESTED_ELEMENTS = 1_000_000;

    static {
        // This assertion should compile down (almost) nothing
        final long doubleMaxNestedElements = MAX_NESTED_ELEMENTS << 1;
        if (doubleMaxNestedElements > Integer.MAX_VALUE) {
            throw new ExceptionInInitializerError("MAX_NESTED_ELEMENTS doubles to " + doubleMaxNestedElements);
        }
    }

    /**
     * The number of spaces to indent by. Guaranteed to be more than 0.
     */
    private final int indentSize;
    /**
     * Pre-computed indentation characters containing {@link #NEWLINE} followed by {@link #CHARS_DEPTH} levels of
     * {@link #indentSize} spaces.
     */
    private final char[] indentChars;

    private IndentedXML(final int indentSize) {
        if (indentSize < 1) {
            throw new IllegalArgumentException("indentSize has to be positive");
        }
        this.indentSize = indentSize;

        indentChars = new char[NEWLINE_SIZE + indentSize * CHARS_DEPTH];
        System.arraycopy(NEWLINE_CHARS, 0, indentChars, 0, NEWLINE_SIZE);
        Arrays.fill(indentChars, NEWLINE_SIZE, indentChars.length, ' ');
    }

    /**
     * Return an instance with default indent, which is {@value #DEFAULT_INDENT_SIZE} spaces, using {@value #NEWLINE}
     * as line separator.
     *
     * @return An {@link IndentedXML} instance
     */
    public static IndentedXML of() {
        return DEFAULT_INDENT;
    }

    /**
     * Return an instance with specified number of spaces as indentation, using {@value #NEWLINE} as line separator.
     *
     * @param indentSize the number of spaces to use for indentation
     * @return An {@link IndentedXML} instance
     * @throws IllegalArgumentException if {@code indentSize} is less than {@code 1}
     */
    public static IndentedXML of(final int indentSize) {
        return switch (indentSize) {
            case DEFAULT_INDENT_SIZE -> DEFAULT_INDENT;
            default -> new IndentedXML(indentSize);
        };
    }

    /**
     * Wrap a {@link XMLStreamWriter} to produce intended XML. Returned {@link XMLStreamWriter} will not support writing
     * more than {@value #MAX_NESTED_ELEMENTS} nested elements.
     *
     * @param delegate the delegate
     * @return an indenting {@link XMLStreamWriter}
     */
    public XMLStreamWriter wrapStreamWriter(final XMLStreamWriter delegate) {
        return new IndentingStreamWriter(this, delegate);
    }

    void writeIndent(final XMLStreamWriter out, final int count) throws XMLStreamException {
        // first write includes the newline
        final int first = Math.min(count, CHARS_DEPTH);
        out.writeCharacters(indentChars, 0, NEWLINE_SIZE + first * indentSize);

        final int remaining = count - first;
        if (remaining > 0) {
            writeIndentTail(out, remaining);
        }
    }

    // split out to aid inlining
    private void writeIndentTail(final XMLStreamWriter out, final int count) throws XMLStreamException {
        int remaining = count;
        do {
            // subsequent writes are only indentation
            final int next = Math.min(remaining, CHARS_DEPTH);
            out.writeCharacters(indentChars, NEWLINE_SIZE, next * indentSize);
            remaining -= next;
        } while (remaining > 0);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("indentSize", indentSize).toString();
    }
}
