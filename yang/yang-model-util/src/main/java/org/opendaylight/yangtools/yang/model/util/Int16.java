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
 * Implementation of Yang int16 built-in type. <br>
 * int16 represents integer values between -32768 and 32767, inclusively. The
 * Java counterpart of Yang int16 built-in type is {@link Short}.
 *
 * @see AbstractSignedInteger
 */
public final class Int16 extends AbstractSignedInteger {
    private static Int16 instance;
    private static final QName NAME = BaseTypes.constructQName("int16");
    private static final String DESCRIPTION = "int16  represents integer values between -32768 and 32767, inclusively.";

    private Int16() {
        super(NAME, DESCRIPTION, Short.MIN_VALUE, Short.MAX_VALUE, "");
    }

    public static Int16 getInstance() {
        if (instance == null) {
            instance = new Int16();
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
