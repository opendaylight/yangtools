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
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.Grouping;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.GroupingArchetype;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.ri.Types;

/**
 * Template for {@link Grouping} specializations.
 */
@NonNullByDefault
final class GroupingTemplate extends InterfaceTemplate<GroupingArchetype> {
    record Builder(GroupingArchetype type, DataRootArchetype root) implements Template.Builder {
        Builder {
            requireNonNull(type);
            requireNonNull(root);
        }

        @Override
        public GroupingTemplate build() {
            return new GroupingTemplate(type, root);
        }
    }

    private static final ConcreteType GROUPING = Types.typeForClass(Grouping.class);

    private GroupingTemplate(final GroupingArchetype archetype, final DataRootArchetype root) {
        super(archetype, root);
    }

    @Override
    @Nullable GroupingArchetype builderTarget() {
        return null;
    }

    @Override
    Iterator<? extends Type> extendsTypes() {
        return Iterators.concat(Iterators.singletonIterator(GROUPING), super.extendsTypes());
    }
}
