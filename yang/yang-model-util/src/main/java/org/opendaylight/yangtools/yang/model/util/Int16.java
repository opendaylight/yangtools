/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Implementation of Yang int16 built-in type. <br>
 * int16 represents integer values between -32768 and 32767, inclusively. The
 * Java counterpart of Yang int16 built-in type is {@link Short}.
 *
 * @see AbstractSignedInteger
 * @deprecated Use {@link org.opendaylight.yangtools.yang.model.util.type.BaseTypes#int16Type()} instead
 */
@Deprecated
public final class Int16 extends AbstractSignedInteger implements Immutable {
    private static final String DESCRIPTION = "int16  represents integer values between -32768 and 32767, inclusively.";

    private static final Int16 INSTANCE = new Int16();

    private Int16() {
        super(BaseTypes.INT16_QNAME, DESCRIPTION, Short.MIN_VALUE, Short.MAX_VALUE, "");
    }

    /**
     * Returns default instance of int16 type.
     * @return default instance of int16 type.
     */
    public static Int16 getInstance() {
        return INSTANCE;
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    @Override
    public String toString() {
        return "type " + BaseTypes.INT16_QNAME;
    }

}
