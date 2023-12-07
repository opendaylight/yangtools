/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.util.EventListener;

/**
 * Interface for listeners interested in updates of the global schema context. The global schema context reflects the
 * global view of the model world, and all entities interacting at the global scale need to maintain a consistent view
 * of that world.
 *
 * @deprecated Use {@link EffectiveModelContextListener} instead.
 */
@Deprecated(since = "11.0.5", forRemoval = true)
public interface SchemaContextListener extends EventListener {
    /**
     * The global schema context is being updated.
     * @param context New global schema context
     */
    void onGlobalContextUpdated(SchemaContext context);
}
