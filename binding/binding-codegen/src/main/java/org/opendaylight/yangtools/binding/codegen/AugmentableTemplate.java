/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.model.api.AugmentableArchetype;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;

/**
 * Base class for code generators based on {@link AugmentableArchetype}.
 */
// TODO: split this class up into reusable components, i.e. use composition instead of inheritance
@NonNullByDefault
abstract sealed class AugmentableTemplate<T extends AugmentableArchetype> extends InterfaceTemplate<T>
        permits CaseObjectTemplate, ContainerObjectTemplate, EntryObjectTemplate, InstanceNotificationTemplate,
                ItemObjectTemplate, KeyedListNotificationTemplate, NotificationTemplate, RpcInputTemplate,
                RpcOutputTemplate {
    static final ConcreteType AUGMENTABLE = ConcreteType.ofClass(Augmentable.class);

    AugmentableTemplate(final DataRootArchetype root, final T archetype) {
        super(root, archetype, DataContainerContract.JAVA, true);
    }

    final Type extendsAugmentable() {
        return ParameterizedType.of(AUGMENTABLE, archetype);
    }
}
