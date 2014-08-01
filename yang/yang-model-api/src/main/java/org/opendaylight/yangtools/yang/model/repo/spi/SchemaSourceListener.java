/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/eplv10.html
 */
package org.opendaylight.yangtools.yang.model.repo.spi;

import java.util.EventListener;

import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;

/**
 * Listener for schema source lifecycle events.
 */
public interface SchemaSourceListener extends EventListener {
    void schemaSourceAdded(SourceIdentifier identifier, Iterable<Class<? extends SchemaSourceRepresentation>> representations);
    void schemaSourceRemoved(SourceIdentifier identifier, Class<? extends SchemaSourceRepresentation> representation);
}
