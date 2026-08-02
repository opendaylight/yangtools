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
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;

/**
 * An {@link Archetype} for a {@link DataContainer}.
 *
 * @since 16.0.0
 */
@Beta
@NonNullByDefault
public sealed interface DataContainerArchetype extends Archetype
        permits AugmentableArchetype, AugmentationArchetype, DataRootArchetype, GroupingArchetype,
                NotificationBodyArchetype, YangDataArchetype {
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
     * {@return the list of interfaces the interface extends}
     */
    // FIXME: extendsTypes() returning a list of DataContainerArchetype.Extentendable
    List<Type> getImplements();

    // List<TypeObjectArchetype<?>> typeObjects();

    /**
     * {@return the list of methods the interface defines}
     */
    // FIXME: yes, these result in methods being generated, but they are somewhat subtle, as they also imply constants
    //        for builders, etc. Most notably, KeyArchetype is presenting a subset of these defined in its corresponding
    //        EntryObjectArchetype
    List<MethodSignature> getMethodDefinitions();

    // FIXME: deprecated and default to defer to typeObjects()
    @Override
    List<TypeObjectArchetype<?>> enclosedTypes();

    @Override
    String toString();
}
