/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.leafref;

import java.util.LinkedList;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QNameModule;

class QNameWithPredicateBuilder {

    private List<QNamePredicate> qnamePredicates;
    private QNameModule moduleQname;
    private String localName;

    public static QNameWithPredicateBuilder UP_PARENT_BUILDER = new QNameWithPredicateBuilder(
            null, "..") {
        @Override
        public QNameWithPredicate build() {
            return QNameWithPredicate.UP_PARENT;
        }
    };

    public QNameWithPredicateBuilder(final QNameModule moduleQname, final String localName) {
        this.moduleQname = moduleQname;
        this.localName = localName;
        this.qnamePredicates = new LinkedList<QNamePredicate>();
    }

    public QNameWithPredicate build() {
        final QNameWithPredicateImpl qNameWithPredicateImpl = new QNameWithPredicateImpl(
                moduleQname, localName, qnamePredicates);

        this.qnamePredicates = new LinkedList<QNamePredicate>();

        return qNameWithPredicateImpl;
    }

    public List<QNamePredicate> getQNamePredicates() {
        return qnamePredicates;
    }

    public void addQNamePredicate(final QNamePredicate qnamePredicate) {
        qnamePredicates.add(qnamePredicate);
    }

    public QNameModule getModuleQname() {
        return moduleQname;
    }

    public void setModuleQname(final QNameModule moduleQname) {
        this.moduleQname = moduleQname;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(final String localName) {
        this.localName = localName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        if (moduleQname != null) {
            sb.append("(");
            sb.append(moduleQname.getNamespace());
            sb.append("?revision=");
            sb.append(moduleQname.getRevision());
            sb.append(")");
        }

        sb.append(localName);

        for (final QNamePredicate predicate : qnamePredicates) {
            sb.append(predicate);
        }

        return sb.toString();
    }
}
