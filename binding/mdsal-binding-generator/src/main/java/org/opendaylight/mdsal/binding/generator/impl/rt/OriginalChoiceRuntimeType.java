/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.CaseRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ChoiceRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;

@Beta
public final class OriginalChoiceRuntimeType extends AbstractChoiceRuntimeType {
    private final @NonNull ImmutableList<CaseRuntimeType> augmentedCases;

    public OriginalChoiceRuntimeType(final GeneratedType bindingType, final ChoiceEffectiveStatement statement,
            final List<RuntimeType> children, final List<AugmentRuntimeType> augments,
            final List<CaseRuntimeType> augmentedCases) {
        super(bindingType, statement, children, augments);
        this.augmentedCases = ImmutableList.copyOf(augmentedCases);
    }

    @Override
    public ChoiceRuntimeType originalType() {
        return null;
    }

    @Override
    public Collection<CaseRuntimeType> additionalCaseChildren() {
        return augmentedCases;
    }
}
