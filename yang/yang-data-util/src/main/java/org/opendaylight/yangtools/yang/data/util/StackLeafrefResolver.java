/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import org.opendaylight.yangtools.yang.data.util.codec.AbstractCodecFactory.LeafrefResolver;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * Utility implementation of {@link LeafrefResolver} based on a {@link SchemaInferenceStack}. This class does not modify
 * supplied stack and relies on its state to provide correct context for the purposes of
 * {@link #resolveLeafref(org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition)}. It is the user's
 * responsibility to maintain coherence between the stack and invocations of that method.
 */
public final class StackLeafrefResolver extends AbstractLeafrefResolver {
    public StackLeafrefResolver(final SchemaInferenceStack stack) {
        super(stack);
    }
}
