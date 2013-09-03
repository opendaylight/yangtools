/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.spi;

import java.net.URI;
import java.util.Date;


public class YANGModuleIdentifier {
    private String name;
    private URI namespace;
    private Date revision;

    /**
     * Returns name.
     * 
     * @return string with name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns URI namespace.
     * 
     * @return URI with namespace
     */
    public URI getNamespace() {
        return this.namespace;
    }

    /**
     * Returns the revision date.
     * 
     * @return date of revision
     */
    public Date getRevision() {
        return this.revision;
    }
}
