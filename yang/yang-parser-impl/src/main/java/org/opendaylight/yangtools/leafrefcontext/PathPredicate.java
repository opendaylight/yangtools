/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.leafrefcontext;

import org.opendaylight.yangtools.yang.common.QName;

public class PathPredicate {

    private QName identifier;
    private LeafRefPath leftPathExpression;
    private LeafRefPath rightPathExpression;


    public PathPredicate(QName identifier, LeafRefPath rightPathExpression, LeafRefPath leftPathExpression) {
        this.identifier = identifier;
        this.rightPathExpression = rightPathExpression;
        this.leftPathExpression = leftPathExpression;
    }

    public QName getIdentifier() {
        return identifier;
    }

    public void setIdentifier(QName identifier) {
        this.identifier = identifier;
    }

    public LeafRefPath getRightPathExpression() {
        return rightPathExpression;
    }

    public void setRightPathExpression(LeafRefPath rightPathExpression) {
        this.rightPathExpression = rightPathExpression;
    }

    public LeafRefPath getLeftPathExpression() {
        return leftPathExpression;
    }

    public void setLeftPathExpression(LeafRefPath leftPathExpression) {
        this.leftPathExpression = leftPathExpression;
    }


}
