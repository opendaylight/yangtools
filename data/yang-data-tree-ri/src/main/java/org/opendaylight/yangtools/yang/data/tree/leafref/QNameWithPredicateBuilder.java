/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.leafref;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;

class QNameWithPredicateBuilder implements Mutable {
    private final List<QNamePredicate> qnamePredicates = new ArrayList<>();
    private QNameModule moduleQname;
    private String localName;

    static final QNameWithPredicateBuilder UP_PARENT_BUILDER = new QNameWithPredicateBuilder(null, "..") {
        @Override
        public QNameWithPredicate build() {
            return QNameWithPredicate.UP_PARENT;
        }
    };

    QNameWithPredicateBuilder(final QNameModule moduleQname, final String localName) {
        this.moduleQname = moduleQname;
        this.localName = localName;
    }

    public @NonNull QNameWithPredicate build() {
        if (qnamePredicates.isEmpty() && moduleQname != null && localName != null) {
            return new SimpleQNameWithPredicate(QName.create(moduleQname, localName));
        }

        final QNameWithPredicateImpl qNameWithPredicateImpl = new QNameWithPredicateImpl(moduleQname, localName,
            qnamePredicates);

        // QNameWithPredicateImpl has taken a copy
        qnamePredicates.clear();
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

    // FIXME: check also predicates ...
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof final QNameWithPredicateBuilder other)) {
            return false;
        }
        return Objects.equals(localName, other.localName) &&  moduleQname.equals(other.moduleQname);
    }

    @Override
    public int hashCode() {
        int result = moduleQname != null ? moduleQname.hashCode() : 0;
        result = 31 * result + Objects.hashCode(localName);
        return result;
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder();

        if (moduleQname != null) {
            sb.append('(').append(moduleQname.namespace());
            final var rev = moduleQname.revision();
            if (rev != null) {
                sb.append("?revision=").append(rev);
            }
            sb.append(')');
        }

        sb.append(localName);

        for (var predicate : qnamePredicates) {
            sb.append(predicate);
        }

        return sb.toString();
    }
}
