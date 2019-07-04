/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;

public interface NotificationNodeContainer {
    /**
     * Return the set of notifications in this container, keyed by QName. RFC7950 specifies that
     * {@link AugmentationSchemaNode}s, {@link GroupingDefinition}s, {@link ListSchemaNode}s and
     * {@link ContainerSchemaNode}s can also contain {@link NotificationDefinition}s.
      *
     * @return set of notification nodes
     */
    @NonNull Set<NotificationDefinition> getNotifications();

    /**
     * Find a notification based on its QName. Default implementation searches the set returned by
     * {@link #getNotifications()}.
     *
     * @param qname Notification QName
     * @return Notification definition, if found
     * @throws NullPointerException if qname is null
     */
    default Optional<NotificationDefinition> findNotification(final QName qname) {
        requireNonNull(qname);
        for (NotificationDefinition notif : getNotifications()) {
            if (qname.equals(notif.getQName())) {
                return Optional.of(notif);
            }
        }
        return Optional.empty();
    }
}
