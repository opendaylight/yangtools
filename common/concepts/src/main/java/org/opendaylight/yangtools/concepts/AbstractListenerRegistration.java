/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import java.util.EventListener;
import org.eclipse.jdt.annotation.NonNull;

@Deprecated(since = "12.0.0", forRemoval = true)
public abstract class AbstractListenerRegistration<T extends EventListener> extends AbstractObjectRegistration<T>
        implements ListenerRegistration<T> {
    protected AbstractListenerRegistration(final @NonNull T listener) {
        super(listener);
    }
}

