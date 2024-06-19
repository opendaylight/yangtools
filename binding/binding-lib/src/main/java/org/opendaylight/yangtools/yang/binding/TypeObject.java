/*
 * Copyright (c) 2019 ZTE Corp. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

/**
 * TypeObject is a mark interface for all derived types.
 *
 * @author Jie Han
 */
public sealed interface TypeObject extends BindingObject
    permits BitsTypeObject, EnumTypeObject, ScalarTypeObject, UnionTypeObject {

}
