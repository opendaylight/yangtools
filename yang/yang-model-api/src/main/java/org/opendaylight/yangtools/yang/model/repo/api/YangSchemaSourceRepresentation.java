/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.repo.api;

import javax.annotation.Nonnull;

/**
 * A YANG {@link SchemaSourceRepresentation}.
 */
public interface YangSchemaSourceRepresentation extends SchemaSourceRepresentation {
    /**
     * Return the concrete representation type.
     *
     * @return The type of representation.
     */
    @Nonnull
    @Override
    Class<? extends YangSchemaSourceRepresentation> getType();
}
