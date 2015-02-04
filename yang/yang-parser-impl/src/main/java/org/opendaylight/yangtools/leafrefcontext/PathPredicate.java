/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.leafrefcontext;

import org.opendaylight.yangtools.yang.common.QName;

import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class PathPredicate {

    private QName identifier;
    private SchemaPath pathExpression;


    public PathPredicate(QName identifier, SchemaPath pathExpression) {
        this.identifier = identifier;
        this.pathExpression = pathExpression;
    }

    public QName getIdentifier() {
        return identifier;
    }

    public void setIdentifier(QName identifier) {
        this.identifier = identifier;
    }

    public SchemaPath getPathExpression() {
        return pathExpression;
    }

    public void setPathExpression(SchemaPath pathExpression) {
        this.pathExpression = pathExpression;
    }


}
