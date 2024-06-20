/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.runtime.api;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.lib.Augmentable;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;

/**
 * A {@link RuntimeType} associated with a {@code choice} statement. Note that unlike YANG semantics, in Binding Spec
 * semantics a type generated for a 'choice' statement is <b>does not</b> implement {@link Augmentable}.
 */
public interface ChoiceRuntimeType extends CompositeRuntimeType, DataRuntimeType {
    @Override
    ChoiceEffectiveStatement statement();

    /**
     * Returns resolved {@link CaseRuntimeType} for specified binding class name.
     *
     * @param typeName Binding class name
     * @return {@link CaseRuntimeType}, or null if absent
     * @throws NullPointerException if {@code typeName} is null
     */
    @Nullable CaseRuntimeType bindingCaseChild(JavaTypeName typeName);

    /**
     * Return all {@link CaseRuntimeType} valid at this type's statement.
     *
     * @return Valid {@link CaseRuntimeType}s
     */
    @NonNull List<CaseRuntimeType> validCaseChildren();
}
