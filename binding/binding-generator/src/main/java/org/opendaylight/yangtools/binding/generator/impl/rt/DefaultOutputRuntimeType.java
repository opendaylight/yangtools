/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.rt;

import java.util.List;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.OutputRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;

public final class DefaultOutputRuntimeType extends AbstractAugmentableRuntimeType<OutputEffectiveStatement>
        implements OutputRuntimeType {
    public DefaultOutputRuntimeType(final GeneratedType bindingType, final OutputEffectiveStatement statement,
            final List<RuntimeType> children, final List<AugmentRuntimeType> augments) {
        super(bindingType, statement, children, augments);
    }
}
