/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.model.api;

/**
 * Implementing this interface allows an object to hold information about that if
 * is generated type suitable for boxing.
 *
 * Example:
 * choice foo-choice {
 *   case foo-case {
 *     container foo {
 *           ...
 *     }
 *   }
 * }
 *
 * Suitable type have to implements ChildOf<T>, where !(T instanceof Identifiable) and
 * T does not place any structural requirements (must/when) on existence/value Foo.
 */
public interface BoxableType {

    /**
     * Check if generated type is suitable for boxing.
     *
     * @return true if generated type is suitable for boxing, false otherwise.
     */
    boolean isSuitableForBoxing();
}
