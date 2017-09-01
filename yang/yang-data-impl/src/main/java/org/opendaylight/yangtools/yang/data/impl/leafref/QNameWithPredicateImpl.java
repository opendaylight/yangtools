/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.leafref;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;

final class QNameWithPredicateImpl implements Immutable, Serializable, QNameWithPredicate {

    private static final long serialVersionUID = 1L;

    private final List<QNamePredicate> qnamePredicates;
    private final QNameModule moduleQname;
    private final String localName;

    QNameWithPredicateImpl(final QNameModule moduleQname, final String localName,
            final List<QNamePredicate> qnamePredicates) {
        this.moduleQname = moduleQname;
        this.localName = localName;
        this.qnamePredicates = ImmutableList.copyOf(qnamePredicates);
    }

    @Override
    public List<QNamePredicate> getQNamePredicates() {
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
        return Objects.equals(localName, other.localName) && moduleQname.equals(other.moduleQname);
    }

    @Override
    public int hashCode() {
        int result = moduleQname != null ? moduleQname.hashCode() : 0;
        result = 31 * result + (localName != null ? localName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        if (moduleQname != null) {
            sb.append('(').append(moduleQname.getNamespace());
            sb.append("?revision=").append(moduleQname.getRevision());
            sb.append(')');
        }

        sb.append(localName);

        for (final QNamePredicate predicate : qnamePredicates) {
            sb.append(predicate);
        }

        return sb.toString();
    }
}
