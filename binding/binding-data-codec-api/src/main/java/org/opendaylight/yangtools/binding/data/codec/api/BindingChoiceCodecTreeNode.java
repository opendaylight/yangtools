/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.api;

import org.opendaylight.yangtools.binding.ChoiceIn;
import org.opendaylight.yangtools.binding.DataContainer;

/**
 * A {@link BindingDataContainerCodecTreeNode} corresponding to a base {@link ChoiceIn}.
 *
 * @param <C> ChoiceIn type
 */
public interface BindingChoiceCodecTreeNode<P extends DataContainer, C extends ChoiceIn<P, C>>
    extends BindingDataContainerCodecTreeNode<P> {
    // Just a marker
}
