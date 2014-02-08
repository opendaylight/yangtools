/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.common;

public enum ResourceUri {
    RESTCONF("/restconf"),
    DATASTORE(RESTCONF.getPath()+"/datastore"),
    MODULES(RESTCONF.path+"/modules"),
    MODULE(MODULES.path+"/module"),
    MODULE_NAME(MODULES.path+"/names"),
    MODULE_REVISION(MODULES.path+"/revision"),
    MODULE_NAMESPACE(MODULES.path+"/namespace"),
    MODULE_FEATURE(MODULES.path+"/feature"),
    MODULE_DEVIATION(MODULES.path+"/deviation"),
    OPERATIONS(RESTCONF.path+"/operations"),
    STREAMS(RESTCONF.getPath()+"/streams"),
    STREAM(STREAMS.getPath()+"/stream"),
    VERSION(RESTCONF.path+"/version"),
    OPERATIONAL(RESTCONF.getPath()+"/operational"),
    CONFIG(RESTCONF.getPath()+"/config");

    private String path;

    ResourceUri(String path){
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public static ResourceUri fromString(String text) {
        if (text != null) {
            for (ResourceUri resourceUri : ResourceUri.values()) {
                if (text.equalsIgnoreCase(resourceUri.path)) {
                    return resourceUri;
                }
            }
        }
        return null;
    }

}
