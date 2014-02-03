/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.client.to;

import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;

public class RestDataObject implements DataObject {

    private Class<? extends DataContainer> container;

    public RestDataObject(Class<? extends DataContainer> container){
        this.container = container;
    }
    @Override
    public Class<? extends DataContainer> getImplementedInterface() {
        return this.container;
    }
}
