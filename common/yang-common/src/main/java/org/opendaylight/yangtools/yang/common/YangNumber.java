/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A YANG-defined number. Can be either {@link Decimal64} or one of {@link YangInteger} types.
 *
 * @since 15.1.0
 */
// FIXME: abstract class extends java.lang.Number
@Beta
@NonNullByDefault
public sealed interface YangNumber<T extends YangNumber<T>> extends CanonicalValue<T> permits YangInteger, Decimal64 {
    // Nothing else
}
