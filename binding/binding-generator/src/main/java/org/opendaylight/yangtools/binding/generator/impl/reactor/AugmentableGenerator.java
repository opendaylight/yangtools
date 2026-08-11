/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.AugmentableArchetype;
import org.opendaylight.yangtools.binding.model.GroupingArchetype;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.runtime.api.AugmentableRuntimeType;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * A {@link DataContainerGenerator} which additionally is an {@link AugmentTargetGenerator}.
 */
// FIXME: JIRA: YANGTOOLS-1935: also NotificationBody
abstract sealed class AugmentableGenerator<S extends EffectiveStatement<?, ?>, R extends AugmentableRuntimeType>
        extends DataContainerGenerator<S, R> implements AugmentTargetGenerator
        permits AbstractNotificationGenerator, CaseGenerator, ContainerGenerator, ListGenerator,
                OperationContainerGenerator {
    @NonNullByDefault
    AugmentableGenerator(final S statement) {
        super(statement);
    }

    @NonNullByDefault
    AugmentableGenerator(final S statement, final CompositeGenerator<?, ?> parent) {
        super(statement, parent);
    }

    @Override
    abstract AugmentableArchetype createTypeImpl(TypeName typeName, @NonNull S statement,
        List<GroupingArchetype> groupings);
}
