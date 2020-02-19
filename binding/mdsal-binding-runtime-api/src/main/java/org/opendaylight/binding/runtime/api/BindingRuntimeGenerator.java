/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.binding.runtime.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.generator.api.BindingGenerator;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Runtime equivalent of {@link BindingGenerator}. It generates equivalent type information, but does not include
 * metadata not required at runtime, such as comments, references and similar.
 */
@Beta
@NonNullByDefault
public interface BindingRuntimeGenerator {
    /**
     * Generate Type mapping from specified {@link SchemaContext} for the specified subset of modules. The SchemaContext
     * MUST contain all of the sub modules otherwise the there is no guarantee that result List of Generated Types will
     * contain correct Generated Types.
     *
     * @param context Schema Context
     * @return Generated type mapping.
     */
    BindingRuntimeTypes generateTypeMapping(SchemaContext context);
}
