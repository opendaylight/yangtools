/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import java.util.EventListener;

/**
 * Class representing a {@link Registration} of an {@link EventListener}. This
 * is interface provides the additional guarantee that the process of
 * unregistration cannot fail for predictable reasons.
 */
public interface ListenerRegistration<T extends EventListener> extends ObjectRegistration<T> {
    /**
     * Unregister the listener. No events should be delivered to the listener
     * once this method returns successfully. While the interface contract
     * allows an implementation to ignore the occurence of RuntimeExceptions,
     * implementations are strongly encouraged to deal with such exceptions
     * internally and to ensure invocations of this method do not fail in such
     * circumstances.
     */
    @Override
    void close() throws Exception;
}
