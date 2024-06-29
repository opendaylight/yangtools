/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.DataContainer.Addressable;

/**
 * A {@link DataObjectStep} which is not exactly specified. Other {@link DataObjectStep} steps can be
 * {@link #matches(DataObjectStep) matched} against it.
 */
public sealed interface InexactDataObjectStep<T extends Addressable.Multiple> extends DataObjectStep<T>
        permits KeylessStep {

    boolean matches(@NonNull DataObjectStep<?> other);
}
