/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.TypeObject;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;

/**
 * An {@link Archetype} for one of the four {@link TypeObject} specializations.
 *
 * @param <T> the {@link TypeObject} specialization
 * @since 16.0.0
 */
@Beta
public sealed interface TypeObjectArchetype<T extends TypeObject>
    // FIXME: this is not entirely accurate: we want to have:
    //        - TypeEffectiveStatement statement()
    //        - TypeEffectiveStatement.MandatoryIn<?, ?> definingStatement();
    extends Archetype.WithStatement<TypeEffectiveStatement.@NonNull MandatoryIn<?, ?>>
    permits EnumTypeObjectArchetype, GeneratedTransferObject {
    // nothing else
}
