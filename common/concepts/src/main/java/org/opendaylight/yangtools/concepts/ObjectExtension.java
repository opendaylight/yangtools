/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import com.google.common.annotations.Beta;

/**
 * An extension to a concrete {@link ExtensibleObject}. This is a marker interface to introduce type safety and unlike
 * full Extensible Objects, does not specify how extensions are attached to an extensible object.
 *
 * @param <T> Extensible object type
 * @param <E> Extension type
 * @author Robert Varga
 */
@Beta
public interface ObjectExtension<T extends ExtensibleObject<T, E>, E extends ObjectExtension<T, E>> {

}
