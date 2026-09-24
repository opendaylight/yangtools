/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.model.AugmentationArchetype;
import org.opendaylight.yangtools.binding.model.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.TypeName;

/**
 * Template for {@link Augmentation} specializations.
 */
@NonNullByDefault
final class AugmentationTemplate extends InterfaceTemplate<AugmentationArchetype>
        implements ArchetypeTemplate.WithBuilder {
    private static final TypeName AUGMENTATION = TypeName.ofClass(Augmentation.class);

    AugmentationTemplate(final DataRootArchetype root, final AugmentationArchetype archetype) {
        super(root, archetype);
    }

    @Override
    public BuilderTemplate newBuilderTemplate() {
        return BuilderTemplate.of(this);
    }

    @Override
    ExtendsKeyword extendsKeyword() {
        return ExtendsKeyword.of(TypeReference.of(javaType(), AUGMENTATION, archetype.targetName(), archetype),
            extendsPartials());
    }

    @Override
    BlockBuilder contractMethods(final BlockBuilder bb) {
        return bb
            .nl()
            .frg(new ImplementedInterfaceMethod.Canonical(this))
            .nl()
            .frg(new JavaDataContainerMethods(javaType(), getters, false));
    }
}
