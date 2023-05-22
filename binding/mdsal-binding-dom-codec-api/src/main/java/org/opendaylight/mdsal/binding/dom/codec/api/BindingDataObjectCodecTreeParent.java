/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.ChoiceIn;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Common interface for entities which can supply a {@link BindingDataObjectCodecTreeNode} based on Binding DataObject
 * class instance.
 *
 * @param <T> Dummy parameter to work around problems with streaming {@link ChoiceIn} classes. Essentially we want
 *            {@link #streamChild(Class)} to also service such classes, which are not {@link DataObject}s. The problem
 *            really is that {@code case} interfaces are DataObjects and hence are an alluring target for that method.
 *            The workaround works with two sides:
 *            <ul>
 *              <li>Here the fact that we are generic means that binary compatibility dictates that our signature be
 *                  backwards compatible with anyone who might have seen us as non-generic, i.e. in streamChild() taking
 *                  a raw class (because there were no generics)</li>
 *              <li>Users pick it up from there: all they need to do is to go to raw types, then accepts any class, but
 *                  from there we can just chain on method return, arriving into a type-safe world again</li>
 *            </ul>
 */
@Beta
// FIXME: Now the above documentation is fine and dandy, but can we adjust the shape of our classes somehow?
//        The problem seems to be grouping interfaces, which are pulling in 'DataObject' to the picture and thus
//        and up marking case statements even if we do not mark them specially. If we could disconnect concrete cases
//        from DataObject in a civil manner, then we could go the DataObject -> ChoiceIn -> CaseOf and not have
//        choice/case statements in the picture. But what would that do to all the concepts hanging off of DataObject?
public interface BindingDataObjectCodecTreeParent<T> {
    /**
     * Returns child context as if it was walked by {@link BindingStreamEventWriter}. This means that to enter case,
     * one must issue {@code streamChild(ChoiceClass).streamChild(CaseClass)}.
     *
     * @param <E> Stream child DataObject type
     * @param childClass Child class by Binding Stream navigation
     * @return Context of child
     * @throws IllegalArgumentException If supplied child class is not valid in specified context.
     */
    <E extends DataObject> @NonNull CommonDataObjectCodecTreeNode<E> streamChild(@NonNull Class<E> childClass);
}
