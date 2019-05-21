/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

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
import org.opendaylight.yangtools.yang.data.api.schema.opaque.AbstractOpaqueIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.opaque.OpaqueIdentifier.ModuleNameAware;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

@Beta
@NonNullByDefault
public final class JSONOpaqueIdentifier extends AbstractOpaqueIdentifier implements ModuleNameAware {
    private static final long serialVersionUID = 1L;

    private final String moduleName;

    public JSONOpaqueIdentifier(final String moduleName, final String localName) {
        super(localName);
        this.moduleName = requireNonNull(moduleName);
    }

    @Override
    public String getModuleName() {
        return moduleName;
    }

    @Override
    public Optional<String> resolveModuleName(final SchemaContext context) {
        return Optional.of(moduleName);
    }

    @Override
    public Optional<URI> resolveNamespace(final SchemaContext context) {
        final Iterator<Module> it = context.findModules(moduleName).iterator();
        return it.hasNext() ? Optional.of(it.next().getNamespace()) : Optional.empty();
    }

    @Override
    public void writeTo(final DataOutput out) throws IOException {
        out.writeUTF(moduleName);
        out.writeUTF(getLocalName());
    }

    public static JSONOpaqueIdentifier readFrom(final DataInput in) throws IOException {
        final String moduleName = in.readUTF();
        final String localName = in.readUTF();
        return new JSONOpaqueIdentifier(moduleName, localName);
    }

    @Override
    protected int subclassHashCode() {
        return moduleName.hashCode();
    }

    @Override
    protected boolean subclassEquals(final OpaqueIdentifier other) {
        return other instanceof JSONOpaqueIdentifier
                && moduleName.equals(((JSONOpaqueIdentifier) other).moduleName);
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return super.addToStringAttributes(helper.add("module", moduleName));
    }
}
