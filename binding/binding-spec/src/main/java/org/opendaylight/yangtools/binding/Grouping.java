/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

/**
 * Base interface extended by all interfaces generated for {@code grouping} statements. This interface can be inherited
 * by other Binding interfaces and therefore cannot capture its identity via generics and is therefore left with
 * type safety afforded to by {@link #implementedInterface()} specialization. Binding code generation is required to
 * generate an abstract method declaration which appropriately narrows the return type. For example:
 * <pre>{@code
 *     public interface Foo extends Grouping {
 *         @Override
 *         Class<? extends Foo> implementedInterface();
 *     }
 * }</pre>
 *
 * <p>
 * Please note that the restriction on generics applies to all interfaces extending this contract.
 */
public non-sealed interface Grouping extends DataContainer {
    @Override
    Class<? extends Grouping> implementedInterface();
}
