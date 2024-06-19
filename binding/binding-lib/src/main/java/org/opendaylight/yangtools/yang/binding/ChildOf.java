/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

/**
 * Child of parent container. Marker interface uniquely bounding generated Java interfaces to their  parent container.
 *
 * <p>
 * Any nested Java interface generated from YANG must implement this interface, where parameter <code>P</code> points
 * to it's defining data node container (interface generated for List, Container, Case).
 *
 * <p>
 * In case of children added by augmentation (which implements {@link Augmentation}) interfaces representing nested
 * container must implements {@link ChildOf} with same argument as Augmentation.
 *
 * @param <P> Parent container
 */
public interface ChildOf<P> extends DataObject {

}
