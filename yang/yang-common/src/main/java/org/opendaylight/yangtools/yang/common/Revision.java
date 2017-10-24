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
import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Dedicated object identifying a YANG module revision.
 *
 * <p>
 * <h3>API design note</h3>
 * This class defines the contents of a revision statement, but modules do not require to have a revision (e.g. they
 * have not started to keep track of revisions).
 *
 * <p>APIs which involve this class should always transfer instances via {@code Optional<Revision>}, which is
 * the primary bridge data type. #compareOptional() is provided defining total ordering on such Optionals.
 *
 * <p>Implementations can use nullable fields with explicit conversions to/from {@link Optional}
 *
 * @author Robert Varga
 */
public abstract class Revision implements Comparable<Revision>, Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Legacy implementation.
     *
     * @author Robert Varga
     */
    private static final class ForDate extends Revision {
        private static final long serialVersionUID = 1L;

        private final Date date;
        private String str;

        ForDate(final Date date) {
            this.date = requireNonNull(date);
        }

        ForDate(final Date date, final String str) {
            this.date = requireNonNull(date);
            this.str = requireNonNull(str);
        }

        @Override
        public Date toDate() {
            return date;
        }

        @Override
        public String toString() {
            String ret = str;
            if (ret == null) {
                synchronized (this) {
                    ret = str;
                    if (ret == null) {
                        ret = SimpleDateFormatUtil.getRevisionFormat().format(date);
                        str = ret;
                    }
                }
            }

            return ret;
        }
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
            try {
                return Revision.forString(str);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    Revision() {
        // Hidden from the world
    }

    /**
     * Convert a Date into a Revision.
     *
     * @param date Input date
     * @return A Revision instance.
     *
     * @deprecated Transition bridge method to ease transition from Date.
     */
    @Deprecated
    public static Revision forDate(@Nonnull final Date date) {
        return new ForDate(date);
    }

    /**
     * Parse a revision string.
     *
     * @param str String to be parsed
     * @return A Revision instance.
     * @throws ParseException if the string format does not conform specification.
     */
    public static Revision forString(@Nonnull final String str) throws ParseException {
        final Date date = SimpleDateFormatUtil.getRevisionFormat().parse(str);
        return new ForDate(date, str);
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
    public static int compareOptional(final Optional<Revision> first, final Optional<Revision> second) {
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
    public static int compareNullable(@Nullable final Revision first, @Nullable final Revision second) {
        if (first != null) {
            return second != null ? first.compareTo(second) : 1;
        }
        return second != null ? -1 : 0;
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public final int compareTo(final Revision o) {
        return toDate().compareTo(o.toDate());
    }

    /**
     * Convert this Revision to a Date object. The returned Date will be in UTC.
     *
     * @return Data representation of this Revision
     *
     * @deprecated Transition bridge method to ease transition from Date.
     */
    @Deprecated
    @Nonnull public abstract Date toDate();

    @Override
    public abstract String toString();

    final Object writeReplace() {
        return new Proxy(toString());
    }
}
