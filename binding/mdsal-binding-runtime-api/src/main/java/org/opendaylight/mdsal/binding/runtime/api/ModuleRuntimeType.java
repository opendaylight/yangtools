/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.YangDataName;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;

/**
 * A {@link RuntimeType} associated with a {@code module} statement.
 */
@Beta
public interface ModuleRuntimeType extends CompositeRuntimeType {
    @Override
    ModuleEffectiveStatement statement();

    /**
     * Look up a child {@link YangDataRuntimeType} by its {@link YangDataName}.
     *
     * @param templateName Yang Data template name
     * @return YangDataRuntimeType or {@code null} if not found
     */
    @Nullable YangDataRuntimeType yangDataChild(YangDataName templateName);
}
