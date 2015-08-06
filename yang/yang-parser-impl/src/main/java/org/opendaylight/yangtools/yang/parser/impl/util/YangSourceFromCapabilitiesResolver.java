/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl.util;

import com.google.common.base.Optional;
import java.io.InputStream;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.util.repo.SchemaSourceProvider;
import org.opendaylight.yangtools.yang.model.util.repo.SchemaSourceProviders;
import org.opendaylight.yangtools.yang.model.util.repo.SourceIdentifier;

/**
 *
 * Source code resolver which resolves Yang Source Context against
 * {@link SchemaSourceProvider} and set of QName which represent capabilities.
 *
 * This source code resolver is useful for components which deals with
 * capability exchange similar to YANG/Netconf specification
 * and there is {@link SchemaSourceProvider} able to retrieve YANG models.
 *
 */
@Deprecated
public final class YangSourceFromCapabilitiesResolver extends YangSourceContextResolver {

    private final Iterable<QName> capabilities;

    /**
     * Construct new {@link YangSourceFromCapabilitiesResolver}.
     *
     * @param capabilities Set of QName representing module capabilities, {@link QName#getLocalName()} represents
     * source name and {@link QName#getRevision()} represents revision of source.
     *
     * @param schemaSourceProvider - {@link SchemaSourceProvider} which should be used to resolve sources.
     */
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
        Optional<InputStream> source = getSourceProvider().getSchemaSource(identifier);
        if (source.isPresent()) {
            return Optional.of(YangModelDependencyInfo.fromInputStream(source.get()));
        }
        return Optional.absent();
    }

}
