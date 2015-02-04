/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.leafrefcontext.impl;

import org.opendaylight.yangtools.yang.common.QName;

import org.opendaylight.yangtools.leafrefcontext.api.QNamePredicate;
import org.opendaylight.yangtools.leafrefcontext.api.QNameWithPredicate;
import java.io.Serializable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import java.util.LinkedList;

public class QNameWithPredicateImpl implements Immutable, Serializable,
        QNameWithPredicate {

    private static final long serialVersionUID = 1L;

    private LinkedList<QNamePredicate> qnamePredicates;
    private QNameModule moduleQname;
    private String localName;

    public QNameWithPredicateImpl(QNameModule moduleQname, String localName,
            LinkedList<QNamePredicate> qnamePredicates) {
        this.moduleQname = moduleQname;
        this.localName = localName;
        this.qnamePredicates = qnamePredicates;
    }

    @Override
    public LinkedList<QNamePredicate> getQNamePredicates() {
        return qnamePredicates;
    }

    @Override
    public QNameModule getModuleQname() {
        return moduleQname;
    }

    @Override
    public String getLocalName() {
        return localName;
    }

    @Override
    public QName getQName() {
        return QName.create(moduleQname, localName);
    }

    // FIXME: check also predicates ...
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof QNameWithPredicateImpl)) {
            return false;
        }
        final QNameWithPredicateImpl other = (QNameWithPredicateImpl) obj;
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
