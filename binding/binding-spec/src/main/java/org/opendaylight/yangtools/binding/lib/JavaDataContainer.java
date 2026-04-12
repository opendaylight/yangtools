/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import org.opendaylight.yangtools.binding.DataContainer;

/**
 * A {@link JavaContract} tied to a {@link DataContainer}.
 *
 * @param <T> the {@link DataContainer} type
 */
public non-sealed interface JavaDataContainer<T extends DataContainer> extends JavaContract<DataContainer, T> {
    // Nothing else
}
