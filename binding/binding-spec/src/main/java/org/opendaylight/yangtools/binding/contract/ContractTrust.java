/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.contract;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.BindingContract;
import org.opendaylight.yangtools.binding.BindingObject;
import org.opendaylight.yangtools.binding.impl.CodegenTrust;
import org.opendaylight.yangtools.binding.lib.CodeHelpers;

/**
 * A sealed class indicating trust derived from a code generation construct. This interface exists to make a safety
 * guarantee that the invocation is coming through {@link CodeHelpers} or some other codegen-accessible construct. While
 * this mechanism is defeatable at runtime, it affords some hoops an abuser needs to go through.
 *
 * @param <T> {@link BindingContract} type
 */
@NonNullByDefault
public sealed interface ContractTrust<T extends BindingObject> permits CodegenTrust {
    /**
     * {@return the contract guarantee of this trust}
     */
    Class<T> codegenContract();
}
