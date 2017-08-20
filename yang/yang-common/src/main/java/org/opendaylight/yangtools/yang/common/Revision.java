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
import javax.annotation.Nonnull;

/**
 * Dedicated object identifying a YANG module revision.
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

    /**
     * {@inheritDoc}
     *
     * @return String representation of this revision.
     */
    @Override
    public abstract String toString();

    final Object writeReplace() {
        return new Proxy(toString());
    }
}
