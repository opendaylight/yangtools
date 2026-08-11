/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.Augmentable;

/**
 * An {@link DataContainerArchetype} for an {@link Augmentable} interface.
 */
@NonNullByDefault
public sealed interface AugmentableArchetype extends DataContainerArchetype
    permits CaseObjectArchetype, ChildOfArchetype, DataContainerArchetype.OfNotification, RpcInputArchetype,
            RpcOutputArchetype {
    /**
     * {@return the set of {@link AugmentationArchetype}s targetting this archetype}
     */
    List<AugmentationArchetype> augmentations();
}
