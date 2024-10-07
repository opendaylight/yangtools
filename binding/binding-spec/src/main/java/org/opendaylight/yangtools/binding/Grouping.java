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
 * by other Binding interfaces and therefore cannot capture its identity via generics. Furthermore it can be used by
 * users to more closely specify what {@link DataObject} species is acceptable in generics, for example:
 * <pre>{@code
 *     public interface Foo extends Grouping {
 *
 *     }
 *
 *     public interface Bar extends DataObject, Foo {
 *
 *     }
 *
 *     public interface Baz extends DataObject, Foo {
 *
 *     }

 *     public interface Service<T extends Foo & DataObject> {
 *
 *        // ...
 *
 *     }
 * }</pre>
 * This precludes generated grouping interfaces from specializing {@code implementedInterface}.
 *
 * <p>Unlike most other {@link BindingObject}s and {@link DataContainer}s, groupings are not instantiated, but capture
 * a reusable trait, which is usually implemented by other {@link DataContainer}s.
 *
 * <p>Please note that above restrictions on generics applies to all interfaces extending this contract.
 */
public non-sealed interface Grouping extends BindingObject, DataContainer {
    // Nothing else
}
