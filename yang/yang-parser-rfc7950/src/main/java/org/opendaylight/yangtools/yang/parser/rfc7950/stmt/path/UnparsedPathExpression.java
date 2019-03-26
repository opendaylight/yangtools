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
import org.opendaylight.yangtools.yang.model.util.AbstractPathExpression;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.ArgumentUtils;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath;

final class UnparsedPathExpression extends AbstractPathExpression {
    private final RuntimeException cause;
    private final boolean absolute;

    UnparsedPathExpression(final String originalString, final RuntimeException cause) {
        super(originalString);
        this.cause = requireNonNull(cause);
        absolute = ArgumentUtils.isAbsoluteXPath(originalString);
    }

    @Override
    public boolean isAbsolute() {
        return absolute;
    }

    @Override
    public YangLocationPath getLocation() {
        throw new UnsupportedOperationException("Expression '" + getOriginalString() + "' was not parsed", cause);
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return super.addToStringAttributes(helper.add("absolute", absolute)).add("cause", cause);
    }
}
