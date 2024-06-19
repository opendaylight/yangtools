/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Type capture of an entity producing NodeCodecContexts. Implementations are required to perform memoization. This
 * interface does not form API surface and is exposed only for generated code. It can change at any time.
 */
@NonNullByDefault
sealed interface CodecContextSupplier permits LazyCodecContextSupplier, ValueNodeCodecContext {

    CodecContext getCodecContext();
}
