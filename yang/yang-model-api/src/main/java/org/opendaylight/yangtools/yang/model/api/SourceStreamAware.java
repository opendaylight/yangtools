/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

/**
 * Allows access to module source.
 *
 * @deprecated This interface is a violation of the effective model contract. To look up the source of a particular
 *             module use a {@link org.opendaylight.yangtools.yang.model.repo.api.SchemaRepository} or a similar
 *             lookup table.
 */
@Deprecated
public interface SourceStreamAware {

    /**
     * Get descriptive source path (usually file path) from which this module was parsed.
     */
    String getModuleSourcePath();
}
