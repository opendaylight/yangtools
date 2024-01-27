/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.leafref;

import java.util.Objects;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QNameModule;

abstract class AbstractQNameWithPredicate implements Immutable, QNameWithPredicate {
    @Override
    public final boolean equals(final Object obj) {
        return this == obj || obj instanceof QNameWithPredicate other
            // FIXME: check also predicates ...
            && Objects.equals(getLocalName(), other.getLocalName())
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
            sb.append('(').append(moduleQname.namespace());
            final var rev = moduleQname.revision();
            if (rev != null) {
                sb.append("?revision=").append(rev);
            }
            sb.append(')');
        }

        sb.append(getLocalName());
        getQNamePredicates().forEach(sb::append);
        return sb.toString();
    }
}
