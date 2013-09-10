/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;

/**
 * Implementation of Yang uint8 built-in type. <br>
 * uint8 represents integer values between 0 and 255, inclusively.
 * 
 * @see AbstractUnsignedInteger
 */
public final class Uint8 extends AbstractUnsignedInteger {
    public static final int MAX_VALUE = 255;
    private static Uint8 instance;
    private static final QName NAME = BaseTypes.constructQName("uint8");
    private static final String DESCRIPTION = "uint8  represents integer values between 0 and 255, inclusively.";

    private Uint8() {
        super(NAME, DESCRIPTION, MAX_VALUE, "");
    }

    public static Uint8 getInstance() {
        if (instance == null) {
            instance = new Uint8();
        }
        return instance;
    }

    @Override
    public UnsignedIntegerTypeDefinition getBaseType() {
        return this;
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
