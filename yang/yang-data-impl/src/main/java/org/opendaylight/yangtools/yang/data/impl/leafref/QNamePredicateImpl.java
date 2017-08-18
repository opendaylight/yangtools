/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.leafref;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;

class QNamePredicateImpl implements Immutable, Serializable, QNamePredicate {

    private static final long serialVersionUID = 1L;
    private final QName identifier;
    private final LeafRefPath pathKeyExpression;

    public QNamePredicateImpl(final QName identifier, final LeafRefPath pathKeyExpression) {
        this.identifier = requireNonNull(identifier, "QNamePredicate: identifier should not be null");
        this.pathKeyExpression = requireNonNull(pathKeyExpression, "QNamePredicate: pathKeyExpression should not be null");
    }

    @Override
    public QName getIdentifier() {
        return identifier;
    }

    @Override
    public LeafRefPath getPathKeyExpression() {
        return pathKeyExpression;
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
