/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import static java.util.Objects.requireNonNull;

import java.io.Serial;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.Relative;

public sealed class YangPathExpr implements YangExpr {
    private static final class WithLocation extends YangPathExpr {
        @Serial
        private static final long serialVersionUID = 1L;

        private final Relative locationPath;

        WithLocation(final YangExpr filterExpr, final Relative locationPath) {
            super(filterExpr);
            this.locationPath = requireNonNull(locationPath);
        }

        @Override
        public Optional<Relative> getLocationPath() {
            return Optional.of(locationPath);
        }
    }

    @Serial
    private static final long serialVersionUID = 1L;

    private final YangExpr filterExpr;

    private YangPathExpr(final YangExpr filterExpr) {
        this.filterExpr = requireNonNull(filterExpr);
    }

    public static YangPathExpr of(final YangExpr filterExpr) {
        return new YangPathExpr(filterExpr);
    }

    public static YangPathExpr of(final YangExpr expr, final Relative locationPath) {
        return new WithLocation(expr, locationPath);
    }

    public final YangExpr getFilterExpr() {
        return filterExpr;
    }

    public Optional<Relative> getLocationPath() {
        return Optional.empty();
    }

    @Override
    public final int hashCode() {
        return Objects.hash(filterExpr, getLocationPath());
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof YangPathExpr other && filterExpr.equals(other.filterExpr)
            && getLocationPath().equals(other.getLocationPath());
    }

    @Override
    public final String toString() {
        // FIXME: this is not right
        return "-(" + filterExpr + ")";
    }
}
