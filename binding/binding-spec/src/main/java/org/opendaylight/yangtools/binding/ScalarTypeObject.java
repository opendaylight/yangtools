/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

/**
 * A {@link TypeObject} that encapsulates an immutable native type. These are generated as YANG Binding type captures,
 * such as those implied by {@code typedef} and parameterized {@code type} statements.
 *
 * @param <T> native value type
 */
public non-sealed interface ScalarTypeObject<T> extends TypeObject, ValueAware<T> {
    // Nothing else
}
