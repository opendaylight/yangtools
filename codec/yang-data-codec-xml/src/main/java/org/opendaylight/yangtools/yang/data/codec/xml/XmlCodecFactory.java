/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MountPointContext;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizationException;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizationResult;
import org.opendaylight.yangtools.yang.data.impl.codec.AbstractIntegerStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.BinaryStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.BitsStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.BooleanStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.DecimalStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.EnumStringCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.StringStringCodec;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.util.codec.AbstractInputStreamNormalizer;
import org.opendaylight.yangtools.yang.data.util.codec.SharedCodecCache;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementInference;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnknownTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

/**
 * A thread-safe factory for instantiating {@link XmlCodec}s.
 */
public final class XmlCodecFactory extends AbstractInputStreamNormalizer<XmlCodec<?>> {
    private final @NonNull InstanceIdentifierXmlCodec instanceIdentifierCodec;
    private final @NonNull MountPointContext mountCtx;
    private final @Nullable PreferredPrefixes pref;

    private XmlCodecFactory(final MountPointContext mountCtx, final boolean modelPrefixes) {
        super(mountCtx.modelContext(), new SharedCodecCache<>());
        this.mountCtx = requireNonNull(mountCtx);
        pref = modelPrefixes ? new PreferredPrefixes.Shared(modelContext()) : null;
        instanceIdentifierCodec = new InstanceIdentifierXmlCodec(this, pref);
    }

    MountPointContext mountPointContext() {
        return mountCtx;
    }

    /**
     * Instantiate a new codec factory attached to a particular context.
     *
     * @param context MountPointContext to which the factory should be bound
     * @return A codec factory instance.
     */
    public static XmlCodecFactory create(final MountPointContext context) {
        return create(context, false);
    }

    /**
     * Instantiate a new codec factory attached to a particular context.
     *
     * @param context MountPointContext to which the factory should be bound
     * @param preferPrefixes prefer prefixes known to {@code context}
     * @return A codec factory instance.
     */
    public static XmlCodecFactory create(final MountPointContext context, final boolean preferPrefixes) {
        return new XmlCodecFactory(context, preferPrefixes);
    }

    /**
     * Instantiate a new codec factory attached to a particular context.
     *
     * @param context SchemaContext to which the factory should be bound
     * @return A codec factory instance.
     */
    public static XmlCodecFactory create(final EffectiveModelContext context) {
        return create(context, false);
    }

    /**
     * Instantiate a new codec factory attached to a particular context.
     *
     * @param context SchemaContext to which the factory should be bound
     * @param preferPrefixes prefer prefixes known to {@code context}
     * @return A codec factory instance.
     */
    public static XmlCodecFactory create(final EffectiveModelContext context, final boolean preferPrefixes) {
        return create(MountPointContext.of(requireNonNull(context)), preferPrefixes);
    }

    @Override
    protected XmlCodec<?> binaryCodec(final BinaryTypeDefinition type) {
        return new QuotedXmlCodec<>(BinaryStringCodec.from(type));
    }

    @Override
    protected XmlCodec<?> booleanCodec(final BooleanTypeDefinition type) {
        return new BooleanXmlCodec(BooleanStringCodec.from(type));
    }

    @Override
    protected XmlCodec<?> bitsCodec(final BitsTypeDefinition type) {
        return new QuotedXmlCodec<>(BitsStringCodec.from(type));
    }

    @Override
    protected XmlCodec<?> emptyCodec(final EmptyTypeDefinition type) {
        return EmptyXmlCodec.INSTANCE;
    }

    @Override
    protected XmlCodec<?> enumCodec(final EnumTypeDefinition type) {
        return new QuotedXmlCodec<>(EnumStringCodec.from(type));
    }

    @Override
    protected XmlCodec<?> identityRefCodec(final IdentityrefTypeDefinition type, final QNameModule module) {
        return new IdentityrefXmlCodec(modelContext(), module, pref);
    }

    @Override
    protected XmlCodec<YangInstanceIdentifier> instanceIdentifierCodec(final InstanceIdentifierTypeDefinition type) {
        return instanceIdentifierCodec;
    }

    @Override
    public XmlCodec<YangInstanceIdentifier> instanceIdentifierCodec() {
        return instanceIdentifierCodec;
    }

    @Override
    protected XmlCodec<?> int8Codec(final Int8TypeDefinition type) {
        return new NumberXmlCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected XmlCodec<?> int16Codec(final Int16TypeDefinition type) {
        return new NumberXmlCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected XmlCodec<?> int32Codec(final Int32TypeDefinition type) {
        return new NumberXmlCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected XmlCodec<?> int64Codec(final Int64TypeDefinition type) {
        return new NumberXmlCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected XmlCodec<?> decimalCodec(final DecimalTypeDefinition type) {
        return new NumberXmlCodec<>(DecimalStringCodec.from(type));
    }

    @Override
    protected XmlCodec<?> stringCodec(final StringTypeDefinition type) {
        return new QuotedXmlCodec<>(StringStringCodec.from(type));
    }

    @Override
    protected XmlCodec<?> uint8Codec(final Uint8TypeDefinition type) {
        return new NumberXmlCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected XmlCodec<?> uint16Codec(final Uint16TypeDefinition type) {
        return new NumberXmlCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected XmlCodec<?> uint32Codec(final Uint32TypeDefinition type) {
        return new NumberXmlCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected XmlCodec<?> uint64Codec(final Uint64TypeDefinition type) {
        return new NumberXmlCodec<>(AbstractIntegerStringCodec.from(type));
    }

    @Override
    protected XmlCodec<?> unionCodec(final UnionTypeDefinition type, final List<XmlCodec<?>> codecs) {
        return UnionXmlCodec.create(type, codecs);
    }

    @Override
    protected XmlCodec<?> unknownCodec(final UnknownTypeDefinition type) {
        return NullXmlCodec.INSTANCE;
    }

    @Override
    protected NormalizationResult<ContainerNode> parseDatastore(final InputStream stream,
            final NodeIdentifier containerName, final Unqualified moduleName)
                throws IOException, NormalizationException {
        final var reader = openStream(stream);
        checkExpectedElement(reader, containerName);
        final var builder = Builders.containerBuilder().withNodeIdentifier(containerName);


        throw new UnsupportedOperationException("FIXME: not implemented");
    }

    private static void checkExpectedElement(final XMLStreamReader reader, final NodeIdentifier containerName)
            throws NormalizationException {
        final var qname = containerName.getNodeType();
        final var expLocalName = qname.getLocalName();
        final var actLocalName = reader.getLocalName();
        final var expNamespace = qname.getNamespace().toString();
        final var actNamespace = reader.getNamespaceURI();
        if (!expLocalName.equals(actLocalName) || !expNamespace.equals(actNamespace)) {
            throw NormalizationException.ofMessage(
                "Expected name (" + expNamespace + ')' + expLocalName + ", got (" + actNamespace + ')' + actLocalName);
        }
    }

    @Override
    protected NormalizationResult<?> parseData(final SchemaInferenceStack stack, final InputStream stream)
            throws IOException, NormalizationException {
        throw new UnsupportedOperationException("FIXME: not implemented");
    }

    @Override
    protected NormalizationResult<?> parseChildData(final InputStream stream,
            final EffectiveStatementInference inference) throws IOException, NormalizationException {
        throw new UnsupportedOperationException("FIXME: not implemented");
    }

    @Override
    protected NormalizationResult<ContainerNode> parseInputOutput(final SchemaInferenceStack stack,
            final QName expected, final InputStream stream) throws IOException, NormalizationException {
        throw new UnsupportedOperationException("FIXME: not implemented");
    }

    private static @NonNull XMLStreamReader openStream(final InputStream stream) throws NormalizationException {
        try {
            final var reader = UntrustedXML.createXMLStreamReader(stream);
            while (!reader.isStartElement()) {
                if (!reader.hasNext()) {
                    throw NormalizationException.ofMessage("Missing documented element start");
                }
                reader.next();
            }
            return reader;
        } catch (XMLStreamException e) {
            throw NormalizationException.ofCause(e);
        }
    }
}
