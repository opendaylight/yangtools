/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.ChildOf;
import org.opendaylight.yangtools.binding.ContainerObject;
import org.opendaylight.yangtools.binding.model.ContainerObjectArchetype;
import org.opendaylight.yangtools.binding.model.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.TypeName;

/**
 * Template for a {@link ChildOf} interface.
 */
@NonNullByDefault
final class ContainerObjectTemplate extends InterfaceTemplate<ContainerObjectArchetype>
        implements ArchetypeTemplate.WithBuilder {
    private static final TypeName CONTAINER_OBJECT = TypeName.ofClass(ContainerObject.class);

    ContainerObjectTemplate(final DataRootArchetype root, final ContainerObjectArchetype archetype) {
        super(root, archetype);
    }

    @Override
    public BuilderTemplate newBuilderTemplate() {
        return BuilderTemplate.of(this);
    }

    @Override
    ExtendsKeyword extendsKeyword() {
        return ExtendsKeyword.of(TypeReference.of(javaType(), CONTAINER_OBJECT, archetype.parentName(), archetype),
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
