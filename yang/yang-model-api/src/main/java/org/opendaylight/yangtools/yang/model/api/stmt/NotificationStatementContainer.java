/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import javax.annotation.Nonnull;

public interface NotificationStatementContainer {

    /**
     * All implementations should override this method.
     * The default definition of this method is used only in YANG 1.0 (RFC6020) implementations of
     * AugmentStatement, ContainerStatement, GroupingStatement and ListStatement
     * which do not allow notification statements.
     * These YANG statements have been changed in YANG 1.1 (RFC 7950) and can now contain notification statements.
     *
     * @return collection of notification statements
     */
    // FIXME: version 2.0.0: make this method non-default
    @Nonnull default Collection<? extends NotificationStatement> getNotifications() {
        return ImmutableList.of();
    }
}
