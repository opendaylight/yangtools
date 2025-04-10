/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import java.io.DataInput;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.regex.Pattern;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Dedicated object identifying a YANG module revision.
 *
 * <h2>API design note</h2>
 * This class defines the contents of a revision statement, but modules do not require to have a revision (e.g. they
 * have not started to keep track of revisions).
 *
 * <p>APIs which involve this class should always transfer instances via {@code Optional<Revision>}, which is
 * the primary bridge data type. Implementations can use nullable fields with explicit conversions to/from
 * {@link Optional}. Both patterns can take advantage of {@link #compare(Optional, Optional)} and
 * {@link #compare(Revision, Revision)} respectively.
 */
public final class Revision implements RevisionUnion {
    // Note: since we are using writeReplace() this version is not significant.
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * String format pattern, which can be used to match parts of a string into components.
     */
    // FIXME: we should improve this to filter incorrect dates -- see constructor.
    public static final Pattern STRING_FORMAT_PATTERN = Pattern.compile("\\d\\d\\d\\d\\-\\d\\d-\\d\\d");

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

    @Override
    public Revision revision() {
        return this;
    }

    @Override
    public String unionString() {
        return str;
    }

    public static @NonNull Revision readFrom(final DataInput in) throws IOException {
        return ofRead(in.readUTF());
    }

    static @NonNull Revision ofRead(final @NonNull String str) throws IOException {
        try {
            return of(str);
        } catch (DateTimeParseException e) {
            throw new IOException("Invalid revision-date string", e);
        }
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

    @java.io.Serial
    Object writeReplace() {
        return new RUv1(str);
    }

    @java.io.Serial
    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        throwNSE();
    }

    @java.io.Serial
    private void readObjectNoData() throws ObjectStreamException {
        throwNSE();
    }

    @java.io.Serial
    private void writeObject(final ObjectOutputStream stream) throws IOException {
        throwNSE();
    }

    static void throwNSE() throws NotSerializableException {
        throw new NotSerializableException(Revision.class.getName());
    }
}
