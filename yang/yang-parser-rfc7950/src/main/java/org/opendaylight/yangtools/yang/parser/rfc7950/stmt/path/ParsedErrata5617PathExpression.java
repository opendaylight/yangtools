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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.Errata5617PathExpression;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.Relative;

final class ParsedErrata5617PathExpression extends ParsedPathExpression implements Errata5617PathExpression {
    private final @NonNull YangLocationPath derefArgument;

    ParsedErrata5617PathExpression(final YangLocationPath derefArgument, final Relative location,
            final String originalString) {
        super(location, originalString);
        this.derefArgument = requireNonNull(derefArgument);
    }

    @Override
    public YangLocationPath getDerefArgument() {
        return derefArgument;
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return super.addToStringAttributes(helper.add("derefArgument", derefArgument));
    }
}
