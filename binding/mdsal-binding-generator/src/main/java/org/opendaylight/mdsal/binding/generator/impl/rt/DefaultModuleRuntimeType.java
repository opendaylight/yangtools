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
import java.util.List;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.runtime.api.ModuleRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.YangDataRuntimeType;
import org.opendaylight.yangtools.yang.common.YangDataName;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;

@Beta
public final class DefaultModuleRuntimeType extends AbstractCompositeRuntimeType<ModuleEffectiveStatement>
        implements ModuleRuntimeType {
    private final ImmutableList<YangDataRuntimeType> yangDataChildren;

    public DefaultModuleRuntimeType(final GeneratedType bindingType, final ModuleEffectiveStatement statement,
            final List<RuntimeType> children) {
        super(bindingType, statement, children);
        yangDataChildren = children.stream()
            .filter(YangDataRuntimeType.class::isInstance)
            .map(YangDataRuntimeType.class::cast)
            .collect(ImmutableList.toImmutableList());
    }

    @Override
    public YangDataRuntimeType yangDataChild(final YangDataName templateName) {
        if (statement().localQNameModule().equals(templateName.module())) {
            final var name = templateName.name();
            for (var child : yangDataChildren) {
                if (name.equals(child.statement().argument())) {
                    return child;
                }
            }
        }
        return null;
    }
}
