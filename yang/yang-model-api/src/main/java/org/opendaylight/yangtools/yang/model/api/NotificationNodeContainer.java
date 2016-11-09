/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.model.api;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nonnull;

public interface NotificationNodeContainer {

    /**
     * All implementations should override this method.
     * The default definition of this method is used in YANG 1.0 (RFC6020) implementations of
     * AugmentationSchema, GroupingDefinition, ListSchemaNode and ContainerSchemaNode
     * which do not allow action statements.
     * These YANG statements have been changed in YANG 1.1 (RFC7950) and can now contain notification statements.
     *
     * The default definition is also used by implementations of ContainerSchemaNode which do not support
     * notification statements such as InputEffectiveStatementImpl and OutputEffectiveStatementImpl.
     *
     * @return set of notification nodes
     */
    @Nonnull default Set<NotificationDefinition> getNotifications() {
        return ImmutableSet.of();
    }
}
