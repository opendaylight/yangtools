/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.leafref;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;

final class QNameWithPredicateImpl extends AbstractQNameWithPredicate {

    private static final long serialVersionUID = 1L;

    private final ImmutableList<QNamePredicate> qnamePredicates;
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
}
