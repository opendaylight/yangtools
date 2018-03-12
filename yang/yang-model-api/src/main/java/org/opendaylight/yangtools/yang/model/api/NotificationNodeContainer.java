/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;

public interface NotificationNodeContainer {
    /**
     * Return the set of notifications in this container, keyed by QName. RFC7950 specifies that
     * {@link AugmentationSchemaNode}s, {@link GroupingDefinition}s, {@link ListSchemaNode}s and
     * {@link ContainerSchemaNode}s can also contain {@link NotificationDefinition}s.
     *
     * @return set of notification nodes
     * @deprecated Use {@link #findNotification(QName)} or {@link #streamNotifications()}} instead.
     */
    @Deprecated
    @NonNull Set<NotificationDefinition> getNotifications();

    /**
     * Finds an notification definition matching specified QName.
     *
     * @return A {@code NotificationDefinition} if a match is found.
     */
    default @NonNull Optional<NotificationDefinition> findNotification(final QName qname) {
        return streamNotifications().filter(ext -> qname.equals(ext.getQName())).findFirst();
    }

    /**
     * Returns a stream of notification definitions defined in YANG modules in this context.
     *
     * @return A stream of {@code NotificationDefinition} instances which represents nodes defined via
     *         {@code notification} YANG keyword
     */
    // FIXME: 4.0.0: make this method non-default
    default @NonNull Stream<NotificationDefinition> streamNotifications() {
        return getNotifications().stream();
    }
}
