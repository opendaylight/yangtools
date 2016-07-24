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
 * https://tools.ietf.org/html/rfc6241#section-7.2
 */
public enum ModifyAction {
    MERGE(true), REPLACE(true), CREATE(false), DELETE(false), REMOVE(false), NONE(true, false);

    public static ModifyAction fromXmlValue(final String xmlNameOfAction) {
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

    private final boolean asDefaultPermitted;
    private final boolean onElementPermitted;

    private ModifyAction(final boolean asDefaultPermitted, final boolean onElementPermitted) {
        this.asDefaultPermitted = asDefaultPermitted;
        this.onElementPermitted = onElementPermitted;
    }

    private ModifyAction(final boolean asDefaultPermitted) {
        this(asDefaultPermitted, true);
    }

    public boolean isAsDefaultPermitted() {
        return asDefaultPermitted;
    }

    public boolean isOnElementPermitted() {
        return onElementPermitted;
    }
}
