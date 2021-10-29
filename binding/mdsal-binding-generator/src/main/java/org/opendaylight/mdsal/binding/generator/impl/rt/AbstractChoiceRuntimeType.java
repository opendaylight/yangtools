/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import com.google.common.collect.Collections2;
import java.util.Collection;
import java.util.List;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.CaseRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ChoiceRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;

abstract class AbstractChoiceRuntimeType extends AbstractCompositeRuntimeType<ChoiceEffectiveStatement>
        implements ChoiceRuntimeType {
    AbstractChoiceRuntimeType(final GeneratedType bindingType, final ChoiceEffectiveStatement statement,
            final List<RuntimeType> children, final List<AugmentRuntimeType> augments) {
        super(bindingType, statement, children, augments);
    }

    @Override
    public final Collection<CaseRuntimeType> validCaseChildren() {
        return (Collection) Collections2.filter(schemaTreeChildren(), CaseRuntimeType.class::isInstance);
    }

    @Override
    public final CaseRuntimeType bindingCaseChild(final JavaTypeName typeName) {
        final var child = bindingChild(typeName);
        return child instanceof CaseRuntimeType ? (CaseRuntimeType) child : null;
    }
}
