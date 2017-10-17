/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import java.io.IOException;

/**
 * A serializer which writes DataObject to supplied stream event writer.
 */
public interface DataObjectSerializer {
    /**
     * Writes stream events representing object to supplied stream.
     *
     * @param obj
     *            Source of stream events
     * @param stream
     *            Stream to which events should be written.
     */
    void serialize(DataObject obj, BindingStreamEventWriter stream) throws IOException;
}
