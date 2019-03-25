/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.PathExpression;

/**
 * A simple XPathExpression implementation.
 *
 * @deprecated This is a transitional class to transition from {@link RevisionAwareXPathImpl}. Users are advised to
 *             supply their own implementation of PathExpression.
 */
@Deprecated
@NonNullByDefault
public final class PathExpressionImpl implements PathExpression {
    private final String originalString;
    private final boolean absolute;

    public PathExpressionImpl(final String xpath, final boolean absolute) {
        this.originalString = requireNonNull(xpath);
        this.absolute = absolute;
    }
    @Override
    public String getOriginalString() {
        return originalString;
    }

    @Override
    public boolean isAbsolute() {
        return absolute;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("absolute", absolute).add("originalString", originalString)
                .toString();
    }
}
