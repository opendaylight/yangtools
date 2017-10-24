/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Preconditions;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.RegEx;

/**
 * Dedicated object identifying a YANG module revision.
 *
 * <p>
 * <h3>API design note</h3>
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
public final class Revision implements Comparable<Revision>, Serializable {
    private static final long serialVersionUID = 1L;

    @RegEx
    private static final String STRING_FORMAT_STR = "\\d\\d\\d\\d\\-\\d\\d-\\d\\d";
    private static final Pattern STRING_FORMAT = Pattern.compile(STRING_FORMAT_STR);

    private final String str;

    private Revision(final String str) {
        // Since all strings conform to this format, compareTo() can be delegated to String.compareTo()
        Preconditions.checkArgument(STRING_FORMAT.matcher(str).matches(), "String '%s' does match revision format",
            str);
        this.str = str;
    }

    /**
     * Parse a revision string.
     *
     * @param str String to be parsed
     * @return A Revision instance.
     * @throws IllegalArgumentException if the string format does not conform specification.
     * @throws NullPointerException if the string is null
     */
    public static Revision valueOf(@Nonnull final String str) {
        return new Revision(str);
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
    public static int compare(final Optional<Revision> first, final Optional<Revision> second) {
        if (first.isPresent()) {
            return second.isPresent() ? first.get().compareTo(second.get()) : 1;
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
    public static int compare(@Nullable final Revision first, @Nullable final Revision second) {
        if (first != null) {
            return second != null ? first.compareTo(second) : 1;
        }
        return second != null ? -1 : 0;
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public int compareTo(final Revision o) {
        return str.compareTo(o.str);
    }

    @Override
    public int hashCode() {
        return str.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof Revision && str.equals(((Revision)obj).str);
    }

    @Override
    public String toString() {
        return str;
    }

    Object writeReplace() {
        return new Proxy(str);
    }

    private static final class Proxy implements Externalizable {
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

        private Object readResolve() {
            return Revision.valueOf(str);
        }
    }
}
