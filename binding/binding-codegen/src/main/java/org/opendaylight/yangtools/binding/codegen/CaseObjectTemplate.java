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
import org.opendaylight.yangtools.binding.model.api.CaseObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.TypeRef;

/**
 * Template for a (non-existing) {@code CaseObject}.
 */
@NonNullByDefault
final class CaseObjectTemplate extends AugmentableTemplate<CaseObjectArchetype>
        implements BuilderTemplate.TargetTemplate {

    static final ConcreteType DATA_OBJECT = ConcreteType.ofClass(DataObject.class);

    CaseObjectTemplate(final DataRootArchetype root, final CaseObjectArchetype archetype) {
        super(root, archetype);
    }

    @Override
    public CaseObjectTemplate self() {
        return this;
    }

    @Override
    Iterator<? extends Type> extendsTypes() {
        return Iterators.concat(
            Iterators.forArray(TypeRef.of(archetype.parentName()), DATA_OBJECT,
                extendsAugmentable(), extendsJavaDataContainer()),
            super.extendsTypes());
    }

    @Override
    QNameConstant constants() {
        return new QNameConstant.InInterface(this, archetype.statement().argument());
    }
}
