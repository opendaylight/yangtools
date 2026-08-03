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
import org.opendaylight.yangtools.binding.model.api.CaseObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.TypeRef;

/**
 * Template for a (non-existing) {@code CaseObject}.
 */
@NonNullByDefault
final class CaseObjectTemplate extends AugmentableTemplate<CaseObjectArchetype> {
    record Builder(CaseObjectArchetype type, DataRootArchetype root) implements Template.Builder {
        Builder {
            requireNonNull(type);
            requireNonNull(root);
        }

        @Override
        public CaseObjectTemplate build() {
            return new CaseObjectTemplate(type, root);
        }
    }

    private CaseObjectTemplate(final CaseObjectArchetype archetype, final DataRootArchetype root) {
        super(archetype, root);
    }

    @Override
    @NonNull CaseObjectArchetype builderTarget() {
        return archetype;
    }

    @Override
    Iterator<? extends Type> extendsTypes() {
        return Iterators.concat(
            Iterators.forArray(TypeRef.of(archetype.parentName()), NotificationTemplate.DATA_OBJECT,
                extendsAugmentable(), extendsJavaDataContainer()),
            super.extendsTypes());
    }

    @Override
    QNameConstant constants() {
        return new QNameConstant.InInterface(this, archetype.statement().argument());
    }
}
