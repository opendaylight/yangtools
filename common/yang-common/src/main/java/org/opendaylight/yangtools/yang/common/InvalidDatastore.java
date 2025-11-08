/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Place-holder for a dynamic {@link DatastoreIdentity}. There are no such identities, which we enforce through the
 * canonical constructor. This means we do not really have to implement the interface contract.
 *
 * <p>This class should be removed once there is actually a known dynamic datastore.
 */
@NonNullByDefault
record InvalidDatastore() implements DatastoreIdentity.Known.Dynamic {
    InvalidDatastore() {
        throw new UnsupportedOperationException();
    }

    @Override
    public QName value() {
        throw new UnsupportedOperationException();
    }
}
