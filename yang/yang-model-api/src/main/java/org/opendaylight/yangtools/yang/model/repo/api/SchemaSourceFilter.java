/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/eplv10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import com.google.common.util.concurrent.ListenableFuture;

public interface SchemaSourceFilter {
    Iterable<Class<? extends SchemaSourceRepresentation>> supportedRepresentations();
    ListenableFuture<Boolean> apply(SchemaSourceRepresentation schemaSource);
}
