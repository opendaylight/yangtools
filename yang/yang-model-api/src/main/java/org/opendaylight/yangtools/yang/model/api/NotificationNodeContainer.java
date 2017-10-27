/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.model.api;

import java.util.Set;
import javax.annotation.Nonnull;

public interface NotificationNodeContainer {

    /**
     * Return the set of notifications in this container, keyed by QName. RFC7950 specifies that
     * {@link AugmentationSchemaNode}s, {@link GroupingDefinition}s, {@link ListSchemaNode}s and
     * {@link ContainerSchemaNode}s can also contain {@link NotificationDefinition}s.
      *
     * @return set of notification nodes
     */
    @Nonnull Set<NotificationDefinition> getNotifications();
}
