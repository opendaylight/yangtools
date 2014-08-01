/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/eplv10.html
 */
package org.opendaylight.yangtools.yang.model.repo.spi;

import org.opendaylight.yangtools.concepts.ListenerRegistration;

/**
 * Registration of a SchemaSourceListener.
 */
public interface SchemaListenerRegistration extends ListenerRegistration<SchemaSourceListener> {

}
