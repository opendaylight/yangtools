/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator;

import java.util.Collection;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;

/**
 * Transform Schema Context to Generated types.
 */
public interface BindingGenerator {
    /**
     * Generate Types from an entire {@link EffectiveModelContext}. The method will return list of all
     * {@link GeneratedType}s that could be Generated from EffectiveModelContext.
     *
     * @param context EffectiveModelContext
     * @return List of Generated Types
     *
     * @see EffectiveModelContext
     */
    default @NonNull List<GeneratedType> generateTypes(final EffectiveModelContext context) {
        return generateTypes(context, context.getModules());
    }

    /**
     * Generate Types from an {@link EffectiveModelContext} restricted by sub set of specified Modules. The effective
     * model context <em>must</em> contain all of the sub modules otherwise the there is no guarantee that result
     * List of Generated Types will contain correct Generated Types.
     *
     * @param context EffectiveModelContext
     * @param modules Subset of Modules
     * @return List of Generated Types restricted by subset of Modules
     *
     * @see Module
     * @see EffectiveModelContext
     */
    @NonNull List<GeneratedType> generateTypes(EffectiveModelContext context, Collection<? extends Module> modules);
}
