/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import java.util.function.Supplier;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Type capture of an entity producing NodeCodecContexts.
 */
interface NodeContextSupplier extends Supplier<NodeCodecContext> {
    @Override
    @NonNull NodeCodecContext get();
}
