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
import org.opendaylight.yangtools.binding.CaseObject;
import org.opendaylight.yangtools.binding.model.CaseObjectArchetype;
import org.opendaylight.yangtools.binding.model.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.Type;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.TypeRef;

/**
 * Template for a (non-existing) {@code CaseObject}.
 */
@NonNullByDefault
final class CaseObjectTemplate extends InterfaceTemplate<CaseObjectArchetype> implements ArchetypeTemplate.WithBuilder {
    private static final ConcreteType CASE_OBJECT = ConcreteType.ofClass(CaseObject.class);

    CaseObjectTemplate(final DataRootArchetype root, final CaseObjectArchetype archetype) {
        super(root, archetype, DataContainerContract.JAVA, true);
    }

    @Override
    public BuilderTemplate newBuilderTemplate() {
        return BuilderTemplate.of(this);
    }

    @Override
    Iterator<? extends Type> extendsTypes() {
        final var choiceIn = TypeRef.of(archetype.choiceName());
        return Iterators.concat(
            Iterators.forArray(
                ParameterizedType.of(CASE_OBJECT, TypeRef.of(archetype.parentName()), choiceIn, archetype),
                choiceIn),
            super.extendsTypes());
    }

    @Override
    QNameConstant constants() {
        return new QNameConstant.InInterface(this, archetype.statement().argument());
    }
}
