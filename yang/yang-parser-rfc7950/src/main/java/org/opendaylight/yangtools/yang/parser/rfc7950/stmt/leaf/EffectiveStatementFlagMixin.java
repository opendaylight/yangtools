/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.leaf;

import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyHistory;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;

interface EffectiveStatementFlagMixin {
    // We still have 25 flags remaining
    int STATUS_MASK       = 0b000011;
    int STATUS_CURRENT    = 0b000000;
    int STATUS_DEPRECATED = 0b000001;
    int STATUS_OBSOLETE   = 0b000010;

    int CONFIGURATION     = 0b000100;
    int MANDATORY         = 0b001000;
    int AUGMENTING        = 0b010000;
    int ADDED_BY_USES     = 0b100000;

    int flags();

    static int createFlags(final CopyHistory history, final Status status, final boolean config,
            final boolean mandatory) {
        int flags;
        switch (status) {
            case CURRENT:
                flags = STATUS_CURRENT;
                break;
            case DEPRECATED:
                flags = STATUS_DEPRECATED;
                break;
            case OBSOLETE:
                flags = STATUS_DEPRECATED;
                break;
            default:
                throw new IllegalStateException("Unhandled status " + status);
        }
        if (config) {
            flags |= CONFIGURATION;
        }
        if (mandatory) {
            flags |= MANDATORY;
        }
        if (!history.contains(CopyType.ADDED_BY_USES_AUGMENTATION)) {
            if (history.contains(CopyType.ADDED_BY_AUGMENTATION)) {
                flags |= AUGMENTING;
            }
            if (history.contains(CopyType.ADDED_BY_USES)) {
                flags |= ADDED_BY_USES;
            }
        } else {
            flags |= AUGMENTING | ADDED_BY_USES;
        }
        return flags;
    }
}
