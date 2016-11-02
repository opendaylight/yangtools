/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.transform;

import com.google.common.base.Preconditions;
import java.util.Map;
import java.util.function.Function;
import org.opendaylight.yangtools.yang.common.QName;

class QNameReplacementFunction implements Function<QName, QName> {

    private final Map<QName, QName> mapping;

    QNameReplacementFunction(Map<QName, QName> mapping) {
        this.mapping = Preconditions.checkNotNull(mapping);
    }

    @Override
    public QName apply(QName input) {
        QName potential = mapping.get(input);
        return potential != null ? potential : input;
    }

}
