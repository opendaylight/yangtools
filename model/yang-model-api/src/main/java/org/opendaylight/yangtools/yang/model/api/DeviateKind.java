/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.model.api;

import static java.util.Objects.requireNonNull;

/**
 * Enum describing YANG deviation 'deviate' statement. It defines how the
 * device's implementation of the target node deviates from its original
 * definition.
 */
public enum DeviateKind {

    NOT_SUPPORTED("not-supported"), ADD("add"), REPLACE("replace"), DELETE("delete");

    private final String keyword;

    DeviateKind(final String keyword) {
        this.keyword = requireNonNull(keyword);
    }

    /**
     * Returns the YANG keyword corresponding to this object.
     *
     * @return String that corresponds to the yang keyword.
     */
    public String getKeyword() {
        return keyword;
    }
}
