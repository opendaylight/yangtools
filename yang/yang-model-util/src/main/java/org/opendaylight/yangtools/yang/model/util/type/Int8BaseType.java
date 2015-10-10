/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import org.opendaylight.yangtools.yang.model.util.BaseTypes;

final class Int8BaseType extends IntegerBaseType {
    static final Int8BaseType INSTANCE = new Int8BaseType();

    private Int8BaseType() {
        super(BaseTypes.INT8_QNAME, Byte.MIN_VALUE, Byte.MAX_VALUE);
    }
}
