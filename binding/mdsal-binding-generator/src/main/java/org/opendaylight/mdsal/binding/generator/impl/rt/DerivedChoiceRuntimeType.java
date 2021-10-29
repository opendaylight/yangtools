/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
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
public final class DerivedChoiceRuntimeType extends AbstractChoiceRuntimeType {
    private final @NonNull ChoiceRuntimeType originalType;

    public DerivedChoiceRuntimeType(final GeneratedType bindingType, final ChoiceEffectiveStatement statement,
            final List<RuntimeType> children, final List<AugmentRuntimeType> augments,
            final ChoiceRuntimeType originalType) {
        super(bindingType, statement, children, augments);
        this.originalType = requireNonNull(originalType);
    }

    @Override
    public @NonNull ChoiceRuntimeType originalType() {
        return originalType;
    }

    @Override
    public Collection<CaseRuntimeType> additionalCaseChildren() {
        final var myJavaTypes = Collections2.transform(validCaseChildren(), CaseRuntimeType::getIdentifier);
        final var result = new ArrayList<CaseRuntimeType>();
        for (var caseType : Iterables.concat(originalType.validCaseChildren(), originalType.additionalCaseChildren())) {
            if (!myJavaTypes.contains(caseType.getIdentifier())) {
                result.add(caseType);
            }
        }
        return result;
    }
}
