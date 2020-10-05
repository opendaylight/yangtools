/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A simple XPathExpression implementation.
 *
 * @deprecated Users are advised to supply their own implementation of PathExpression.
 */
@Deprecated(forRemoval = true)
@NonNullByDefault
public final class PathExpressionImpl extends AbstractPathExpression {
    private final @Nullable Steps steps;
    private final boolean absolute;

    @SuppressFBWarnings(value = "NP_STORE_INTO_NONNULL_FIELD", justification = "Non-grok on SpotBugs part")
    public PathExpressionImpl(final String xpath, final boolean absolute) {
        super(xpath);
        this.absolute = absolute;
        this.steps = null;
    }

    public PathExpressionImpl(final String xpath, final Steps steps) {
        super(xpath);
        this.steps = requireNonNull(steps);
        this.absolute = steps instanceof LocationPathSteps
                && ((LocationPathSteps) steps).getLocationPath().isAbsolute();
    }

    @Override
    public boolean isAbsolute() {
        return absolute;
    }

    @Override
    public Steps getSteps() {
        final Steps loc = steps;
        if (loc == null) {
            throw new UnsupportedOperationException("Steps have not been provided");
        }
        return loc;
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return super.addToStringAttributes(helper.add("absolute", absolute).add("steps", steps));
    }
}
