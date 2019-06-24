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
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.Relative;

@NonNullByDefault
final class ParsedDerefPathExpression extends ParsedPathExpression {
    private final Relative derefArgument;

    ParsedDerefPathExpression(final Relative derefArgument, final Relative location, final String originalString) {
        super(location, originalString);
        this.derefArgument = requireNonNull(derefArgument);
    }

    @Override
    public Optional<YangLocationPath> getDerefArgument() {
        return Optional.of(derefArgument);
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return super.addToStringAttributes(helper.add("derefArgument", derefArgument));
    }
}
