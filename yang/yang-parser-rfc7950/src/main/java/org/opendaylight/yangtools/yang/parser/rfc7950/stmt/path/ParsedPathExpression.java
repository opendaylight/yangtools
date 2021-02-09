/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.path;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.spi.AbstractPathExpression;

@NonNullByDefault
final class ParsedPathExpression extends AbstractPathExpression {
    private final Steps steps;

    ParsedPathExpression(final Steps steps, final String originalString) {
        super(originalString);
        this.steps = requireNonNull(steps);
    }

    @Override
    public Steps getSteps() {
        return steps;
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return super.addToStringAttributes(helper.add("steps", steps));
    }
}
