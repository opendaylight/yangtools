/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.spi;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;

/**
 * An SPI-level interface to find the schema source for a particular YANG module, as packaged in the final artifact.
 * The module must be part of the current resolution context.
 */
@Beta
@FunctionalInterface
public interface ModuleResourceResolver {
    /**
     * Find the path of the packaged resource which corresponds to the specified module in the specified representation.
     *
     * @param module Requested module
     * @param representation Requested representation
     * @return Path to packaged resource
     * @throws NullPointerException if any argument is null
     * @throws IllegalArgumentException if the requested representation is not supported by this resolver
     */
    Optional<String> findModuleResourcePath(Module module, Class<? extends SchemaSourceRepresentation> representation);
}
