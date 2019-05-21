/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.opaque;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.net.URI;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Identifier;
import org.opendaylight.yangtools.concepts.WritableObject;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * An opaque version of NodeIdentifier. This identifier may be resolved based on a SchemaContext, as it semantically
 * refers to a QNameModule based on different semantics.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public abstract class OpaqueIdentifier implements Identifier, WritableObject {
    /**
     * An {@link OpaqueIdentifier} which has been previously bound to a namespace. There is no implied relationship
     * between the namespace and any SchemaContext. This URI is sufficient only for transporting data through XML.
     *
     * @author Robert Varga
     */
    public interface NamespaceAware {
        /**
         * Return the XML namespace which has been bound to this identifier. This does not necessarily contribute to
         * equality of the identifier.
         *
         * @return A namespace.
         */
        URI getNamespace();
    }

    private static final long serialVersionUID = 1L;

    public abstract String getLocalName();

    public abstract Optional<String> resolveModuleName(SchemaContext context);

    public abstract Optional<URI> resolveNamespace(SchemaContext context);

    @Override
    public final int hashCode() {
        return 31 * getLocalName().hashCode() + subclassHashCode();
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
        return getLocalName().equals(other.getLocalName()) && subclassEquals(other);
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("localName", getLocalName());
    }

    protected abstract int subclassHashCode();

    protected abstract boolean subclassEquals(OpaqueIdentifier other);
}
