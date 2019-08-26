/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import java.io.IOException;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.OpaqueObject;

interface AnydataBindingStreamWriter extends BindingStreamEventWriter {

    void anydataNode(String name, OpaqueObject<?> value) throws IOException;
}
