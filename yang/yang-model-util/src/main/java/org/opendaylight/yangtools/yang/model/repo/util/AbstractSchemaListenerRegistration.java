/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.util;

import org.opendaylight.yangtools.concepts.AbstractListenerRegistration;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaListenerRegistration;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceListener;

public abstract class AbstractSchemaListenerRegistration extends AbstractListenerRegistration<SchemaSourceListener>
        implements SchemaListenerRegistration {
    protected AbstractSchemaListenerRegistration(final SchemaSourceListener listener) {
        super(listener);
    }
}
