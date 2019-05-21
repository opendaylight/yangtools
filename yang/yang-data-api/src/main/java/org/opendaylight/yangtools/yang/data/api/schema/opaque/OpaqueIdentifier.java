/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.opaque;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.net.URI;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Identifier;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * An opaque version of NodeIdentifier. This identifier may be resolved based on a SchemaContext, as it semantically
 * refers to a QNameModule based on different semantics.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public abstract class OpaqueIdentifier implements Identifier {
    private static final long serialVersionUID = 1L;

    private final String localName;

    protected OpaqueIdentifier(final String localName) {
        this.localName = requireNonNull(localName);
    }

    public final String getLocalName() {
        return localName;
    }

    public abstract Optional<String> resolveModuleName(SchemaContext context);

    public abstract Optional<URI> resolveNamespace(SchemaContext context);

    @Override
    public final int hashCode() {
        return 31 * localName.hashCode() + subclassHashCode();
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OpaqueIdentifier)) {
            return false;
        }
        final OpaqueIdentifier other = (OpaqueIdentifier) obj;
        return localName.equals(other.localName) && subclassEquals(other);
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("localName", localName);
    }

    protected abstract int subclassHashCode();

    protected abstract boolean subclassEquals(OpaqueIdentifier other);
}
