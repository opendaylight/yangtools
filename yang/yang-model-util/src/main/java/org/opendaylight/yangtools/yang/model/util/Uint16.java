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
 * Implementation of Yang uint16 built-in type. <br>
 * uint16 represents integer values between 0 and 65535, inclusively. The Java
 * counterpart of Yang uint16 built-in type is {@link Integer}.
 * 
 */
public final class Uint16 extends AbstractUnsignedInteger {
    public static final int MAX_VALUE = 65535;
    private static Uint16 instance;
    private static final QName NAME = BaseTypes.constructQName("uint16");
    private static final String DESCRIPTION = "uint16 represents integer values between 0 and 65535, inclusively.";

    private Uint16() {
        super(NAME, DESCRIPTION, MAX_VALUE, "");
    }

    public static Uint16 getInstance() {
        if (instance == null) {
            instance = new Uint16();
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
