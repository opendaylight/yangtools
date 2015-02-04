/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.leafrefcontext;

import org.opendaylight.yangtools.yang.common.QNameModule;
import java.util.LinkedList;

public class QNameWithPredicate {

    public static final QNameWithPredicate UP_PARENT = new QNameWithPredicate(null, "..");

    private LinkedList<QNamePredicate> qnamePredicates;
    private QNameModule moduleQname;
    private String localName;


    private QNameWithPredicate(QNameModule moduleQname, String localName) {
        this.moduleQname = moduleQname;
        this.localName = localName;
        this.qnamePredicates = new LinkedList<QNamePredicate>();
    }


    public static QNameWithPredicate create(QNameModule moduleQname, String localName) {
        return new QNameWithPredicate(moduleQname,localName);
    }

    public LinkedList<QNamePredicate> getQNamePredicates() {
        return qnamePredicates;
    }

    public void addQNamePredicate(QNamePredicate qnamePredicate) {
        qnamePredicates.add(qnamePredicate);
    }

    public QNameModule getModuleQname() {
        return moduleQname;
    }

    public void setModuleQname(QNameModule moduleQname) {
        this.moduleQname = moduleQname;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (moduleQname != null) {
            sb.append("(" + moduleQname.getNamespace());
            sb.append("?revision=" + moduleQname.getRevision());
            sb.append(")");
        }

        sb.append(localName);

        for (QNamePredicate predicate : qnamePredicates) {
            sb.append(predicate);
        }

        return sb.toString();
    }

}
