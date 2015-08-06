/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.api;

import java.util.Arrays;

// TODO rename to ModifyOperation

/**
 * http://tools.ietf.org/html/rfc6241#section-7.2
 */
public enum ModifyAction {
    MERGE, REPLACE, CREATE, DELETE, REMOVE, NONE;

    public static ModifyAction fromXmlValue(String xmlNameOfAction) {
        switch (xmlNameOfAction) {
        case "merge":
            return MERGE;
        case "replace":
            return REPLACE;
        case "remove":
            return REMOVE;
        case "delete":
            return DELETE;
        case "create":
            return CREATE;
        case "none":
            return NONE;
        default:
            throw new IllegalArgumentException("Unknown operation " + xmlNameOfAction + " available operations "
                    + Arrays.toString(ModifyAction.values()));
        }
    }

    public boolean isAsDefaultPermitted() {
        boolean isPermitted = this == MERGE;
        isPermitted |= this == REPLACE;
        isPermitted |= this == NONE;
        return isPermitted;
    }

    public boolean isOnElementPermitted() {
        return this != NONE;
    }

}
