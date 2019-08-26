/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

/**
 * A serializer capable of encoding an input (typically DataObject) into some other form. This interface is present in
 * this package only due to constraints imposed by current implementation.
 */
// FIXME: this interface should not be necessary
interface BindingSerializer<P, I> {
    P serialize(I input);
}
