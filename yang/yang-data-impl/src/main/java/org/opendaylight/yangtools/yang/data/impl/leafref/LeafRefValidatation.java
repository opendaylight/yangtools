/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.leafref;

import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeCandidate;

/**
 * @deprecated Use {@link LeafRefValidation} instead.
 */
@Deprecated
public final class LeafRefValidatation {
    private LeafRefValidatation() {

    }

    public static void validate(final DataTreeCandidate tree, final LeafRefContext rootLeafRefCtx)
            throws LeafRefDataValidationFailedException {
        LeafRefValidation.validate(tree, rootLeafRefCtx);
    }
}
