/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.impl;

import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.yang.xpath.api.YangLiteralExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath;

final class InstanceIdentifierLiteralExpr extends YangLiteralExpr {
    private static final long serialVersionUID = 1L;

    private final YangLocationPath.Absolute path;

    InstanceIdentifierLiteralExpr(final String str, final YangLocationPath.Absolute path) {
        super(str);
        this.path = requireNonNull(path);
    }

    YangLocationPath.Absolute getPath() {
        return path;
    }
}
