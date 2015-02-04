/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.leafrefcontext;

import org.opendaylight.yangtools.yang.common.QName;

public class QNamePredicate {

    private QName identifier;
    private LeafRefPath identifierPath;
    private LeafRefPath pathKeyExpression;

    public QNamePredicate(){

    }

    public QNamePredicate(QName identifier, LeafRefPath pathKeyExpression, LeafRefPath identifierPath) {
        this.identifier = identifier;
        this.pathKeyExpression = pathKeyExpression;
        this.identifierPath = identifierPath;
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

    public LeafRefPath getIdentifierPath() {
        return identifierPath;
    }

    public void setIdentifierPath(LeafRefPath identifierPath) {
        this.identifierPath = identifierPath;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("[");

        sb.append(identifier);
        sb.append("=current()");

        Iterable<QNameWithPredicate> pathFromRoot = pathKeyExpression.getPathFromRoot();

        for (QNameWithPredicate qName : pathFromRoot) {
            sb.append("/"+qName);
        }

        sb.append("]");
        return sb.toString();
    }

}
