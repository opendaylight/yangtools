/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.leafrefcontext.builder;

import org.opendaylight.yangtools.leafrefcontext.impl.QNameWithPredicateImpl;

import org.opendaylight.yangtools.leafrefcontext.api.QNamePredicate;
import org.opendaylight.yangtools.leafrefcontext.api.QNameWithPredicate;
import java.util.LinkedList;
import org.opendaylight.yangtools.yang.common.QNameModule;

public class QNameWithPredicateBuilder {

    private LinkedList<QNamePredicate> qnamePredicates;
    private QNameModule moduleQname;
    private String localName;

    public static QNameWithPredicateBuilder UP_PARENT_BUILDER = new QNameWithPredicateBuilder(
            null, "..") {
        public QNameWithPredicate build() {
            return QNameWithPredicate.UP_PARENT;
        }
    };

    public QNameWithPredicateBuilder(QNameModule moduleQname, String localName) {
        this.moduleQname = moduleQname;
        this.localName = localName;
        this.qnamePredicates = new LinkedList<QNamePredicate>();
    }

    public QNameWithPredicate build() {
        QNameWithPredicateImpl qNameWithPredicateImpl = new QNameWithPredicateImpl(
                moduleQname, localName, qnamePredicates);

        this.qnamePredicates = new LinkedList<QNamePredicate>();

        return qNameWithPredicateImpl;
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

    // FIXME: check also predicates ...
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof QNameWithPredicateBuilder)) {
            return false;
        }
        final QNameWithPredicateBuilder other = (QNameWithPredicateBuilder) obj;
        if (localName == null) {
            if (other.localName != null) {
                return false;
            }
        } else if (!localName.equals(other.localName)) {
            return false;
        }
        return moduleQname.equals(other.moduleQname);
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
