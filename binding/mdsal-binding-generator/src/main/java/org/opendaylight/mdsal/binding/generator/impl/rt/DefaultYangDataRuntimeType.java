/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import com.google.common.annotations.Beta;
import java.util.List;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.YangDataRuntimeType;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;

@Beta
public final class DefaultYangDataRuntimeType extends AbstractCompositeRuntimeType<YangDataEffectiveStatement>
        implements YangDataRuntimeType {
    public DefaultYangDataRuntimeType(final GeneratedType bindingType, final YangDataEffectiveStatement statement,
            final List<RuntimeType> children) {
        super(bindingType, statement, children);
    }
}
