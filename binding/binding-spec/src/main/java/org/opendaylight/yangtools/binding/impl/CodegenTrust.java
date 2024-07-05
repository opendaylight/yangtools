/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.impl;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.BindingObject;
import org.opendaylight.yangtools.binding.contract.ContractTrust;
import org.opendaylight.yangtools.binding.lib.CodeHelpers;

/**
 * A {@link ContractTrust} derived from generated code, made accessible via
 * {@link CodeHelpers#establishScalarTrust(Class)}. Note that all callers are required to perform defensive checks
 * before instantiating this class.
 */
@NonNullByDefault
public record CodegenTrust<T extends BindingObject>(Class<T> codegenContract) implements ContractTrust<T> {
    public CodegenTrust {
        requireNonNull(codegenContract);
    }
}
