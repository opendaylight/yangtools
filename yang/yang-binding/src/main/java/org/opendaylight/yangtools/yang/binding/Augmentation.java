/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

/**
 * Augmentation (extension) of other interface.
 * 
 * This interface uniquely bounds Augmentation to generated 
 * interface.
 * 
 * All interfaces generated from YANG Augmentation statement
 * must implement this interface with parameter <code>P</code>
 * which uniquely points to it's target class.
 * 
 * 
 *
 * @param <T> Class to which this implementation is extension.
 */
public interface Augmentation<T> {

}
