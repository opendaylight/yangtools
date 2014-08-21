/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson.helpers;

import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
final class ObjectCodec extends AbstractCodecImpl implements Codec<Object, Object> {
    public static final Codec LEAFREF_DEFAULT_CODEC = new LeafrefCodecImpl();
    private static final Logger LOG = LoggerFactory.getLogger(RestCodecFactory.class);
    private final Codec instanceIdentifier;
    private final Codec identityrefCodec;
    private final TypeDefinition<?> type;

    ObjectCodec(final SchemaContextUtils schema, final TypeDefinition<?> typeDefinition) {
        super(schema);
        type = RestUtil.resolveBaseTypeFrom(typeDefinition);
        if (type instanceof IdentityrefTypeDefinition) {
            identityrefCodec = new IdentityrefCodecImpl(schema);
        } else {
            identityrefCodec = null;
        }
        if (type instanceof InstanceIdentifierTypeDefinition) {
            instanceIdentifier = new InstanceIdentifierCodecImpl(schema);
        } else {
            instanceIdentifier = null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object deserialize(final Object input) {
        try {
            if (type instanceof IdentityrefTypeDefinition) {
                if (input instanceof IdentityValuesDTO) {
                    return identityrefCodec.deserialize(input);
                }
                LOG.debug("Value is not instance of IdentityrefTypeDefinition but is {}. Therefore NULL is used as translation of  - {}",
                        input == null ? "null" : input.getClass(), String.valueOf(input));
                return null;
            } else if (type instanceof LeafrefTypeDefinition) {
                if (input instanceof IdentityValuesDTO) {
                    return LEAFREF_DEFAULT_CODEC.deserialize(((IdentityValuesDTO) input).getOriginValue());
                }
                return LEAFREF_DEFAULT_CODEC.deserialize(input);
            } else if (type instanceof InstanceIdentifierTypeDefinition) {
                if (input instanceof IdentityValuesDTO) {
                    return instanceIdentifier.deserialize(input);
                }
                LOG.info(
                        "Value is not instance of InstanceIdentifierTypeDefinition but is {}. Therefore NULL is used as translation of  - {}",
                        input == null ? "null" : input.getClass(), String.valueOf(input));
                return null;
            } else {
                TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>> typeAwarecodec = TypeDefinitionAwareCodec
                        .from(type);
                if (typeAwarecodec != null) {
                    if (input instanceof IdentityValuesDTO) {
                        return typeAwarecodec.deserialize(((IdentityValuesDTO) input).getOriginValue());
                    }
                    return typeAwarecodec.deserialize(String.valueOf(input));
                } else {
                    LOG.debug("Codec for type \"" + type.getQName().getLocalName()
                            + "\" is not implemented yet.");
                    return null;
                }
            }
        } catch (ClassCastException e) {
            // TODO remove this catch when everyone use codecs
            LOG.error("ClassCastException was thrown when codec is invoked with parameter " + String.valueOf(input),
                    e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object serialize(final Object input) {
        try {
            if (type instanceof IdentityrefTypeDefinition) {
                return identityrefCodec.serialize(input);
            } else if (type instanceof LeafrefTypeDefinition) {
                return LEAFREF_DEFAULT_CODEC.serialize(input);
            } else if (type instanceof InstanceIdentifierTypeDefinition) {
                return instanceIdentifier.serialize(input);
            } else {
                TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>> typeAwarecodec = TypeDefinitionAwareCodec
                        .from(type);
                if (typeAwarecodec != null) {
                    return typeAwarecodec.serialize(input);
                } else {
                    LOG.debug("Codec for type \"" + type.getQName().getLocalName()
                            + "\" is not implemented yet.");
                    return null;
                }
            }
        } catch (ClassCastException e) { // TODO remove this catch when everyone use codecs
            LOG.error(
                    "ClassCastException was thrown when codec is invoked with parameter " + String.valueOf(input),
                    e);
            return input;
        }
    }

}