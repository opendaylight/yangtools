/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.impl;

import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.BindingContract;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.contract.ContractTrust;
import org.slf4j.LoggerFactory;

/**
 * Trust derived from code generation/subclass access. The accessing stack is required to conform to the specified
 * {@link BindingContract}.
 *
 * @param <T> the {@link BindingContract} this instance abides to
 */
@NonNullByDefault
public final class CodegenTrust implements ContractTrust {
    /**
     * Contract ContractTrust afforded to {@link EntryObject} subclasses. This trust authorizes a bypass on:
     * <ul>
     *   <li>{@link org.opendaylight.yangtools.binding.KeyStep} class class and type class checks</li>
     * </ul>
     */
    public static final ContractTrust ENTRY_OBJECT = new CodegenTrust(EntryObject.class);

    /**
     * Global flag indicating that {@link ContractTrust} should not be trusted.
     */
    public static final boolean UNTRUSTED;

    static {
        UNTRUSTED = !"true".equals(
            System.getProperty("org.opendaylight.yangtools.binding.trust-codegen", "true"));
        if (UNTRUSTED) {
            LoggerFactory.getLogger(CodegenTrust.class).info("CodegenTrust is not trusted");
        }
    }

    private final Class<? extends BindingContract<?>> contract;

    @SuppressWarnings("unchecked")
    private CodegenTrust(final Class<? extends BindingContract<?>> contract) {
        // Defeats raw Class<?> access
        this.contract = (@NonNull Class<? extends BindingContract<?>>) contract.asSubclass(BindingContract.class);
    }

    @Override
    public Class<? extends BindingContract<?>> codegenContract() {
        return contract;
    }

    @Override
    public int hashCode() {
        return contract.hashCode();
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return obj == this || obj instanceof CodegenTrust other && contract.equals(other.contract);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("contract", contract).toString();
    }
}
