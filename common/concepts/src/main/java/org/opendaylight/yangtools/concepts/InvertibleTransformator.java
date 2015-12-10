/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

/**
 * @deprecated Unused undocumented concept, scheduled for removal. Users should migrate to Guava's Converter.
 */
@Deprecated
public interface InvertibleTransformator<P, I> extends Transformator<P, I> {

    I fromProduct(P product);

}
