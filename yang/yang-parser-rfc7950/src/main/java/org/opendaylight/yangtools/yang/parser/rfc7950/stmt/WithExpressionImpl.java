/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath.WithExpression;
import org.opendaylight.yangtools.yang.model.util.RevisionAwareXPathImpl;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression.QualifiedBound;

// TODO: disconnect from RevisionAwareXPathImpl
// FIXME: absolute should not be needed: since the expression is qualified, we known how to interpret it
// FIXME: originalString() is an escape hatch: everybody should operate of QualifiedBound
@NonNullByDefault
final class WithExpressionImpl extends RevisionAwareXPathImpl implements WithExpression {
    private final QualifiedBound xpathExpression;

    WithExpressionImpl(final String xpath, final boolean absolute, final QualifiedBound xpathExpression) {
        super(xpath, absolute);
        this.xpathExpression = requireNonNull(xpathExpression);
    }

    @Override
    public QualifiedBound getXPathExpression() {
        return xpathExpression;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("xpath", getOriginalString()).toString();
    }
}
