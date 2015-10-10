/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import java.math.BigInteger;
import org.opendaylight.yangtools.yang.model.util.BaseTypes;

final class Uint64BaseType extends UnsignedBaseType {
    static final Uint64BaseType INSTANCE = new Uint64BaseType();

    private Uint64BaseType() {
        super(BaseTypes.UINT64_QNAME, BigInteger.ZERO, new BigInteger("18446744073709551615"));
    }
}
