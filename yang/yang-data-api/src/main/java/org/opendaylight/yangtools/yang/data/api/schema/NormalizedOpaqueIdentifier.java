/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.WritableObject;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueIdentifier.NamespaceAware;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Normalized {@link OpaqueIdentifier}. This represents an identifier, which has been bound to a SchemaContext.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public final class NormalizedOpaqueIdentifier extends OpaqueIdentifier implements NamespaceAware, WritableObject {
    private static final long serialVersionUID = 1L;

    private final QName qname;

    public NormalizedOpaqueIdentifier(final QName qname) {
        this.qname = requireNonNull(qname);
    }

    @Override
    public String getLocalName() {
        return qname.getLocalName();
    }

    @Override
    public Optional<String> resolveModuleName(final SchemaContext context) {
        return context.findModule(qname.getModule()).map(Module::getName);
    }

    @Override
    public Optional<URI> resolveNamespace(final SchemaContext context) {
        return Optional.of(getNamespace());
    }

    @Override
    public URI getNamespace() {
        return qname.getNamespace();
    }

    @Override
    public void writeTo(final DataOutput out) throws IOException {
        qname.writeTo(out);
    }

    public static NormalizedOpaqueIdentifier readFrom(final DataInput in) throws IOException {
        return new NormalizedOpaqueIdentifier(QName.readFrom(in));
    }

    @Override
    protected int subclassHashCode() {
        return qname.getModule().hashCode();
    }

    @Override
    protected boolean subclassEquals(@NonNull final OpaqueIdentifier other) {
        return other instanceof NormalizedOpaqueIdentifier && qname.equals(((NormalizedOpaqueIdentifier) other).qname);
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return super.addToStringAttributes(helper.add("module", qname.getModule()));
    }
}
