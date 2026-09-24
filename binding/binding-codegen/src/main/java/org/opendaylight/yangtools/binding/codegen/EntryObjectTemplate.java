/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.model.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.EntryObjectArchetype;
import org.opendaylight.yangtools.binding.model.KeyArchetype;
import org.opendaylight.yangtools.binding.model.TypeName;

/**
 * Template for {@link EntryObject} specializations.
 */
@NonNullByDefault
final class EntryObjectTemplate extends InterfaceTemplate<EntryObjectArchetype>
        implements ArchetypeTemplate.WithBuilder {
    private static final TypeName ENTRY_OBJECT = TypeName.ofClass(EntryObject.class);

    final KeyArchetype key;

    EntryObjectTemplate(final DataRootArchetype root, final EntryObjectArchetype archetype, final KeyArchetype key) {
        super(root, archetype);
        this.key = requireNonNull(key);
    }

    @Override
    public BuilderTemplate newBuilderTemplate() {
        return BuilderTemplate.of(this);
    }

    @Override
    ExtendsKeyword extendsKeyword() {
        return ExtendsKeyword.of(typeRefOf(ENTRY_OBJECT, archetype.parentName(), archetype.name(), key.name()),
            extendsPartials());
    }

    @Override
    QNameConstant constants() {
        return new QNameConstant.InInterface(this, archetype.statement().argument());
    }

    @Override
    BlockBuilder contractMethods(final BlockBuilder bb) {
        return bb
            .nl()
            .frg(new ImplementedInterfaceMethod.Canonical(this))
            .nl()
            .frg(new JavaDataContainerMethods(javaType(), getters, true));
    }
}
