/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client.api.event;

import java.util.Date;

import org.opendaylight.yangtools.concepts.Identifiable;

public interface EventStreamInfo extends Identifiable<String> {

    /**
     * 
     * @return name of event stream.
     */
    @Override
    String getIdentifier();

    String getDescription();

    boolean isReplaySupported();

    Date getReplayLogCreationTime();
}
