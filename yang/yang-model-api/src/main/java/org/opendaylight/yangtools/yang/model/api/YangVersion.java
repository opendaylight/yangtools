/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

public enum YangVersion {
    /**
     * Enum of yang versions.
     */
    VERSION_1("1"), VERSION_1_1("1.1");

    private final String versionStr;

    YangVersion(final String versionStr) {
        this.versionStr = versionStr;
    }

    /**
     * @return String that corresponds to the yang version.
     */
    public String getVersionStr() {
        return versionStr;
    }
}
