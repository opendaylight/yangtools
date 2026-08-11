/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

/**
 * A {@link DataContainer} shared between {@link InstanceNotification}s defined in a {@link Grouping}. A specialization
 * of this interface is generated at the definition site and then every instantiation gets a separate
 * {@link InstanceNotification} interface, all inheriting that specialization.
 *
 * <p>The contract is similar to that of {@link Grouping}, but we only allow single inheritence of this interface and
 * therefore we capture the concrete instantiation.
 *
 * @param <B> Concrete {@link NotificationBody} type
 */
public non-sealed interface NotificationBody<B extends NotificationBody<B>> extends BindingObject, DataContainer {
    @Override
    Class<? extends B> implementedInterface();
}
