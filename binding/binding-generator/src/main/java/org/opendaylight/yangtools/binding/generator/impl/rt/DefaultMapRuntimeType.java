/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.rt;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.model.EntryObjectArchetype;
import org.opendaylight.yangtools.binding.model.KeyArchetype;
import org.opendaylight.yangtools.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.ListRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;

public final class DefaultMapRuntimeType extends AbstractAugmentableRuntimeType<ListEffectiveStatement>
        implements ListRuntimeType.WithKey {
    private final @NonNull KeyArchetype keyType;

    public DefaultMapRuntimeType(final EntryObjectArchetype bindingType, final ListEffectiveStatement statement,
            final List<RuntimeType> children, final List<AugmentRuntimeType> augments, final KeyArchetype keyType) {
        super(bindingType, statement, children, augments);
        this.keyType = requireNonNull(keyType);
    }

    @Override
    public EntryObjectArchetype javaType() {
        return (EntryObjectArchetype) super.javaType();
    }

    @Override
    public KeyArchetype keyType() {
        return keyType;
    }
}
