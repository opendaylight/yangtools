/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

/**
 * Class representing a registration. Such a registration is a proper resource and should be cleaned up when no longer
 * required.
 */
public interface Registration extends AutoCloseable {
    /**
     * Unregisters the object. This operation is required not to invoke blocking operations. Implementations which
     * require interaction with outside world must provide guarantees that any work is done behind the scenes and
     * the unregistration process looks as if it has already succeeded once this method returns.
     *
     * <p>
     * The above requirement does not necessarily mean that all interactions with the registered entity seize before
     * this method returns, but they should complete within a reasonable time frame.
     *
     * <p>
     * While the interface contract allows an implementation to ignore the occurrence of RuntimeExceptions,
     * implementations are strongly encouraged to deal with such exceptions internally and to ensure invocations of
     * this method do not fail in such circumstances.
     */
    @Override
    void close();
}
