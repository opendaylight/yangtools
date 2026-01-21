/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Definition of an argument to a YANG statement.
 *
 * @param argumentName the name of the argument
 * @param yinElement {@code true} if the argument is a YIN element
 */
public record ArgumentDefinition(QName argumentName, boolean yinElement) implements Immutable {
    /**
     * Default constructor.
     *
     * @param argumentName the name of the argument
     * @param yinElement {@code true} if the argument is a YIN element
     */
    public ArgumentDefinition {
        requireNonNull(argumentName);
    }
}
