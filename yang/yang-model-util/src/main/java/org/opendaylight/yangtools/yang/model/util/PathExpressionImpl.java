/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.base.MoreObjects.ToStringHelper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath;

/**
 * A simple XPathExpression implementation.
 *
 * @deprecated This is a transitional class to transition from {@link RevisionAwareXPathImpl}. Users are advised to
 *             supply their own implementation of PathExpression.
 */
@Deprecated
@NonNullByDefault
public final class PathExpressionImpl extends AbstractPathExpression {
    private final @Nullable YangLocationPath location;
    private final boolean absolute;

    @SuppressFBWarnings(value = "NP_STORE_INTO_NONNULL_FIELD", justification = "Non-grok on SpotBugs part")
    public PathExpressionImpl(final String xpath, final boolean absolute) {
        super(xpath);
        this.absolute = absolute;
        this.location = null;
    }

    public PathExpressionImpl(final String xpath, final YangLocationPath location) {
        super(xpath);
        this.absolute = location.isAbsolute();
        this.location = location;
    }

    @Override
    public boolean isAbsolute() {
        return absolute;
    }

    @Override
    public YangLocationPath getLocation() {
        final YangLocationPath loc = location;
        if (loc == null) {
            throw new UnsupportedOperationException("Location has not been provided");
        }
        return loc;
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return super.addToStringAttributes(helper.add("absolute", absolute).add("location", location));
    }
}
