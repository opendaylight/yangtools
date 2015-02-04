/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.leafrefcontext.impl;

import org.opendaylight.yangtools.leafrefcontext.api.LeafRefPath;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.leafrefcontext.api.QNamePredicate;
import org.opendaylight.yangtools.leafrefcontext.api.QNameWithPredicate;
import java.io.Serializable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;

public class QNamePredicateImpl implements Immutable, Serializable,
        QNamePredicate {

    private static final long serialVersionUID = 1L;
    private QName identifier;
    private LeafRefPath pathKeyExpression;

    public QNamePredicateImpl(QName identifier, LeafRefPath pathKeyExpression) {
        Preconditions.checkNotNull(identifier,
                "QNamePredicate: identifier should not be null");
        Preconditions.checkNotNull(pathKeyExpression,
                "QNamePredicate: pathKeyExpression should not be null");

        this.identifier = identifier;
        this.pathKeyExpression = pathKeyExpression;
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
