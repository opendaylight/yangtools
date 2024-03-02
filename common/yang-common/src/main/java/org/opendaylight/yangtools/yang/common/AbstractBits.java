/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import java.util.AbstractSet;
import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
abstract sealed class AbstractBits extends AbstractSet<String> implements Bits permits Bits32 {
    @java.io.Serial
    private static final long serialVersionUID = 1L;


    @java.io.Serial
    abstract Object writeReplace();
}
