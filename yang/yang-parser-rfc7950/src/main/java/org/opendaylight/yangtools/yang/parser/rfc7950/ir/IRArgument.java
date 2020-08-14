/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.ir;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * An argument to a YANG statement, as defined by section 6.1.3 of both
 * <a href="https://tools.ietf.org/html/rfc6020#section-6.1.3">RFC6020</a> and
 * <a href="https://tools.ietf.org/html/rfc7950#section-6.1.3">RFC7950</a>. An argument is effectively any old string,
 * except it can be defined in a number of ways:
 * <ul>
 *   <li>it can be a simple unquoted string, or</li>
 *   <li>it can be a single-quoted string, with its contents being completely preserved, or</li>
 *   <li>it can be a double-quoted string, which defines some escaping and whitespace-stripping rules, or</li>
 *   <li>it can be a concatenation of any number of single- or double-quoted strings</li>
 * </ul>
 *
 * <p>
 * The first three cases as covered by {@link Single} subclass, which exposes appropriate methods to infer how its
 * string literal is to be interpreted. The last case is handled by {@link Concatenation} subclass, which exposes
 * the constituent parts as {@link Single} items.
 *
 * <p>
 * Please note that parser implementations producing these argument representations are <b>NOT</b> required to retain
 * the format of the original definition. They are free to perform quoting and concatenation transformations as long as
 * they maintain semantic equivalence. As a matter of example, these transformations are explicitly allowed:
 * <ul>
 *   <li>elimination of unneeded quotes, for example turning {@code "foo"} into {@code foo}</li>
 *   <li>transformation of quotes, for example turning {@code "foo\nbar"} into {@code 'foo&#10bar'}</li>
 *   <li>concatenation processing, for example turning {@code 'foo' + 'bar'} into {@code foobar}</li>
 * </ul>
 */
@Beta
public abstract class IRArgument implements Immutable {
    /**
     * An argument composed of multiple concatenated parts.
     */
    public static final class Concatenation extends IRArgument {
        private final @NonNull ImmutableList<Single> parts;

        Concatenation(final ImmutableList<Single> parts) {
            this.parts = requireNonNull(parts);
        }

        /**
         * Return the argument parts that need to be concatenated.
         *
         * @return Argument parts.
         */
        public @NonNull List<? extends Single> parts() {
            return parts;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            final Iterator<Single> it = parts.iterator();
            sb.append(it.next());
            while (it.hasNext()) {
                sb.append(" + ").append(it.next());
            }
            return sb.toString();
        }
    }

    /**
     * An argument composed of a single string. This string may need further validation and processing, as it may not
     * actually conform to the specification as requested by {@code yang-version}.
     */
    /*
     * This is the public footprint which is served by three final subclasses: DoublyQuoted, SingleQuoted, Unquoted.
     * Those classes must never be exposed, as they are a manifestation of current implementation in StatementFactory.
     * As noted in the interface contract of IRArgument, we have very much free reign on syntactic transformations,
     * StatementFactory is just not taking advantage of those at this point.
     *
     * The subclasses may very much change, in terms of both naming and function, to support whatever StatementFactory
     * ends up doing.
     */
    public abstract static class Single extends IRArgument {
        private final @NonNull String string;

        Single(final String string) {
            this.string = requireNonNull(string);
        }

        /**
         * Significant portion of this argument. For unquoted and single-quoted strings this is the unquoted string
         * literal. For double-quoted strings this is the unquoted string, after whitespace trimming as defined by
         * RFC6020/RFC7950 section 6.1.3, but before escape substitution.
         *
         * @return Significant portion of this argument.
         */
        public final @NonNull String string() {
            return string;
        }

        /**
         * Check if this argument needs further unescape operation (which is version-specific) to arrive at the literal
         * string value. This is false for unquoted and single-quoted strings, which do not support any sort of
         * escaping. This may be true for double-quoted strings, as they <b>may</b> need to be further processed in
         * version-dependent ways to arrive at the correct literal value.
         *
         * <p>
         * This method is allowed to err on the false-positive side -- i.e. it may report any double-quoted string
         * as needing further processing, even when the actual content could be determined to not need further
         * processing.
         *
         * @return False if the value of {@link #string} can be used as-is.
         */
        public final boolean needUnescape() {
            return this instanceof DoubleQuoted;
        }

        /**
         * Check if this argument needs an additional content check for compliance. This is false if the string was
         * explicitly quoted and therefore cannot contain stray single- or double-quotes, or if the content has already
         * been checked to not contain them.
         *
         * <p>
         * The content check is needed to ascertain RFC7950 compliance, because RFC6020 allows constructs like
         * <pre>abc"def</pre> in unquoted strings, while RFC7950 explicitly forbids them.
         *
         * <p>
         * This method is allowed to err on the false-positive side -- i.e. it may report any unquoted string as
         * needing this check, even when the actual content could be determined to not contain quotes.
         *
         * @return True if this argument requires a version-specific check for quote content.
         */
        public final boolean needQuoteCheck() {
            return this instanceof Unquoted;
        }
    }

    static final class DoubleQuoted extends Single {
        DoubleQuoted(final String string) {
            super(string);
        }

        @Override
        public String toString() {
            // Note this is just an approximation. We do not have enough state knowledge to restore any whitespace we
            // may have trimmed.
            return '"' + string() + '"';
        }
    }

    static final class SingleQuoted extends Single {
        SingleQuoted(final String string) {
            super(string);
        }

        @Override
        public String toString() {
            return "'" + string() + "'";
        }
    }

    static final class Unquoted extends Single {
        Unquoted(final String string) {
            super(string);
        }

        @Override
        public String toString() {
            return string();
        }
    }

    IRArgument() {
        // Hidden on purpose
    }

    @Override
    public abstract String toString();
}
