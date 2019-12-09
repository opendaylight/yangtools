/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Marker interface for YANG-defined instance {@code notification}s. A concrete InstanceNotification and its
 * implementations may choose to also extend/implement the {@link EventInstantAware} interface. In case they do,
 * {@link EventInstantAware#eventInstant()} returns the time when this notification was generated.
 *
 * @param <N> Concrete notification type
 * @param <T> Parent data tree instance type
 */
@Beta
public interface InstanceNotification<N extends InstanceNotification<N, T>, T extends DataObject>
        extends BaseNotification {

    @Override
    @NonNull Class<N> implementedInterface();
}
