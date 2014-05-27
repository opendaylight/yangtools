/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/eplv10.html
 */
package org.opendaylight.yangtools.yang.parser.impl.util;

import java.io.InputStream;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.util.repo.SchemaSourceProvider;
import org.opendaylight.yangtools.yang.model.util.repo.SchemaSourceProviders;
import org.opendaylight.yangtools.yang.model.util.repo.SourceIdentifier;

import com.google.common.base.Optional;

public final class YangSourceFromCapabilitiesResolver extends YangSourceContextResolver {

    private final Iterable<QName> capabilities;

    public YangSourceFromCapabilitiesResolver(final Iterable<QName> capabilities,
            final SchemaSourceProvider<InputStream> schemaSourceProvider) {
        super(SchemaSourceProviders.toAdvancedSchemaSourceProvider(schemaSourceProvider));
        this.capabilities = capabilities;
    }

    @Override
    public YangSourceContext resolveContext() {
        for (QName capability : capabilities) {
            resolveCapability(capability);
        }
        return createSourceContext();
    }

    private void resolveCapability(final QName capability) {
        super.resolveSource(capability.getLocalName(), Optional.fromNullable(capability.getFormattedRevision()));
    }

    @Override
    public Optional<YangModelDependencyInfo> getDependencyInfo(final SourceIdentifier identifier) {
        Optional<InputStream> source = getSchemaSource(identifier);
        if (source.isPresent()) {
            return Optional.of(YangModelDependencyInfo.fromInputStream(source.get()));
        }
        return Optional.absent();
    }

    private Optional<InputStream> getSchemaSource(final SourceIdentifier identifier) {
        return getSourceProvider().getSchemaSource(identifier.getName(),
                Optional.fromNullable(identifier.getRevision()));
    }

}
