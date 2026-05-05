/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.KeyArchetype;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;

/**
 * A run-time implementation of {@link KeyArchetype}.
 */
@NonNullByDefault
record RuntimeKeyArchetype(
        JavaTypeName name,
        JavaTypeName entryObject,
        KeyEffectiveStatement statement,
        List<Type> fields) implements KeyArchetype {
    RuntimeKeyArchetype {
        requireNonNull(name);
        requireNonNull(entryObject);
        requireNonNull(statement);
        fields = List.copyOf(fields);
        verify(fields.size() == statement.argument().size());
    }
}
