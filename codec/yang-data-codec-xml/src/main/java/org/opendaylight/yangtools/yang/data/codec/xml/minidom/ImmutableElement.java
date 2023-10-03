/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml.minidom;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
abstract sealed class ImmutableElement extends ImmutableNode implements Element
        permits ImmutableContainerElement, ImmutableTextElement {
    private final Object attributes;

    ImmutableElement(final @Nullable String namespace, final String localName, final List<Attribute> attributes) {
        super(namespace, localName);
        this.attributes = maskList(ImmutableList.copyOf(attributes));
    }

    @Override
    public List<Attribute> attributes() {
        return unmaskList(attributes, Attribute.class);
    }

    /**
     * Utility method for squashing singleton lists into single objects. This is a CPU/mem trade-off, which we are
     * usually willing to make: for the cost of an instanceof check we can save one object and re-create it when needed.
     * The inverse operation is #unmaskSubstatements(Object)}.
     *
     * @param list list to mask
     * @return Masked list
     * @throws NullPointerException if list is null
     */
    static final Object maskList(final ImmutableList<?> list) {
        // Note: ImmutableList guarantees non-null content
        return list.size() == 1 ? list.get(0) : list;
    }

    /**
     * Utility method for recovering singleton lists squashed by {@link #maskList(ImmutableList)}.
     *
     * @param masked list to unmask
     * @return Unmasked list
     * @throws NullPointerException if any argument is null
     * @throws ClassCastException if masked object does not match expected class
     */
    @SuppressWarnings("unchecked")
    static final <T> ImmutableList<T> unmaskList(final Object masked, final Class<T> type) {
        return masked instanceof ImmutableList ? (ImmutableList<T>) masked
            // Yes, this is ugly code, which could use an explicit verify, that would just change the what sort
            // of exception we throw. ClassCastException is as good as VerifyException.
            : ImmutableList.of(type.cast(masked));
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        super.addToStringAttributes(helper);
        final var size = attributes().size();
        if (size != 0) {
            helper.add("attrs", size);
        }
        return helper;
    }

}