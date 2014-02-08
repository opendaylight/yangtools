/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client.api;

public class RestMessage {

    private String message;
    private MessageType messageType;

    public RestMessage(String message,MessageType messageType){
        this.message = message;
        this.messageType = messageType;
    }
    public enum MessageType{
        XML,
        JSON;
    }
}
