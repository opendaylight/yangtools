/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import com.google.common.base.Preconditions;
import java.text.ParseException;
import java.util.Date;
import javax.annotation.Nonnull;

public abstract class Revision {
    private static final class ForDate extends Revision {
        private final Date date;
        private String str;

        ForDate(final Date date) {
            this.date = Preconditions.checkNotNull(date);
        }

        ForDate(final Date date, final String str) {
            this.date = Preconditions.checkNotNull(date);
            this.str = Preconditions.checkNotNull(str);
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

    Revision() {
        // Hidden from the world
    }

    public static Revision forDate(@Nonnull final Date date) {
        return new ForDate(date);
    }

    public static Revision forString(@Nonnull final String str) throws ParseException {
        final Date date = SimpleDateFormatUtil.getRevisionFormat().parse(str);
        return new ForDate(date, str);
    }

    /**
     * Convert this Revision to a Date object. The returned Date will be in UTC.
     *
     * @return Data representation of this Revision
     */
    @Nonnull public abstract Date toDate();

    @Override
    public abstract String toString();
}
