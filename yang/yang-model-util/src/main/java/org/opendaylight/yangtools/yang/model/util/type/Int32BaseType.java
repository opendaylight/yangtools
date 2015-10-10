/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import org.opendaylight.yangtools.yang.model.util.BaseTypes;

final class Int32BaseType extends IntegerBaseType {
    static final Int32BaseType INSTANCE = new Int32BaseType();

    private Int32BaseType() {
        super(BaseTypes.INT32_QNAME, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }
}
