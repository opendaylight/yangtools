/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * A {@link Type} representing a Java class generated for a YANG statement.
 *
 * @since 15.0.0
 */
public sealed interface Archetype extends Type
        permits ChoiceInArchetype, DataContainerArchetype, FeatureArchetype, IdentityArchetype, OperationArchetype,
                KeyArchetype, OpaqueObjectArchetype, TypeObjectArchetype {
    /**
     * {@return the {@link EffectiveStatement} from which the class was generated}
     *
     * @since 16.0.0
     */
    @NonNull EffectiveStatement<?, ?> statement();
}
