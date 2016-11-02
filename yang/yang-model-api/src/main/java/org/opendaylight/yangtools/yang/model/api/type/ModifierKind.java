/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.model.api.type;

import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;

/**
 * Enum describing the effect of a YANG modifier statement.
 *
 * As of YANG 1.1 (RFC7950) there is only one modifier value
 * available and that is "invert-match".
 * If there are more possible values added in the future,
 * this enum can be extended with more enum constants.
 */
public enum ModifierKind {

    INVERT_MATCH("invert-match");

    private final String keyword;

    ModifierKind(final String keyword) {
        this.keyword = Preconditions.checkNotNull(keyword);
    }

    /**
     * @return String that corresponds to the yang keyword.
     */
    public @Nonnull String getKeyword() {
        return keyword;
    }
}
