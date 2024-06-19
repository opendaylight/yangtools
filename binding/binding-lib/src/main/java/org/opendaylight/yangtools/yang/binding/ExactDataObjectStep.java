/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

/**
 * A {@link DataObjectStep} which is exactly specified. Due to how {@link DataObject} and {@link KeyAware} are tied
 * together, a class generated for a {@code list} is strictly a {@link DataObject}, but its semantics differ.
 *
 * <p>
 * This interface captures two possible outcomes:
 * <ol>
 *   <li>this is a plain {@link NodeStep}</li>
 *   <li>this is a fully-specified {@link KeyStep}</li>
 * </ol>
 *
 * @param <T> DataObject type
 */
public sealed interface ExactDataObjectStep<T extends DataObject> extends DataObjectStep<T>
    permits KeyStep, NodeStep {
    // for class hierarchy only
}