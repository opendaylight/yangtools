/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serial;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.regex.Pattern;
import org.checkerframework.checker.regex.qual.Regex;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Dedicated object identifying a YANG module revision.
 *
 * <h2>API design note</h2>
 * This class defines the contents of a revision statement, but modules do not require to have a revision (e.g. they
 * have not started to keep track of revisions).
 *
 * <p>
 * APIs which involve this class should always transfer instances via {@code Optional<Revision>}, which is
 * the primary bridge data type. Implementations can use nullable fields with explicit conversions to/from
 * {@link Optional}. Both patterns can take advantage of {@link #compare(Optional, Optional)} and
 * {@link #compare(Revision, Revision)} respectively.
 *
 * @author Robert Varga
 */
public final class Revision implements Comparable<Revision>, Immutable, Serializable {
    // Note: since we are using writeReplace() this version is not significant.
    @Serial
    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Regex
    // FIXME: we should improve this to filter incorrect dates -- see constructor.
    private static final String STRING_FORMAT_PATTERN_STR = "\\d\\d\\d\\d\\-\\d\\d-\\d\\d";

    /**
     * String format pattern, which can be used to match parts of a string into components.
     */
    public static final Pattern STRING_FORMAT_PATTERN = Pattern.compile(STRING_FORMAT_PATTERN_STR);

    /**
     * Revision which compares as greater than any other valid revision.
     */
    public static final Revision MAX_VALUE = Revision.of("9999-12-31");

    private final @NonNull String str;

    private Revision(final @NonNull String str) {
        /*
         * According to RFC7950 (https://www.rfc-editor.org/rfc/rfc7950#section-7.1.9):
         *
         *   The "revision" statement specifies the editorial revision history of
         *   the module, including the initial revision.  A series of "revision"
         *   statements detail the changes in the module's definition.  The
         *   argument is a date string in the format "YYYY-MM-DD", [...]
         *
         * Hence we use JDK-provided parsing faculties to parse the date.
         */
        FORMATTER.parse(str);
        this.str = str;
    }

    /**
     * Parse a revision string.
     *
     * @param str String to be parsed
     * @return A Revision instance.
     * @throws DateTimeParseException if the string format does not conform specification.
     * @throws NullPointerException if the string is null
     */
    public static @NonNull Revision of(final @NonNull String str) {
        return new Revision(str);
    }

    /**
     * Parse a (potentially null) revision string. Null strings result result in {@link Optional#empty()}.
     *
     * @param str String to be parsed
     * @return An optional Revision instance.
     * @throws DateTimeParseException if the string format does not conform specification.
     */
    public static @NonNull Optional<Revision> ofNullable(final @Nullable String str) {
        return str == null ? Optional.empty() : Optional.of(new Revision(str));
    }

    /**
     * Compare two {@link Optional}s wrapping Revisions. Arguments and return value are consistent with
     * {@link java.util.Comparator#compare(Object, Object)} interface contract. Missing revisions compare as lower
     * than any other revision.
     *
     * @param first First optional revision
     * @param second Second optional revision
     * @return Positive, zero, or negative integer.
     */
    public static int compare(final @NonNull Optional<Revision> first, final @NonNull Optional<Revision> second) {
        if (first.isPresent()) {
            return second.isPresent() ? first.orElseThrow().compareTo(second.orElseThrow()) : 1;
        }
        return second.isPresent() ? -1 : 0;
    }

    /**
     * Compare two explicitly nullable Revisions. Unlike {@link #compareTo(Revision)}, this handles both arguments
     * being null such that total ordering is defined.
     *
     * @param first First revision
     * @param second Second revision
     * @return Positive, zero, or negative integer.
     */
    public static int compare(final @Nullable Revision first, final @Nullable Revision second) {
        if (first != null) {
            return second != null ? first.compareTo(second) : 1;
        }
        return second != null ? -1 : 0;
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public int compareTo(final Revision o) {
        // Since all strings conform to the format, we can use their comparable property to do the correct thing
        // with respect to temporal ordering.
        return str.compareTo(o.str);
    }

    @Override
    public int hashCode() {
        return str.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof Revision other && str.equals(other.str);
    }

    /**
     * Return the revision string according to
     * <a href="https://www.rfc-editor.org/rfc/rfc7950.html#section-7.1.9">RFC7950, section 7.1.9</a>. The string is
     * guaranteed to be composed on 10 ASCII characters in {@code YYYY-MM-DD} format. The items are guaranteed to be
     * decimal numbers. There are no further guarantees as to convertibility to an actual calendar date, as this method
     * can validly return {@code "2019-02-29"} (even if 2019 is not a leap year) or {@code "2022-12-32"} (even if no
     * month has 32 days).
     *
     * @return A string in {@code YYYY-MM-DD} format
     */
    @Override
    public String toString() {
        return str;
    }

    @Serial
    Object writeReplace() {
        return new Proxy(str);
    }

    private static final class Proxy implements Externalizable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String str;

        @SuppressWarnings("checkstyle:redundantModifier")
        public Proxy() {
            // For Externalizable
        }

        Proxy(final String str) {
            this.str = requireNonNull(str);
        }

        @Override
        public void writeExternal(final ObjectOutput out) throws IOException {
            out.writeObject(str);
        }

        @Override
        public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
            str = (String) in.readObject();
        }

        @Serial
        private Object readResolve() {
            return Revision.of(requireNonNull(str));
        }
    }
}
