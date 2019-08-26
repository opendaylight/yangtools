/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import java.io.IOException;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * A serializer which writes DataObject to supplied stream event writer.
 */
// FIXME: this interface should not be necessary
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
