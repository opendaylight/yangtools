/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.Type;

/**
 * A {@link Type} that is valid as being a part of the return type of a method generated for a {@link GetterMethod}.
 *
 * @since 16.0.0
 */
@Beta
public sealed interface ReturnType extends Type
    permits ConcreteType, ChoiceInArchetype, ContainerObjectArchetype, EntryObjectArchetype, IdentityArchetype,
            ItemObjectArchetype, OpaqueObjectArchetype, TypeObjectArchetype {
    // nothing else
}
