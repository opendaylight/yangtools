/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.InstanceNotification;
import org.opendaylight.yangtools.binding.model.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.InstanceNotificationArchetype;
import org.opendaylight.yangtools.binding.model.TypeName;

/**
 * Template for a {@link InstanceNotificationArchetype}.
 */
@NonNullByDefault
final class InstanceNotificationTemplate extends InterfaceTemplate<InstanceNotificationArchetype>
        implements ArchetypeTemplate.WithBuilder {
    private static final TypeName INSTANCE_NOTIFICATION = TypeName.ofClass(InstanceNotification.class);

    InstanceNotificationTemplate(final DataRootArchetype root, final InstanceNotificationArchetype archetype) {
        super(root, archetype);
    }

    @Override
    public BuilderTemplate newBuilderTemplate() {
        return BuilderTemplate.of(this);
    }

    @Override
    ExtendsKeyword extendsKeyword() {
        return ExtendsKeyword.of(typeRefOf(INSTANCE_NOTIFICATION, archetype.name(), archetype.parentName()),
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
