/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A {@link TypeObject} that encapsulates an immutable native type. These are generated as YANG Binding type captures,
 * such as those implied by {@code typedef} and parameterized {@code type} statements.
 */
@Beta
public interface ScalarTypeObject<T> extends TypeObject {
    /**
     * Return encapsulated value.
     *
     * @return Encapsulated value.
     */
    @NonNull T getValue();
}
