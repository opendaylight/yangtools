/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

/**
 * An extension to a concrete {@link ExtensibleObject}. This is a marker interface to introduce type safety and unlike
 * full Extensible Objects, does not specify how extensions are attached to an extensible object.
 *
 * <p>
 * {@link ObjectExtension} instances are attached to their host object and share its state, which means they work in
 * concert and care must be taken to ensure consistency, such as thread safety and observable effects.
 *
 * @param <O> Extensible object type
 * @param <E> Extension type
 */
public interface ObjectExtension<O extends ExtensibleObject<O, E>, E extends ObjectExtension<O, E>> {

}
