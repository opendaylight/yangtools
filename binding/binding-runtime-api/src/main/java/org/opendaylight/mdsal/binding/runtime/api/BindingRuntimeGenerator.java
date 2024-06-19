/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;

/**
 * Runtime binding type generator. Generated types contain metadata required for integrating compile-time generated
 * code with YANG-modeled schema.
 */
@Beta
@NonNullByDefault
public interface BindingRuntimeGenerator {
    /**
     * Generate Type mapping from specified {@link EffectiveModelContext} for the specified subset of modules.
     * The EffectiveModelContext MUST contain all of the sub modules otherwise the there is no guarantee that result
     * List of Generated Types will contain correct Generated Types.
     *
     * @param modelContext effective model context
     * @return Generated type mapping.
     */
    BindingRuntimeTypes generateTypeMapping(EffectiveModelContext modelContext);
}
