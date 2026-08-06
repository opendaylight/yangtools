/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import org.opendaylight.yangtools.binding.lib.JavaDataContainer;

/**
 * A concrete {@code case} in a {@code choice}. Interfaces generated to this specification are required to implement
 * a generated {@link ChoiceIn} class.
 *
 * @param <P> Parent container
 * @param <C> a {@link ChoiceIn} in that parent
 * @param <T> concrete
 * @see ChoiceIn
 */
public interface CaseObject<
        P extends DataContainer,
        C extends ChoiceIn<P, C>,
        T extends CaseObject<P, C, T> & ChoiceIn<P, C>>
        extends Augmentable<T>, DataObject, JavaDataContainer<T> {
    @Override
    Class<T> implementedInterface();
}
