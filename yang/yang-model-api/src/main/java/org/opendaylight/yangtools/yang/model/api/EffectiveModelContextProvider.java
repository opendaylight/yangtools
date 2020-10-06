/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A static provider of an {@link EffectiveModelContext}.
 *
 * @author Robert Varga
 */
@Beta
public interface EffectiveModelContextProvider {
    /**
     * Return the {@link EffectiveModelContext} attached to this object.
     *
     * @return An EffectiveModelContext instance.
     * @throws IllegalStateException if the context is not available.
     */
    @NonNull EffectiveModelContext getEffectiveModelContext();
}
