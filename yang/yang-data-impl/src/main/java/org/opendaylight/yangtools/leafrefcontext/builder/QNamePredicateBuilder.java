/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.leafrefcontext.builder;

import org.opendaylight.yangtools.leafrefcontext.impl.QNamePredicateImpl;

import org.opendaylight.yangtools.leafrefcontext.api.LeafRefPath;
import org.opendaylight.yangtools.leafrefcontext.api.QNamePredicate;
import org.opendaylight.yangtools.leafrefcontext.api.QNameWithPredicate;
import org.opendaylight.yangtools.yang.common.QName;

public class QNamePredicateBuilder {

    private QName identifier;
    private LeafRefPath pathKeyExpression;

    public QNamePredicateBuilder() {
    }

    public QNamePredicateBuilder(QName identifier, LeafRefPath pathKeyExpression) {
        this.identifier = identifier;
        this.pathKeyExpression = pathKeyExpression;
    }

    public QName getIdentifier() {
        return identifier;
    }

    public void setIdentifier(QName identifier) {
        this.identifier = identifier;
    }

    public LeafRefPath getPathKeyExpression() {
        return pathKeyExpression;
    }

    public void setPathKeyExpression(LeafRefPath pathKeyExpression) {
        this.pathKeyExpression = pathKeyExpression;
    }

    public QNamePredicate build() {
        return new QNamePredicateImpl(identifier, pathKeyExpression);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        sb.append(identifier);
        sb.append("=current()");

        Iterable<QNameWithPredicate> pathFromRoot = pathKeyExpression
                .getPathFromRoot();

        for (QNameWithPredicate qName : pathFromRoot) {
            sb.append("/" + qName);
        }

        sb.append("]");
        return sb.toString();
    }
}
