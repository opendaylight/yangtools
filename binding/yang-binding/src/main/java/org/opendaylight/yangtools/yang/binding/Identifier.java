/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

/**
 * Object is unique identifier for another object.
 *
 * @param <T>
 *            Class of object for which this object is identifier
 * @author ttkacik
 */
public interface Identifier<T extends Identifiable<?>> extends org.opendaylight.yangtools.concepts.Identifier {

}
