/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import com.google.common.base.Verify;

@Beta
public enum CopyType {
    ORIGINAL,
    ADDED_BY_USES,
    ADDED_BY_AUGMENTATION,
    ADDED_BY_USES_AUGMENTATION;

    private final int bit;

    CopyType() {
        // CopyHistory relies on the fact that the result fits into a short
        Verify.verify(ordinal() < Short.SIZE);
        bit = 1 << ordinal();
    }

    int bit() {
        return bit;
    }
}