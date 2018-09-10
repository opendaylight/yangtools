/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.leafref;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;

abstract class AbstractQNameWithPredicate implements Immutable, Serializable, QNameWithPredicate {
    private static final long serialVersionUID = 1L;

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof QNameWithPredicate)) {
            return false;
        }
        final QNameWithPredicate other = (QNameWithPredicate) obj;
        // FIXME: check also predicates ...
        return Objects.equals(getLocalName(), other.getLocalName())
                && Objects.equals(getModuleQname(), other.getModuleQname());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getModuleQname(), getLocalName());
    }

    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();

        final QNameModule moduleQname = getModuleQname();
        if (moduleQname != null) {
            sb.append('(').append(moduleQname.getNamespace());
            final Optional<Revision> rev = moduleQname.getRevision();
            if (rev.isPresent()) {
                sb.append("?revision=").append(rev.get());
            }
            sb.append(')');
        }

        sb.append(getLocalName());
        getQNamePredicates().forEach(sb::append);
        return sb.toString();
    }
}
