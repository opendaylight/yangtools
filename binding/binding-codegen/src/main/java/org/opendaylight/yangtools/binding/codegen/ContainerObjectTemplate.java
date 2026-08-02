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
import org.opendaylight.yangtools.binding.ChildOf;
import org.opendaylight.yangtools.binding.model.api.ContainerObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.Type;

/**
 * Template for a {@link ChildOf} interface.
 */
@NonNullByDefault
final class ContainerObjectTemplate extends ChildOfTemplate<ContainerObjectArchetype> {
    record Builder(ContainerObjectArchetype type, DataRootArchetype root) implements Template.Builder {
        Builder {
            requireNonNull(type);
            requireNonNull(root);
        }

        @Override
        public ContainerObjectTemplate build() {
            return new ContainerObjectTemplate(type, root);
        }
    }

    private ContainerObjectTemplate(final ContainerObjectArchetype archetype, final DataRootArchetype root) {
        super(archetype, root);
    }

    @Override
    @NonNull ContainerObjectArchetype builderTarget() {
        return archetype;
    }

    @Override
    Iterator<? extends Type> extendsAfterChildOf() {
        return Iterators.forArray(extendsAugmentable(), extendsJavaDataContainer());
    }

    @Override
    QNameConstant constants() {
        return new QNameConstant.InInterface(this, archetype.statement().argument());
    }
}
