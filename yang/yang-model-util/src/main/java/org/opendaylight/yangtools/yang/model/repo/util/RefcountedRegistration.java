/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.util;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistration;

final class RefcountedRegistration {
    private final SchemaSourceRegistration<?> reg;
    private int refcount = 1;

    RefcountedRegistration(final SchemaSourceRegistration<?> reg) {
        this.reg = requireNonNull(reg);
    }

    public void incRef() {
        refcount++;
    }

    public boolean decRef() {
        checkState(refcount > 0, "Refcount underflow: %s", refcount);

        if (0 == --refcount) {
            reg.close();
            return true;
        }

        return false;
    }
}