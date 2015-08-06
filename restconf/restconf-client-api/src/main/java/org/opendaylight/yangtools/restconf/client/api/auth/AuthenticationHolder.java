/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.restconf.client.api.auth;

/**
 * Created by mbobak on 2/14/14.
 */
public interface AuthenticationHolder {

    RestAuthType getAuthType();
    String getUserName();
    String getPassword();
    boolean authenticationRequired();
}
