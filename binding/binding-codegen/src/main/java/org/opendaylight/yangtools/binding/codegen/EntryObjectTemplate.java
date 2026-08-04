/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Iterators;
import java.util.Iterator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.EntryObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.TypeRef;

/**
 * Template for {@link EntryObject} specializations.
 */
@NonNullByDefault
final class EntryObjectTemplate extends ChildOfTemplate<EntryObjectArchetype> {
    record Builder(EntryObjectArchetype type, DataRootArchetype root) implements Template.Builder {
        Builder {
            requireNonNull(type);
            requireNonNull(root);
        }

        @Override
        public EntryObjectTemplate build() {
            return new EntryObjectTemplate(type, root);
        }
    }

    private static final ConcreteType ENTRY_OBJECT = ConcreteType.ofClass(EntryObject.class);

    private EntryObjectTemplate(final EntryObjectArchetype archetype, final DataRootArchetype root) {
        super(archetype, root);
    }

    @Override
    @NonNull EntryObjectArchetype builderTarget() {
        return archetype;
    }

    @Override
    Iterator<? extends Type> extendsAfterChildOf() {
        return Iterators.singletonIterator(
            ParameterizedType.of(ENTRY_OBJECT, archetype, TypeRef.of(archetype.keyName())));
    }

    @Override
    QNameConstant constants() {
        return new QNameConstant.InInterface(this, archetype.statement().argument());
    }
}
