/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.BaseIdentity;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * A codec capable of translating {@code identity} values between their YANG (QName) and Binding (Class) representation.
 */
@Beta
public interface BindingIdentityCodec {
    /**
     * Convert a QNname to its corresponding Binding class.
     *
     * @param qname Identity QName
     * @return A binding Class corresponding to the QName
     * @throws IllegalArgumentException if the qname does not map to an identity
     * @throws NullPointerException if qname is null
     */
    @NonNull Class<? extends BaseIdentity> toBinding(@NonNull QName qname);

    /**
     * Concert a Binding class to its QName equivalent.
     *
     * @param bindingClass Binding class to convert
     * @return QName corresponding to the binding class
     * @throws NullPointerException if bindingClass is null
     */
    @NonNull QName fromBinding(Class<? extends BaseIdentity> bindingClass);
}
