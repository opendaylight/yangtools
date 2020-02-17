/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.osgi;

import com.google.common.annotations.Beta;
import org.opendaylight.mdsal.binding.generator.api.BindingRuntimeContext;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;

/**
 * A {@link BindingRuntimeContext} enriched with the ability to look up {@link YangTextSchemaSource}s.
 *
 * @author Robert Varga
 *
 * @deprecated This service is exposed for transition purposes only.
 */
@Deprecated
@Beta
public interface BindingRuntimeContextService extends SchemaSourceProvider<YangTextSchemaSource> {
    /**
     * Return the current {@link BindingRuntimeContext}.
     *
     * @return Current BindingRuntimeContext.
     */
    BindingRuntimeContext getBindingRuntimeContext();
}
