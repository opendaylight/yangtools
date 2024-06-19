/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.AbstractQName;

/**
 * Enumeration of known strategies for translating a YANG node identifier into a Java package name segment or a Java
 * simple class name.
 */
abstract class ClassNamingStrategy implements Immutable {
    /**
     * Return the YANG node identifier backing this naming strategy. Only the {@link AbstractQName#getLocalName()} part
     * of the identifier is significant.
     *
     * @return YANG node identifier.
     */
    abstract @NonNull AbstractQName nodeIdentifier();

    /**
     * Return the simple Java class name assigned by this naming strategy.
     *
     * @return Simple class name
     */
    @NonNull  String simpleClassName() {
        return BindingMapping.getClassName(nodeIdentifier().getLocalName());
    }

    /**
     * Return the fallback naming strategy. The fallback is used if this strategy ends up being insufficient in
     * assigning a unique name.
     *
     * @return Fallback strategy, {@code null} if there is no fallback.
     */
    abstract @Nullable ClassNamingStrategy fallback();

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    abstract @NonNull ToStringHelper addToStringAttributes(@NonNull ToStringHelper helper);
}
