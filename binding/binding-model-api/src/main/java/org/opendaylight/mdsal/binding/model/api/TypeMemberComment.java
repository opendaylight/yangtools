/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Structured comment of a particular class member. This is aimed towards unifying the layout of a particular type.
 */
@Beta
public final class TypeMemberComment implements Immutable {
    private final String contractDescription;
    private final String referenceDescription;
    private final String typeSignature;

    public TypeMemberComment(final String contractDescription, final String referenceDescription,
            final String typeSignature) {
        this.contractDescription = contractDescription;
        this.referenceDescription = referenceDescription;
        this.typeSignature = typeSignature;
    }

    /**
     * Return the member contract description. This string, if present will represent the equivalent of the words you
     * are just reading. This forms what is usually:
     * <ul>
     *   <li>hand-written with careful explanation</li>
     *   <li>describing the general contract outline, what the member does/holds/etc. For methods this might be pre-
     *       and post-conditions.</li>
     * </ul>
     *
     * @return The equivalent of the above blurb.
     */
    public @Nullable String contractDescription() {
        return contractDescription;
    }

    /**
     * Return the member reference description. This description is passed unmodified, pre-formatted in a single block.
     * It is expected to look something like the following paragraph:
     *
     * <p>
     * <pre>
     *   <code>
     *     A 32-bit bit unsigned word. Individual bits are expected to be interpreted as follows:
     *
     *       31
     *     +----+ ...
     *   </code>
     * </pre>
     *
     * @return The equivalent of the above pre-formmated paragraph.
     */
    public @Nullable String referenceDescription() {
        return referenceDescription;
    }

    /**
     * Return the type signature of this type member. This is only applicable for methods, use of anywhere else is
     * expected to either be ignored, or processed as is. As a matter of example, this method has a signature starting
     * right after this period<b>.</b>
     *
     * @return Return the signature description, just like these words right here
     */
    public @Nullable String typeSignature() {
        return typeSignature;
    }

    public static @NonNull TypeMemberComment contractOf(final String contractDescription) {
        return new TypeMemberComment(requireNonNull(contractDescription), null, null);
    }

    public static @NonNull TypeMemberComment referenceOf(final String referenceDescription) {
        return new TypeMemberComment(null, requireNonNull(referenceDescription), null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contractDescription, referenceDescription, typeSignature);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof TypeMemberComment)) {
            return false;
        }
        final TypeMemberComment other = (TypeMemberComment) obj;
        return Objects.equals(contractDescription, other.contractDescription)
            && Objects.equals(referenceDescription, other.referenceDescription)
            && Objects.equals(typeSignature, other.typeSignature);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues()
            .add("contract", contractDescription).add("reference", referenceDescription).add("type", typeSignature)
            .toString();
    }
}
