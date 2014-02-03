/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client.api.event;

public class RestModule {
    private String name;
    private String revision;
    private String namespace;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getRevision() {
        return revision;
    }
    public void setRevision(String revision) {
        this.revision = revision;
    }
    public String getNamespace() {
        return namespace;
    }
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

}
