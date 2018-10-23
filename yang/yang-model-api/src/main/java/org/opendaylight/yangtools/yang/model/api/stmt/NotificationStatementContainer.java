/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Marker interface for statements which may contain a 'notification' statement, as defined in RFC7950. There is
 * a significant difference RFC6020 (YANG 1) and RFC7590 (YANG 1.1) in which statements sport this feature.
 *
 * @deprecated Use {@link NotificationStatementAwareDeclaredStatement} instead.
 */
@Deprecated
public interface NotificationStatementContainer {
    /**
     * Return collection of {@link NotificationStatement}. For RFC6020, this method returns an empty collection for
     * statements which do not allow for must statement children.
     *
     * @return collection of notification statements
     */
    @NonNull Collection<? extends NotificationStatement> getNotifications();
}
