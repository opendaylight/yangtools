/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.util.Collection;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.InputEffectiveStatement;

/**
 * An {@link InputSchemaNode} defines inputs of an {@link RpcDefinition} or an {@link ActionDefinition}.
 */
public interface InputSchemaNode extends ContainerLike, AugmentationTarget.Mixin<InputEffectiveStatement>,
        MustConstraintAware.Mixin<InputEffectiveStatement>, WhenConditionAware.Mixin<InputEffectiveStatement> {
    @Override
    default QName getQName() {
        return asEffectiveStatement().argument();
    }

    @Override
    default Collection<? extends ActionDefinition> getActions() {
        return List.of();
    }

    @Override
    default Collection<? extends NotificationDefinition> getNotifications() {
        return List.of();
    }
}
