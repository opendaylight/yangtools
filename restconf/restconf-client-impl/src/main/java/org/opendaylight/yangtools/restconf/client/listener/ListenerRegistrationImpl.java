/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client.listener;

import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.binding.NotificationListener;

public class ListenerRegistrationImpl<T extends NotificationListener> implements ListenerRegistration {

    private T listener;

    public ListenerRegistrationImpl(T registeredListener){
        this.listener =   registeredListener;

    }
    @Override
    public Object getInstance() {
        return listener;
    }

    @Override
    public void close() throws Exception {
        close();
    }
}
