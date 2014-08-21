/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson.helpers;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.codec.IdentityrefCodec;
import org.opendaylight.yangtools.yang.data.codec.gson.helpers.IdentityValuesDTO.IdentityValue;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class IdentityrefCodecImpl extends AbstractCodecImpl implements IdentityrefCodec<IdentityValuesDTO> {
    private static final Logger LOG = LoggerFactory.getLogger(IdentityrefCodecImpl.class);

    IdentityrefCodecImpl(final SchemaContextUtils schema) {
        super(schema);
    }

    @Override
    public IdentityValuesDTO serialize(final QName data) {
        return new IdentityValuesDTO(data.getNamespace().toString(), data.getLocalName(), data.getPrefix(), null);
    }

    @Override
    public QName deserialize(final IdentityValuesDTO data) {
        IdentityValue valueWithNamespace = data.getValuesWithNamespaces().get(0);
        Module module = getModuleByNamespace(valueWithNamespace.getNamespace());
        if (module == null) {
            LOG.info("Module was not found for namespace {}", valueWithNamespace.getNamespace());
            LOG.info("Idenetityref will be translated as NULL for data - {}", String.valueOf(valueWithNamespace));
            return null;
        }

        return QName.create(module.getNamespace(), module.getRevision(), valueWithNamespace.getValue());
    }

}