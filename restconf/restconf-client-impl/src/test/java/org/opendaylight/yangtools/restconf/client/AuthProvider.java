/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client;

import org.opendaylight.yangtools.restconf.client.api.auth.AuthenticationHolder;

public class AuthProvider implements AuthenticationHolder {
    @Override
    public String getUserName() {
        return "admin";
    }

    @Override
    public String getPassword() {
        return "admin";
    }

    @Override
    public boolean authenticationRequired() {
        return false;
    }
}
