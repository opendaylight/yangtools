/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.model.api;

import com.google.common.annotations.Beta;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Node which can contain action nodes.
 */
@Beta
public interface ActionNodeContainer {
    /**
     * Return the set of actions.
     *
     * @return set of action nodes
     */
    @NonNull Set<ActionDefinition> getActions();
}
