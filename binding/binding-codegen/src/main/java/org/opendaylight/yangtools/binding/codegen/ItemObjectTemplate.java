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
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.ItemObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.Type;

/**
 * Template for {@link DataObject} specializations generated for {@link list} statements without a {@code key}
 * statement.
 */
@NonNullByDefault
final class ItemObjectTemplate extends ChildOfTemplate<ItemObjectArchetype> implements BuilderTemplate.TargetTemplate {
    ItemObjectTemplate(final DataRootArchetype root, final ItemObjectArchetype archetype) {
        super(root, archetype);
    }

    @Override
    public ItemObjectTemplate self() {
        return this;
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
