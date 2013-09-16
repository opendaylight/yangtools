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
 * Implementation of Yang uint32 built-in type. <br>
 * uint32 represents integer values between 0 and 4294967295, inclusively.
 *
 */
public final class Uint32 extends AbstractUnsignedInteger {
    public static final long MAX_VALUE = 4294967295L;
    private static Uint32 instance;
    private static final QName NAME = BaseTypes.constructQName("uint32");
    private static final String DESCRIPTION = "uint32 represents integer values between 0 and 4294967295, inclusively.";

    private Uint32() {
        super(NAME, DESCRIPTION, MAX_VALUE, "");
    }

    public static Uint32 getInstance() {
        if (instance == null) {
            instance = new Uint32();
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
