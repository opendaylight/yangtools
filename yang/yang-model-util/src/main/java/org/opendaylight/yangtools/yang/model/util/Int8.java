/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import org.opendaylight.yangtools.yang.common.QName;

/**
 * Implementation of Yang int8 built-in type. <br>
 * int8 represents integer values between -128 and 127, inclusively. The Java
 * counterpart of Yang int8 built-in type is {@link Byte}.
 *
 * @see AbstractSignedInteger
 */
public final class Int8 extends AbstractSignedInteger {
    private static Int8 instance;
    private static final QName NAME = BaseTypes.constructQName("int8");
    private static final String DESCRIPTION = "represents integer values between -128 and 127, inclusively.";

    private Int8() {
        super(NAME, DESCRIPTION, Byte.MIN_VALUE, Byte.MAX_VALUE, "");
    }

    public static Int8 getInstance() {
        if (instance == null) {
            instance = new Int8();
        }
        return instance;
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    @Override
    public String toString() {
        return "type " + NAME;
    }

}
