/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.common;

public enum ResourceMediaTypes {

    XML("application/yang.api+xml"),
    JSON("application/yang.api+json");

    private final String mediaType;

    ResourceMediaTypes(String mediaType){
        this.mediaType =  mediaType;
    }
    public static ResourceMediaTypes fromString(String text) {
        if (text != null) {
            for (ResourceMediaTypes resourceMediaType : ResourceMediaTypes.values()) {
                if (text.equalsIgnoreCase(resourceMediaType.mediaType)) {
                    return resourceMediaType;
                }
            }
        }
        return null;
    }

    public String getMediaType() {
        return mediaType;
    }
}
