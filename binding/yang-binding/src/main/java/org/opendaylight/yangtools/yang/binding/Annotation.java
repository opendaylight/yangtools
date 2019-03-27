/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import com.google.common.annotations.Beta;

/**
 * Base marker interface implemented by all YANG-defined annotations through the facilities provided by
 * <a href="https://tools.ietf.org/html/rfc7952">RFC7952</a>. This interface is not meant to be directly implemented,
 * but rather serves as a hook for generated code.
 *
 * <p>
 * An Annotation is a cross between a {@link TypeObject}, a {@link DataObject} and an {@link Augmentation}:
 * <ul>
 * <li>Similar to a {@code TypeObject} it can only hold a single scalar value, which has to be present.</li>
 * <li>Similar to most {@code TypeObject}s, the value is available through {@link #getValue()} method</li>
 * <li>Similar to a {@code DataObject}, the return type of {@link #getValue()} can point to a generated
 *     {@code TypeObject}, not only a base YANG type</li>
 * <li>Similar to an {@code Augmentation} it can be attached to other objects via {@link AnnotationAware} interface.
 *     Unlike augmentations, though, annotations do not have a strict tie to the object they attach to.</li>
 * </ul>
 *
 * @param <T> Value type
 */
@Beta
public interface Annotation<T> extends BindingObject, ValueAware<T> {

}
