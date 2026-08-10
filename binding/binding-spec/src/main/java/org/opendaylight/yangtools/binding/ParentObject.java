/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import org.opendaylight.yangtools.binding.lib.JavaDataContainer;

/**
 * A concrete {@link DataContainer} contract that is implemented by means of {@link JavaDataContainer}.
 *
 * @param <T> concrete {@link ParentObject} contract
 */
public sealed interface ParentObject<T extends ParentObject<T>>
        extends BindingObject, DataContainer, JavaDataContainer<T>
        permits Augmentation, CaseObject, ContainerObject, EntryObject, InstanceNotification, ItemObject, Notification,
                RpcInput, RpcOutput {
    @Override
    Class<T> implementedInterface();
}
