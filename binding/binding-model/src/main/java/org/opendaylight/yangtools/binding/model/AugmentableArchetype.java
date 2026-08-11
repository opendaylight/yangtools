/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.binding.Augmentable;

/**
 * An {@link DataContainerArchetype} for an {@link Augmentable} interface.
 */
@Beta
public sealed interface AugmentableArchetype extends DataContainerArchetype
    permits CaseObjectArchetype, ChildOfArchetype, DataContainerArchetype.OfNotification, NotificationBodyArchetype,
            RpcInputArchetype, RpcOutputArchetype {
    // nothing else
}
