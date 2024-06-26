/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.BaseIdentity;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * A codec capable of translating {@code identity} values between their YANG (QName) and Binding (Class) representation.
 */
public interface BindingIdentityCodec extends Immutable {
    /**
     * Convert a QNname to its corresponding Binding class.
     *
     * @param <T> Expected identity type
     * @param qname Identity QName
     * @return A binding value corresponding to the QName
     * @throws IllegalArgumentException if the qname does not map to an identity
     * @throws NullPointerException if {@code qname} is null
     */
    <T extends BaseIdentity> @NonNull T toBinding(@NonNull QName qname);

    /**
     * Convert a Binding value to its QName equivalent.
     *
     * @param bindingValue Binding value to convert
     * @return QName corresponding to the binding value
     * @throws IllegalArgumentException if the supplied value does not map to a known identity
     * @throws NullPointerException if bindingClass is null
     */
    @NonNull QName fromBinding(@NonNull BaseIdentity bindingValue);
}
