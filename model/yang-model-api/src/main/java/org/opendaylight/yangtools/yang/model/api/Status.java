/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Enumeration describing YANG 'status' statement. If no status is specified, the
 * default is CURRENT.
 */
@NonNullByDefault
public enum Status {
    /**
     * CURRENT means that the definition is current and valid.
     */
    CURRENT("current"),
    /**
     * DEPRECATED indicates an obsolete definition, but it permits new/
     * continued implementation in order to foster interoperability with
     * older/existing implementations.
     */
    DEPRECATED("deprecated"),
    /**
     * OBSOLETE means the definition is obsolete and SHOULD NOT be implemented
     * and/or can be removed from implementations.
     */
    OBSOLETE("obsolete");

    private final String argumentString;

    Status(final String argumentString) {
        this.argumentString = argumentString;
    }

    @Beta
    public String getArgumentString() {
        return argumentString;
    }

    @Beta
    public static Status forArgumentString(final String argumentString) {
        switch (argumentString) {
            case "current":
                return CURRENT;
            case "deprecated":
                return DEPRECATED;
            case "obsolete":
                return OBSOLETE;
            default:
                throw new IllegalArgumentException("Invalid status string '" + argumentString + "'");
        }
    }
}
