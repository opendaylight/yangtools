/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import org.opendaylight.yangtools.yang.model.util.BaseTypes;

final class Uint8BaseType extends UnsignedBaseType {
    static final Uint8BaseType INSTANCE = new Uint8BaseType();

    private Uint8BaseType() {
        super(BaseTypes.UINT8_QNAME, (short)0, (short)255);
    }
}
