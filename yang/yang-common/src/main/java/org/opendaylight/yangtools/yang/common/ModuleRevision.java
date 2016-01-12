/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import java.text.ParseException;
import java.util.Date;
import java.util.regex.Pattern;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * RFC6020 module revision. As per definition a revision is a string corresponding to the format:
 * <pre>4DIGIT "-" 2DIGIT "-" 2DIGIT</pre>.
 */
public final class ModuleRevision implements Comparable<ModuleRevision>, Immutable {
    private static final Pattern VALUE_PATTERN = Pattern.compile("\\d{4}-\\d\\d-\\d\\d");
    private final String value;
    private volatile Date date;

    private ModuleRevision(final String value) {
        this.value = Preconditions.checkNotNull(value);
    }

    /**
     * Construct a {@link ModuleRevision} for a particular string.
     *
     * @param revision Revision string
     * @return A {@link ModuleRevision} instance.
     * @throws NullPointerException if revision is null
     * @throws IllegalArgumentException when argument does not match expected format
     */
    public static ModuleRevision valueOf(final String revision) {
        Preconditions.checkArgument(VALUE_PATTERN.matcher(revision).matches(), "Illegal value %s", revision);
        return new ModuleRevision(revision);
    }

    /**
     * Construct a {@link ModuleRevision} for a particular {@link Date}.
     *
     * @param date Revision string
     * @return A {@link ModuleRevision} instance.
     * @throws NullPointerException if revision is null
     * @throws IllegalArgumentException when argument does not match expected format
     * @deprecated This method is provided for migration only and will be removed once all Date-based interfaces are
     *             removed.
     */
    @Deprecated
    public static ModuleRevision valueOf(final Date date) {
        return new ModuleRevision(SimpleDateFormatUtil.getRevisionFormat().format(date));
    }

    /**
     * Return this ModuleRevision as a Date.
     *
     * @return A {@link Date} corresponding to this revision.
     * @deprecated This method is provided for migration only and will be removed once all Date-based interfaces are
     *             removed.
     */
    @Deprecated
    public Date toDate() {
        Date ret = date;
        if (ret == null) {
            synchronized (this) {
                ret = date;
                if (ret == null) {
                    try {
                        ret = SimpleDateFormatUtil.getRevisionFormat().parse(value);
                    } catch (ParseException e) {
                        throw Throwables.propagate(e);
                    }

                    date = ret;
                }
            }
        }

        return ret;
    }

    @Override
    public int compareTo(final ModuleRevision o) {
        return value.compareTo(o.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        return o == this || ((o instanceof ModuleRevision) && value.equals(((ModuleRevision)o).value));
    }

    @Override
    public String toString() {
        return value;
    }
}
