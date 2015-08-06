/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.repo;

import com.google.common.base.Optional;

/**
 * Provider of text stream representation of YANG Modules
 *
 * Provider is holder / user implemented service, which
 * may be able to retrieve representation of YANG sources
 * for other components.
 *
 * @param <F> Format in which YANG source is represented.
 * @deprecated Repalced With {@link AdvancedSchemaSourceProvider}
 */
@Deprecated
public interface SchemaSourceProvider<F> {

    /**
     * Returns source for supplied YANG module identifier and revision.
     *
     * @param moduleName module name
     * @param revision revision of module
     * @return source representation if supplied YANG module is available
     *  {@link Optional#absent()} otherwise.
     *  @deprecated Use {@link AdvancedSchemaSourceProvider#getSchemaSource(SourceIdentifier)}
     *     instead.
     */
    @Deprecated
    Optional<F> getSchemaSource(String moduleName, Optional<String> revision);

}
