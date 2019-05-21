/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.AbstractOpaqueIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueIdentifier.NamespaceAware;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

@Beta
@NonNullByDefault
public final class XMLOpaqueIdentifier extends AbstractOpaqueIdentifier implements NamespaceAware {
    private static final long serialVersionUID = 1L;

    private final URI namespace;

    public XMLOpaqueIdentifier(final URI xmlNamespace, final String localName) {
        super(localName);
        this.namespace = requireNonNull(xmlNamespace);
    }

    public XMLOpaqueIdentifier(final QName qname) {
        this(qname.getNamespace(), qname.getLocalName());
    }

    @Override
    public URI getNamespace() {
        return namespace;
    }

    @Override
    public Optional<String> resolveModuleName(final SchemaContext context) {
        final Iterator<Module> it = context.findModules(namespace).iterator();
        return it.hasNext() ? Optional.of(it.next().getName()) : Optional.empty();
    }

    @Override
    public Optional<URI> resolveNamespace(final SchemaContext context) {
        return Optional.of(namespace);
    }

    @Override
    public void writeTo(final DataOutput out) throws IOException {
        out.writeUTF(namespace.toString());
        out.writeUTF(getLocalName());
    }

    public static XMLOpaqueIdentifier readFrom(final DataInput in) throws IOException {
        final URI namespace = URI.create(in.readUTF());
        final String localName = in.readUTF();
        return new XMLOpaqueIdentifier(namespace, localName);
    }

    @Override
    protected int subclassHashCode() {
        return namespace.hashCode();
    }

    @Override
    protected boolean subclassEquals(final OpaqueIdentifier other) {
        return other instanceof XMLOpaqueIdentifier
                && namespace.equals(((XMLOpaqueIdentifier) other).namespace);
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return super.addToStringAttributes(helper.add("namespace", namespace));
    }
}
