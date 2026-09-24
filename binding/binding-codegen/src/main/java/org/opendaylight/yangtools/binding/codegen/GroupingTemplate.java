/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.Grouping;
import org.opendaylight.yangtools.binding.model.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.GroupingArchetype;
import org.opendaylight.yangtools.binding.model.TypeName;

/**
 * Template for {@link Grouping} specializations.
 */
@NonNullByDefault
final class GroupingTemplate extends InterfaceTemplate<GroupingArchetype> {
    private static final TypeName GROUPING = TypeName.ofClass(Grouping.class);

    GroupingTemplate(final DataRootArchetype root, final GroupingArchetype archetype) {
        super(root, archetype);
    }

    @Override
    ExtendsKeyword extendsKeyword() {
        return ExtendsKeyword.of(TypeReference.of(javaType(), GROUPING), extendsPartials());
    }
}
