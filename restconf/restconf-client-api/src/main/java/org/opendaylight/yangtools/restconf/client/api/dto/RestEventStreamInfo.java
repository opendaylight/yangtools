/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client.api.dto;

import java.util.Date;
import org.opendaylight.yangtools.restconf.client.api.event.EventStreamInfo;

public class RestEventStreamInfo implements EventStreamInfo {

    private String identifier;

    private String description;

    private boolean replaySupported;

    private Date replayLogCreationTime;

    public String getIdentifier() {
        return this.identifier;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean isReplaySupported() {
        return this.replaySupported;
    }

    public Date getReplayLogCreationTime() {
        return this.replayLogCreationTime;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setReplaySupported(boolean replaySupported) {
        this.replaySupported = replaySupported;
    }

    public void setReplayLogCreationTime(Date replayLogCreationTime) {
        this.replayLogCreationTime = replayLogCreationTime;
    }

}
