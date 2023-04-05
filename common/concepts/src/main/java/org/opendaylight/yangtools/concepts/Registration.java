/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import org.eclipse.jdt.annotation.NonNull;

/**
 * Class representing a registration. Such a registration is a proper resource and should be cleaned up when no longer
 * required.
 */
public interface Registration extends AutoCloseable {
    /**
     * Unregisters the object. This operation is required not to invoke blocking operations. Implementations which
     * require interaction with outside world must provide guarantees that any work is done behind the scenes and
     * the unregistration process looks as if it has already succeeded once this method returns.
     */
    @Override
    void close();

    static @NonNull Registration of(final Runnable callback) {
        return new CallbackRegistration(callback);
    }
}
