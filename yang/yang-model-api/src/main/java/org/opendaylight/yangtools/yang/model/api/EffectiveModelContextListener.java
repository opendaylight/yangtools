/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import com.google.common.annotations.Beta;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.EventListener;

/**
 * Interface for listeners interested in updates of an EffectiveModelContext.
 */
@Beta
public interface EffectiveModelContextListener extends EventListener {
    /**
     * Invoked when the model context changes.
     *
     * @param newModelContext New model context being installed
     */
    void onEffectiveModelContextUpdated(@NonNull EffectiveModelContext newModelContext);
}
