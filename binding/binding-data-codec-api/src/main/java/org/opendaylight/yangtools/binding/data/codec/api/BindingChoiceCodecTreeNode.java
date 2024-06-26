/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.api;

import org.opendaylight.yangtools.binding.ChoiceIn;

/**
 * A {@link BindingDataContainerCodecTreeNode} corresponding to a base {@link ChoiceIn}.
 *
 * @param <C> ChoiceIn type
 */
public interface BindingChoiceCodecTreeNode<C extends ChoiceIn<?>> extends BindingDataContainerCodecTreeNode<C> {
    // Just a marker
}
