/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.leafref;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;

public class SimpleQNameWithPredicate extends AbstractQNameWithPredicate {
    private static final long serialVersionUID = 1L;

    private final QName qname;

    SimpleQNameWithPredicate(final QName qname) {
        this.qname = requireNonNull(qname);
    }

    @Override
    public List<QNamePredicate> getQNamePredicates() {
        return ImmutableList.of();
    }

    @Override
    public QNameModule getModuleQname() {
        return qname.getModule();
    }

    @Override
    public String getLocalName() {
        return qname.getLocalName();
    }

    @Override
    public QName getQName() {
        return qname;
    }
}
