/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import com.google.common.collect.Iterators;
import java.util.Collections;
import java.util.Iterator;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.ChildOf;
import org.opendaylight.yangtools.binding.model.api.ChildOfArchetype;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.TypeRef;

/**
 * Base class for code generators based on {@link ChildOfArchetype}.
 */
@NonNullByDefault
abstract sealed class ChildOfTemplate<T extends ChildOfArchetype> extends AugmentableTemplate<T>
        permits EntryObjectTemplate {
    private static final ConcreteType CHILD_OF = ConcreteType.ofClass(ChildOf.class);

    ChildOfTemplate(final DataRootArchetype root, final T archetype) {
        super(root, archetype);
    }

    @Override
    final Iterator<? extends Type> extendsTypes() {
        return Iterators.concat(
            Iterators.singletonIterator(ParameterizedType.of(CHILD_OF, TypeRef.of(archetype.parentName()))),
            extendsAfterChildOf(), super.extendsTypes());
    }

    Iterator<? extends Type> extendsAfterChildOf() {
        return Collections.emptyIterator();
    }
}
