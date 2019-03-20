/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.jdt.annotation.Nullable;

@Beta
public class YangPathExpr implements YangExpr {
    private static final class WithLocation extends YangPathExpr {
        private static final long serialVersionUID = 1L;

        private final YangLocationPath locationPath;

        WithLocation(final YangExpr filterExpr, final YangLocationPath locationPath) {
            super(filterExpr);
            this.locationPath = requireNonNull(locationPath);
        }

        @Override
        public Optional<YangLocationPath> getLocationPath() {
            return Optional.of(locationPath);
        }
    }

    private static final long serialVersionUID = 1L;

    private final YangExpr filterExpr;

    YangPathExpr(final YangExpr filterExpr) {
        this.filterExpr = requireNonNull(filterExpr);
    }

    public static YangPathExpr of(final YangExpr filterExpr) {
        return new YangPathExpr(filterExpr);
    }

    public static YangPathExpr of(final YangExpr expr, final YangLocationPath locationPath) {
        return new WithLocation(expr, locationPath);
    }

    public final YangExpr getFilterExpr() {
        return filterExpr;
    }

    public Optional<YangLocationPath> getLocationPath() {
        return Optional.empty();
    }

    @Override
    public final int hashCode() {
        return Objects.hash(filterExpr, getLocationPath());
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof YangPathExpr)) {
            return false;
        }
        final YangPathExpr other = (YangPathExpr) obj;
        return filterExpr.equals(((YangPathExpr) obj).filterExpr) && getLocationPath().equals(other.getLocationPath());
    }

    @Override
    public final String toString() {
        // FIXME: this is not right
        return "-(" + filterExpr + ")";
    }
}
