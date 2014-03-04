/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.util;

/**
 * 
 * Contains constants used when methods and attributes for YANG list key are
 * generated.
 */
public final class ListKeyConstants {

    /**
     * Value which represents name of class field for list key
     */
    public static final String KEY_FIELD_NAME = "key$";

    /**
     * Name of getter method for list key in builder
     */
    public static final String KEY_BUILDER_GETTER_NAME = "key";

    /**
     * Name of getter method for list key in interface and implementation of
     * interface
     */
    public static final String KEY_INTERFACE_GETTER_NAME = "key";

    /**
     * Name of setter method for list key in interface, builder and
     * implementation of interface
     */
    public static final String KEY_SETTER_NAME = "key";

    /**
     * Creation of new instance is prohibited.
     */
    private ListKeyConstants() {
    }
}
