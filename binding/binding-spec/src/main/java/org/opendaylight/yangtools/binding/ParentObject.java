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
 * A {@link DataContainer} that is
 */
public sealed interface ParentObject<T extends ParentObject<T> & G> extends DataContainer, JavaDataContainer<T>
        permits Augmentable, Augmentation, CaseObject, ChildOf, DataRoot, RpcInput, RpcOutput {
    @Override
    Class<T> implementedInterface();
}
