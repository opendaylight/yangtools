/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import javax.annotation.Nullable;

/**
 * Interface describing YANG 'notification' statement. The notification
 * statement is used to define a NETCONF notification.
 */
public interface NotificationDefinition extends SchemaNode, DataNodeContainer, AugmentationTarget, CopyableNode {

    /**
     * All implementations should override this method.
     * The default definition of this method is used only in YANG 1.0 (RFC6020) implementation of
     * NotificationDefinition which does not support any constraints.
     * YANG notification statement has been changed in YANG 1.1 (RFC7950) and now allows must constraints.
     *
     * @return the constraints associated with this NotificationDefinition
     */
     // FIXME: version 2.0.0: make this method non-default
    @Nullable default ConstraintDefinition getConstraints() {
        return null;
    }
}
