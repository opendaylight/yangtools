/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import com.google.common.collect.Iterators;
import java.util.Iterator;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.ChildOf;
import org.opendaylight.yangtools.binding.ContainerObject;
import org.opendaylight.yangtools.binding.model.ContainerObjectArchetype;
import org.opendaylight.yangtools.binding.model.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.Type;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.TypeRef;

/**
 * Template for a {@link ChildOf} interface.
 */
@NonNullByDefault
final class ContainerObjectTemplate extends InterfaceTemplate<ContainerObjectArchetype>
        implements ArchetypeTemplate.WithBuilder {
    private static final ConcreteType CONTAINER_OBJECT = ConcreteType.ofClass(ContainerObject.class);

    ContainerObjectTemplate(final DataRootArchetype root, final ContainerObjectArchetype archetype) {
        super(root, archetype, DataContainerContract.JAVA, true);
    }

    @Override
    public BuilderTemplate newBuilderTemplate() {
        return BuilderTemplate.of(this);
    }

    @Override
    Iterator<? extends Type> extendsTypes() {
        return Iterators.concat(
            Iterators.singletonIterator(
                ParameterizedType.of(CONTAINER_OBJECT, TypeRef.of(archetype.parentName()), archetype)),
            super.extendsTypes());
    }

    @Override
    QNameConstant constants() {
        return new QNameConstant.InInterface(this, archetype.statement().argument());
    }
}
