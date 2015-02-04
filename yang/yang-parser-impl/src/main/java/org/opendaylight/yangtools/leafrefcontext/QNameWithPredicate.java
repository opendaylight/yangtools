/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.leafrefcontext;

import java.net.URI;

import org.opendaylight.yangtools.yang.common.QName;

public class QNameWithPredicate extends QName {

    private static final long serialVersionUID = 1L;
    private PathPredicate pathPredicate;

    public QNameWithPredicate(URI namespace, String localName) {
        super(namespace, localName);
        this.pathPredicate = null;
    }

    public QNameWithPredicate(URI namespace, String localName,
            PathPredicate pathPredicate) {
        super(namespace, localName);
        this.pathPredicate = pathPredicate;
    }

    public PathPredicate getPathPredicate() {
        return pathPredicate;
    }

    public void setPathPredicate(PathPredicate pathPredicate) {
        this.pathPredicate = pathPredicate;
    }

}
