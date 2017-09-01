/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.leafref;

import org.opendaylight.yangtools.yang.common.QName;

class QNamePredicateBuilder {

    private QName identifier;
    private LeafRefPath pathKeyExpression;

    QNamePredicateBuilder() {
    }

    QNamePredicateBuilder(final QName identifier, final LeafRefPath pathKeyExpression) {
        this.identifier = identifier;
        this.pathKeyExpression = pathKeyExpression;
    }

    public QName getIdentifier() {
        return identifier;
    }

    public void setIdentifier(final QName identifier) {
        this.identifier = identifier;
    }

    public LeafRefPath getPathKeyExpression() {
        return pathKeyExpression;
    }

    public void setPathKeyExpression(final LeafRefPath pathKeyExpression) {
        this.pathKeyExpression = pathKeyExpression;
    }

    public QNamePredicate build() {
        return new QNamePredicateImpl(identifier, pathKeyExpression);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append('[');

        sb.append(identifier);
        sb.append("=current()");

        final Iterable<QNameWithPredicate> pathFromRoot = pathKeyExpression
                .getPathFromRoot();

        for (final QNameWithPredicate qName : pathFromRoot) {
            sb.append('/').append(qName);
        }

        sb.append(']');
        return sb.toString();
    }
}
