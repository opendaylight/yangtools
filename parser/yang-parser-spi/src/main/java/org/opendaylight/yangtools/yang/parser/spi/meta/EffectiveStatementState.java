/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Significant state captured by an {@link EffectiveStatement} at its instantiation site. This can be used to compare
 * statements which are instantiated through inference, for example through {@code uses} from {@code grouping}s, when
 * the reactor establishes the two instances have the same substatements.
 */
@Beta
public abstract class EffectiveStatementState implements Immutable {
    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);
}
