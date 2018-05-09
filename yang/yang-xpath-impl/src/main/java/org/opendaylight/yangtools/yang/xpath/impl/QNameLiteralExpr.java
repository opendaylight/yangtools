/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.xpath.api.YangLiteralExpr;

/**
 * Eagerly-bound literal interpreted a PrefixName.
 *
 * @author Robert Varga
 */
final class QNameLiteralExpr extends YangLiteralExpr {
    private static final long serialVersionUID = 1L;

    private final QName qname;

    QNameLiteralExpr(final String str, final QName qname) {
        super(str);
        checkArgument(str.endsWith(qname.getLocalName()));
        this.qname = requireNonNull(qname);
    }

    QName getQName() {
        return qname;
    }
}
