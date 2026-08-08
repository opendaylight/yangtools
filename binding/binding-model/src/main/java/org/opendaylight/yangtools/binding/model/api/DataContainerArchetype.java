/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import com.google.common.annotations.Beta;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.BaseNotification;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.binding.lib.ImplementedInterface;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;

/**
 * An {@link Archetype} for a {@link DataContainer}. Most subclasses of {@link DataContainerArchetype} represent
 * a concrete {@link ImplementedInterface} contract, but some represent interfaces that can be reused for multiple
 * contracts.
 *
 *
 * @since 16.0.0
 */
@Beta
@NonNullByDefault
public sealed interface DataContainerArchetype extends Archetype
        permits AugmentableArchetype, AugmentationArchetype, DataContainerArchetype.Partial, DataRootArchetype,
                YangDataArchetype {
    /**
     * A {@link DataContainerArchetype} that can be a part of another {@link DataContainerArchetype}. Interfaces
     * generated from such archetypes do not provide an implementation of
     * {@link ImplementedInterface#implementedInterface()}.
     */
    sealed interface Partial extends DataContainerArchetype permits GroupingArchetype, NotificationBodyArchetype {
        // just a marker
    }

    /**
     * An {@link DataContainerArchetype} for a {@code notification} statement. Implementations of this archetype result
     * in a subclass of {@link BaseNotification} and the hierarchy of this class reflects that. These are not to be
     * confused with {@link NotificationBodyArchetype}.
     */
    sealed interface OfNotification extends AugmentableArchetype
            permits InstanceNotificationArchetype, KeyedListNotificationArchetype, NotificationArchetype {
        @Override
        NotificationEffectiveStatement statement();
    }

    /**
     * {@return the list of {@link Partial}s the interface represented by this archetypes {@code extends}}
     */
    List<Partial> partials();

    /**
     * {@return the {@link TypeObjectArchetype}s for inner classes}
     */
    List<TypeObjectArchetype<?>> typeObjects();

    /**
     * {@return the list of {@link GetterMethod}s the interface defines}
     */
    List<GetterMethod> getters();

    @Override
    String toString();
}
