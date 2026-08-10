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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.model.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.EntryObjectArchetype;
import org.opendaylight.yangtools.binding.model.KeyArchetype;
import org.opendaylight.yangtools.binding.model.Type;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.TypeRef;

/**
 * Template for {@link EntryObject} specializations.
 */
@NonNullByDefault
final class EntryObjectTemplate extends InterfaceTemplate<EntryObjectArchetype>
        implements ArchetypeTemplate.WithBuilder {
    private static final ConcreteType ENTRY_OBJECT = ConcreteType.ofClass(EntryObject.class);

    final KeyArchetype key;

    EntryObjectTemplate(final DataRootArchetype root, final EntryObjectArchetype archetype, final KeyArchetype key) {
        super(root, archetype, DataContainerContract.JAVA, true);
        this.key = requireNonNull(key);
    }

    @Override
    public BuilderTemplate newBuilderTemplate() {
        return BuilderTemplate.of(this);
    }

    @Override
    Iterator<? extends Type> extendsTypes() {
        return Iterators.concat(
            Iterators.singletonIterator(
                ParameterizedType.of(ENTRY_OBJECT, TypeRef.of(archetype.parentName()), archetype, key)),
            super.extendsTypes());
    }

    @Override
    QNameConstant constants() {
        return new QNameConstant.InInterface(this, archetype.statement().argument());
    }
}
