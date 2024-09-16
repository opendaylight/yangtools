/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.data;

/**
 * An instantiation of a YANG type. There are two kinds of instantiations:
 * <ol>
 *   <li>{@link ScalarValue}, corresponding to a value held by a {@code leaf}</li>
 *   <li>{@link ArrayValue}, corresponding to a value held by a {@code leaf-list}</li>
 * </ol>
 */
// FIXME: this should become an abstract class once ScalarValue can be an abstract class
public sealed interface Value extends ModeledData permits ArrayValue, ScalarValue {
    // Nothing else, really
}
