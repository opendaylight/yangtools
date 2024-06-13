/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter.UNKNOWN_SIZE;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.UncheckedExecutionException;
import java.io.DataInput;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Either;
import org.opendaylight.yangtools.concepts.WritableObjects;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Abstract base class for NormalizedNodeDataInput based on {@link PotassiumNode}, {@link PotassiumPathArgument} and
 * {@link PotassiumValue}.
 */
final class PotassiumDataInput extends AbstractNormalizedNodeDataInput {
    private static final Logger LOG = LoggerFactory.getLogger(PotassiumDataInput.class);

    // Known singleton objects
    private static final @NonNull Byte INT8_0 = 0;
    private static final @NonNull Short INT16_0 = 0;
    private static final @NonNull Integer INT32_0 = 0;
    private static final @NonNull Long INT64_0 = 0L;
    private static final byte @NonNull[] BINARY_0 = new byte[0];

    private final List<NodeIdentifier> codedNodeIdentifiers = new ArrayList<>();
    private final List<QNameModule> codedModules = new ArrayList<>();
    private final List<String> codedStrings = new ArrayList<>();

    PotassiumDataInput(final DataInput input) {
        super(input);
    }

    @Override
    public NormalizedNodeStreamVersion getVersion() {
        return NormalizedNodeStreamVersion.POTASSIUM;
    }

    @Override
    public void streamNormalizedNode(final NormalizedNodeStreamWriter writer) throws IOException {
        streamNormalizedNode(requireNonNull(writer), null, input.readByte());
    }

    private void streamNormalizedNode(final NormalizedNodeStreamWriter writer, final PathArgument parent,
            final byte nodeHeader) throws IOException {
        switch (nodeHeader & PotassiumNode.TYPE_MASK) {
            case PotassiumNode.NODE_LEAF -> streamLeaf(writer, parent, nodeHeader);
            case PotassiumNode.NODE_CONTAINER -> streamContainer(writer, nodeHeader);
            case PotassiumNode.NODE_LIST -> streamList(writer, nodeHeader);
            case PotassiumNode.NODE_MAP -> streamMap(writer, nodeHeader);
            case PotassiumNode.NODE_MAP_ORDERED -> streamMapOrdered(writer, nodeHeader);
            case PotassiumNode.NODE_LEAFSET -> streamLeafset(writer, nodeHeader);
            case PotassiumNode.NODE_LEAFSET_ORDERED -> streamLeafsetOrdered(writer, nodeHeader);
            case PotassiumNode.NODE_CHOICE -> streamChoice(writer, nodeHeader);
            case PotassiumNode.NODE_ANYXML -> streamAnyxml(writer, nodeHeader);
            case PotassiumNode.NODE_LIST_ENTRY -> streamListEntry(writer, parent, nodeHeader);
            case PotassiumNode.NODE_LEAFSET_ENTRY -> streamLeafsetEntry(writer, parent, nodeHeader);
            case PotassiumNode.NODE_MAP_ENTRY -> streamMapEntry(writer, parent, nodeHeader);
            default -> throw new InvalidNormalizedNodeStreamException("Unexpected node header " + nodeHeader);
        }
    }

    private void streamAnyxml(final NormalizedNodeStreamWriter writer, final byte nodeHeader) throws IOException {
        final var identifier = decodeNodeIdentifier(nodeHeader);
        LOG.trace("Streaming anyxml node {}", identifier);

        final var value = readDOMSource();
        if (writer.startAnyxmlNode(identifier, DOMSource.class)) {
            writer.domSourceValue(value);
            writer.endNode();
        }
    }

    private void streamChoice(final NormalizedNodeStreamWriter writer, final byte nodeHeader) throws IOException {
        final var identifier = decodeNodeIdentifier(nodeHeader);
        LOG.trace("Streaming choice node {}", identifier);
        writer.startChoiceNode(identifier, UNKNOWN_SIZE);
        commonStreamContainer(writer, identifier);
    }

    private void streamContainer(final NormalizedNodeStreamWriter writer, final byte nodeHeader) throws IOException {
        final var identifier = decodeNodeIdentifier(nodeHeader);
        LOG.trace("Streaming container node {}", identifier);
        writer.startContainerNode(identifier, UNKNOWN_SIZE);
        commonStreamContainer(writer, identifier);
    }

    private void streamLeaf(final NormalizedNodeStreamWriter writer, final PathArgument parent, final byte nodeHeader)
            throws IOException {
        final var identifier = decodeNodeIdentifier(nodeHeader);
        LOG.trace("Streaming leaf node {}", identifier);
        writer.startLeafNode(identifier);

        final Object value;
        if ((nodeHeader & PotassiumNode.PREDICATE_ONE) == PotassiumNode.PREDICATE_ONE) {
            if (!(parent instanceof NodeIdentifierWithPredicates nip)) {
                throw new InvalidNormalizedNodeStreamException("Invalid predicate leaf " + identifier + " in parent "
                        + parent);
            }

            value = nip.getValue(identifier.getNodeType());
            if (value == null) {
                throw new InvalidNormalizedNodeStreamException("Failed to find predicate leaf " + identifier
                    + " in parent " + parent);
            }
        } else {
            value = readLeafValue();
        }

        writer.scalarValue(value);
        writer.endNode();
    }

    private void streamLeafset(final NormalizedNodeStreamWriter writer, final byte nodeHeader) throws IOException {
        final var identifier = decodeNodeIdentifier(nodeHeader);
        LOG.trace("Streaming leaf set node {}", identifier);
        writer.startLeafSet(identifier, UNKNOWN_SIZE);
        commonStreamContainer(writer, identifier);
    }

    private void streamLeafsetOrdered(final NormalizedNodeStreamWriter writer, final byte nodeHeader)
            throws IOException {
        final var identifier = decodeNodeIdentifier(nodeHeader);
        LOG.trace("Streaming ordered leaf set node {}", identifier);
        writer.startOrderedLeafSet(identifier, UNKNOWN_SIZE);

        commonStreamContainer(writer, identifier);
    }

    private void streamLeafsetEntry(final NormalizedNodeStreamWriter writer, final PathArgument parent,
            final byte nodeHeader) throws IOException {
        final var nodeId = decodeNodeIdentifier(nodeHeader, parent);
        final var value = readLeafValue();
        final var leafIdentifier = new NodeWithValue<>(nodeId.getNodeType(), value);
        LOG.trace("Streaming leaf set entry node {}", leafIdentifier);
        writer.startLeafSetEntryNode(leafIdentifier);
        writer.scalarValue(value);
        writer.endNode();
    }

    private void streamList(final NormalizedNodeStreamWriter writer, final byte nodeHeader) throws IOException {
        final var identifier = decodeNodeIdentifier(nodeHeader);
        writer.startUnkeyedList(identifier, UNKNOWN_SIZE);
        commonStreamContainer(writer, identifier);
    }

    private void streamListEntry(final NormalizedNodeStreamWriter writer, final PathArgument parent,
            final byte nodeHeader) throws IOException {
        final var identifier = decodeNodeIdentifier(nodeHeader, parent);
        LOG.trace("Streaming unkeyed list item node {}", identifier);
        writer.startUnkeyedListItem(identifier, UNKNOWN_SIZE);
        commonStreamContainer(writer, identifier);
    }

    private void streamMap(final NormalizedNodeStreamWriter writer, final byte nodeHeader) throws IOException {
        final var identifier = decodeNodeIdentifier(nodeHeader);
        LOG.trace("Streaming map node {}", identifier);
        writer.startMapNode(identifier, UNKNOWN_SIZE);
        commonStreamContainer(writer, identifier);
    }

    private void streamMapOrdered(final NormalizedNodeStreamWriter writer, final byte nodeHeader) throws IOException {
        final var identifier = decodeNodeIdentifier(nodeHeader);
        LOG.trace("Streaming ordered map node {}", identifier);
        writer.startOrderedMapNode(identifier, UNKNOWN_SIZE);
        commonStreamContainer(writer, identifier);
    }

    private void streamMapEntry(final NormalizedNodeStreamWriter writer, final PathArgument parent,
            final byte nodeHeader) throws IOException {
        final var nodeId = decodeNodeIdentifier(nodeHeader, parent);

        final int size = switch (mask(nodeHeader, PotassiumNode.PREDICATE_MASK)) {
            case PotassiumNode.PREDICATE_ZERO -> 0;
            case PotassiumNode.PREDICATE_ONE -> 1;
            case PotassiumNode.PREDICATE_1B -> input.readUnsignedByte();
            case PotassiumNode.PREDICATE_4B -> input.readInt();
            default ->
                // ISE on purpose: this should never ever happen
                throw new IllegalStateException("Failed to decode NodeIdentifierWithPredicates size from header "
                    + nodeHeader);
        };
        final var identifier = readNodeIdentifierWithPredicates(nodeId.getNodeType(), size);
        LOG.trace("Streaming map entry node {}", identifier);
        writer.startMapEntryNode(identifier, UNKNOWN_SIZE);
        commonStreamContainer(writer, identifier);
    }

    private void commonStreamContainer(final NormalizedNodeStreamWriter writer, final PathArgument parent)
            throws IOException {
        for (byte nodeType = input.readByte(); nodeType != PotassiumNode.NODE_END; nodeType = input.readByte()) {
            streamNormalizedNode(writer, parent, nodeType);
        }
        writer.endNode();
    }

    private @NonNull NodeIdentifier decodeNodeIdentifier() throws IOException {
        final var module = decodeQNameModule();
        final var localName = readRefString();
        final NodeIdentifier nodeId;
        try {
            nodeId = QNameFactory.getNodeIdentifier(module, localName);
        } catch (ExecutionException e) {
            throw new InvalidNormalizedNodeStreamException("Illegal QName module=" + module + " localName="
                    + localName, e);
        }

        codedNodeIdentifiers.add(nodeId);
        return nodeId;
    }

    private NodeIdentifier decodeNodeIdentifier(final byte nodeHeader) throws IOException {
        return decodeNodeIdentifier(nodeHeader, null);
    }

    private NodeIdentifier decodeNodeIdentifier(final byte nodeHeader, final PathArgument parent) throws IOException {
        return switch (nodeHeader & PotassiumNode.ADDR_MASK) {
            case PotassiumNode.ADDR_DEFINE -> readNodeIdentifier();
            case PotassiumNode.ADDR_LOOKUP_1B -> lookupNodeIdentifier(input.readUnsignedByte());
            case PotassiumNode.ADDR_LOOKUP_4B -> lookupNodeIdentifier(input.readInt());
            case PotassiumNode.ADDR_PARENT -> {
                if (parent instanceof NodeIdentifier nid) {
                    yield nid;
                }
                throw new InvalidNormalizedNodeStreamException("Invalid node identifier reference to parent " + parent);
            }
            default -> throw new InvalidNormalizedNodeStreamException("Unexpected node identifier addressing in header "
                + nodeHeader);
        };
    }

    @Override
    public YangInstanceIdentifier readYangInstanceIdentifier() throws IOException {
        final byte type = input.readByte();
        if (type == PotassiumValue.YIID) {
            return readYangInstanceIdentifier(input.readInt());
        } else if (type >= PotassiumValue.YIID_0) {
            // Note 'byte' is range limited, so it is always '&& type <= PotassiumValue.YIID_31'
            return readYangInstanceIdentifier(type - PotassiumValue.YIID_0);
        } else {
            throw new InvalidNormalizedNodeStreamException("Unexpected YangInstanceIdentifier type " + type);
        }
    }

    private @NonNull YangInstanceIdentifier readYangInstanceIdentifier(final int size) throws IOException {
        if (size > 0) {
            final var builder = ImmutableList.<PathArgument>builderWithExpectedSize(size);
            for (int i = 0; i < size; ++i) {
                builder.add(readPathArgument());
            }
            return YangInstanceIdentifier.of(builder.build());
        } else if (size == 0) {
            return YangInstanceIdentifier.of();
        } else {
            throw new InvalidNormalizedNodeStreamException("Invalid YangInstanceIdentifier size " + size);
        }
    }

    @Override
    public QName readQName() throws IOException {
        final byte type = input.readByte();
        return switch (type) {
            case PotassiumValue.QNAME -> decodeQName();
            case PotassiumValue.QNAME_REF_1B -> decodeQNameRef1();
            case PotassiumValue.QNAME_REF_2B -> decodeQNameRef2();
            case PotassiumValue.QNAME_REF_4B -> decodeQNameRef4();
            default -> throw new InvalidNormalizedNodeStreamException("Unexpected QName type " + type);
        };
    }

    @Override
    public PathArgument readPathArgument() throws IOException {
        final byte header = input.readByte();
        return switch (header & PotassiumPathArgument.TYPE_MASK) {
            case PotassiumPathArgument.NODE_IDENTIFIER -> {
                verifyPathIdentifierOnly(header);
                yield readNodeIdentifier(header);
            }
            case PotassiumPathArgument.NODE_IDENTIFIER_WITH_PREDICATES -> readNodeIdentifierWithPredicates(header);
            case PotassiumPathArgument.NODE_WITH_VALUE -> {
                verifyPathIdentifierOnly(header);
                yield readNodeWithValue(header);
            }
            default -> throw new InvalidNormalizedNodeStreamException("Unexpected PathArgument header " + header);
        };
    }

    @Override
    @Deprecated(since = "11.0.0", forRemoval = true)
    public Either<PathArgument, LegacyPathArgument> readLegacyPathArgument() throws IOException {
        return Either.ofFirst(readPathArgument());
    }

    private @NonNull NodeIdentifier readNodeIdentifier() throws IOException {
        return decodeNodeIdentifier();
    }

    private @NonNull NodeIdentifier readNodeIdentifier(final byte header) throws IOException {
        return switch (header & PotassiumPathArgument.QNAME_MASK) {
            case PotassiumPathArgument.QNAME_DEF -> decodeNodeIdentifier();
            case PotassiumPathArgument.QNAME_REF_1B -> decodeNodeIdentifierRef1();
            case PotassiumPathArgument.QNAME_REF_2B -> decodeNodeIdentifierRef2();
            case PotassiumPathArgument.QNAME_REF_4B -> decodeNodeIdentifierRef4();
            default -> throw new InvalidNormalizedNodeStreamException("Invalid QName coding in " + header);
        };
    }

    private @NonNull  NodeIdentifierWithPredicates readNodeIdentifierWithPredicates(final byte header)
            throws IOException {
        final var qname = readNodeIdentifier(header).getNodeType();
        return switch (mask(header, PotassiumPathArgument.SIZE_MASK)) {
            case PotassiumPathArgument.SIZE_1B -> readNodeIdentifierWithPredicates(qname, input.readUnsignedByte());
            case PotassiumPathArgument.SIZE_2B -> readNodeIdentifierWithPredicates(qname, input.readUnsignedShort());
            case PotassiumPathArgument.SIZE_4B -> readNodeIdentifierWithPredicates(qname, input.readInt());
            default -> readNodeIdentifierWithPredicates(qname, rshift(header, PotassiumPathArgument.SIZE_SHIFT));
        };
    }

    private @NonNull NodeIdentifierWithPredicates readNodeIdentifierWithPredicates(final QName qname, final int size)
            throws IOException {
        if (size == 1) {
            return NodeIdentifierWithPredicates.of(qname, readQName(), readLeafValue());
        } else if (size > 1) {
            final var builder = ImmutableMap.<QName, Object>builderWithExpectedSize(size);
            for (int i = 0; i < size; ++i) {
                builder.put(readQName(), readLeafValue());
            }
            return NodeIdentifierWithPredicates.of(qname, builder.build());
        } else if (size == 0) {
            return NodeIdentifierWithPredicates.of(qname);
        } else {
            throw new InvalidNormalizedNodeStreamException("Invalid predicate count " + size);
        }
    }

    private @NonNull NodeWithValue<?> readNodeWithValue(final byte header) throws IOException {
        final var qname = readNodeIdentifier(header).getNodeType();
        return new NodeWithValue<>(qname, readLeafValue());
    }

    private static void verifyPathIdentifierOnly(final byte header) throws InvalidNormalizedNodeStreamException {
        if (mask(header, PotassiumPathArgument.SIZE_MASK) != 0) {
            throw new InvalidNormalizedNodeStreamException("Invalid path argument header " + header);
        }
    }

    private @NonNull NodeIdentifier decodeNodeIdentifierRef1() throws IOException {
        return lookupNodeIdentifier(input.readUnsignedByte());
    }

    private @NonNull NodeIdentifier decodeNodeIdentifierRef2() throws IOException {
        return lookupNodeIdentifier(input.readUnsignedShort() + 256);
    }

    private @NonNull NodeIdentifier decodeNodeIdentifierRef4() throws IOException {
        return lookupNodeIdentifier(input.readInt());
    }

    private @NonNull QName decodeQName() throws IOException {
        return decodeNodeIdentifier().getNodeType();
    }

    private @NonNull QName decodeQNameRef1() throws IOException {
        return lookupQName(input.readUnsignedByte());
    }

    private @NonNull QName decodeQNameRef2() throws IOException {
        return lookupQName(input.readUnsignedShort() + 256);
    }

    private @NonNull QName decodeQNameRef4() throws IOException {
        return lookupQName(input.readInt());
    }

    private @NonNull QNameModule decodeQNameModule() throws IOException {
        final byte type = input.readByte();
        return switch (type) {
            case PotassiumValue.MODREF_1B -> lookupQNameModule(input.readUnsignedByte());
            case PotassiumValue.MODREF_2B -> lookupQNameModule(input.readUnsignedShort() + 256);
            case PotassiumValue.MODREF_4B -> lookupQNameModule(input.readInt());
            default -> decodeQNameModuleDef(type);
        };
    }

    private @NonNull QNameModule lookupQNameModule(final int index) throws InvalidNormalizedNodeStreamException {
        try {
            return codedModules.get(index);
        } catch (IndexOutOfBoundsException e) {
            throw new InvalidNormalizedNodeStreamException("Invalid QNameModule reference " + index, e);
        }
    }

    // QNameModule definition, i.e. two encoded strings
    private @NonNull QNameModule decodeQNameModuleDef(final byte type) throws IOException {
        final String namespace = readRefString(type);

        final byte refType = input.readByte();
        final String revision = refType == PotassiumValue.STRING_EMPTY ? null : readRefString(refType);
        final QNameModule module;
        try {
            module = QNameFactory.createModule(namespace, revision);
        } catch (UncheckedExecutionException e) {
            throw new InvalidNormalizedNodeStreamException("Illegal QNameModule ns=" + namespace + " rev=" + revision,
                e);
        }

        codedModules.add(module);
        return module;
    }

    private @NonNull String readRefString() throws IOException {
        return readRefString(input.readByte());
    }

    private @NonNull String readRefString(final byte type) throws IOException {
        return switch (type) {
            case PotassiumValue.STRING_REF_1B -> lookupString(input.readUnsignedByte());
            case PotassiumValue.STRING_REF_2B -> lookupString(input.readUnsignedShort() + 256);
            case PotassiumValue.STRING_REF_4B -> lookupString(input.readInt());
            case PotassiumValue.STRING_EMPTY -> "";
            case PotassiumValue.STRING_2B -> defineString(readString2());
            case PotassiumValue.STRING_4B -> defineString(readString4());
            case PotassiumValue.STRING_CHARS -> defineString(readCharsString());
            case PotassiumValue.STRING_UTF -> defineString(input.readUTF());
            default -> throw new InvalidNormalizedNodeStreamException("Unexpected String type " + type);
        };
    }

    private @NonNull String defineString(final String str) {
        // TODO: consider interning Strings -- that would help with bits, but otherwise it's probably not worth it
        final var ret = verifyNotNull(str);
        codedStrings.add(ret);
        return ret;
    }

    private @NonNull String readString() throws IOException {
        final byte type = input.readByte();
        return switch (type) {
            case PotassiumValue.STRING_EMPTY -> "";
            case PotassiumValue.STRING_UTF -> input.readUTF();
            case PotassiumValue.STRING_2B -> readString2();
            case PotassiumValue.STRING_4B -> readString4();
            case PotassiumValue.STRING_CHARS -> readCharsString();
            default -> throw new InvalidNormalizedNodeStreamException("Unexpected String type " + type);
        };
    }

    private @NonNull String readString2() throws IOException {
        return readByteString(input.readUnsignedShort());
    }

    private @NonNull String readString4() throws IOException {
        return readByteString(input.readInt());
    }

    private @NonNull String readByteString(final int size) throws IOException {
        if (size > 0) {
            final var bytes = new byte[size];
            input.readFully(bytes);
            return new String(bytes, StandardCharsets.UTF_8);
        } else if (size == 0) {
            return "";
        } else {
            throw new InvalidNormalizedNodeStreamException("Invalid String bytes length " + size);
        }
    }

    private @NonNull String readCharsString() throws IOException {
        final int size = input.readInt();
        if (size > 0) {
            final var chars = new char[size];
            for (int i = 0; i < size; ++i) {
                chars[i] = input.readChar();
            }
            return String.valueOf(chars);
        } else if (size == 0) {
            return "";
        } else {
            throw new InvalidNormalizedNodeStreamException("Invalid String chars length " + size);
        }
    }

    private @NonNull NodeIdentifier lookupNodeIdentifier(final int index) throws InvalidNormalizedNodeStreamException {
        try {
            return codedNodeIdentifiers.get(index);
        } catch (IndexOutOfBoundsException e) {
            throw new InvalidNormalizedNodeStreamException("Invalid QName reference " + index, e);
        }
    }

    private @NonNull QName lookupQName(final int index) throws InvalidNormalizedNodeStreamException {
        return lookupNodeIdentifier(index).getNodeType();
    }

    private @NonNull String lookupString(final int index) throws InvalidNormalizedNodeStreamException {
        try {
            return codedStrings.get(index);
        } catch (IndexOutOfBoundsException e) {
            throw new InvalidNormalizedNodeStreamException("Invalid String reference " + index, e);
        }
    }

    private @NonNull DOMSource readDOMSource() throws IOException {
        final String str = readString();
        try {
            return new DOMSource(UntrustedXML.newDocumentBuilder().parse(new InputSource(new StringReader(str)))
                .getDocumentElement());
        } catch (SAXException e) {
            throw new IOException("Error parsing XML: " + str, e);
        }
    }

    private @NonNull Object readLeafValue() throws IOException {
        final byte type = input.readByte();
        return switch (type) {
            case PotassiumValue.BOOLEAN_FALSE -> Boolean.FALSE;
            case PotassiumValue.BOOLEAN_TRUE -> Boolean.TRUE;
            case PotassiumValue.EMPTY -> Empty.value();
            case PotassiumValue.INT8 -> input.readByte();
            case PotassiumValue.INT8_0 -> INT8_0;
            case PotassiumValue.INT16 -> input.readShort();
            case PotassiumValue.INT16_0 -> INT16_0;
            case PotassiumValue.INT32 -> input.readInt();
            case PotassiumValue.INT32_0 -> INT32_0;
            case PotassiumValue.INT32_2B -> input.readShort() & 0xFFFF;
            case PotassiumValue.INT64 -> input.readLong();
            case PotassiumValue.INT64_0 -> INT64_0;
            case PotassiumValue.INT64_4B -> input.readInt() & 0xFFFFFFFFL;
            case PotassiumValue.UINT8 -> Uint8.fromByteBits(input.readByte());
            case PotassiumValue.UINT8_0 -> Uint8.ZERO;
            case PotassiumValue.UINT16 -> Uint16.fromShortBits(input.readShort());
            case PotassiumValue.UINT16_0 -> Uint16.ZERO;
            case PotassiumValue.UINT32 -> Uint32.fromIntBits(input.readInt());
            case PotassiumValue.UINT32_0 -> Uint32.ZERO;
            case PotassiumValue.UINT32_2B -> Uint32.fromIntBits(input.readShort() & 0xFFFF);
            case PotassiumValue.UINT64 -> Uint64.fromLongBits(input.readLong());
            case PotassiumValue.UINT64_0 -> Uint64.ZERO;
            case PotassiumValue.UINT64_4B -> Uint64.fromLongBits(input.readInt() & 0xFFFFFFFFL);
            case PotassiumValue.DECIMAL64 -> Decimal64.of(input.readByte(), WritableObjects.readLong(input));
            case PotassiumValue.STRING_EMPTY -> "";
            case PotassiumValue.STRING_UTF -> input.readUTF();
            case PotassiumValue.STRING_2B -> readString2();
            case PotassiumValue.STRING_4B -> readString4();
            case PotassiumValue.STRING_CHARS -> readCharsString();
            case PotassiumValue.BINARY_0 -> BINARY_0;
            case PotassiumValue.BINARY_1B -> readBinary(128 + input.readUnsignedByte());
            case PotassiumValue.BINARY_2B -> readBinary(384 + input.readUnsignedShort());
            case PotassiumValue.BINARY_4B -> readBinary(input.readInt());
            case PotassiumValue.YIID_0 -> YangInstanceIdentifier.of();
            case PotassiumValue.YIID -> readYangInstanceIdentifier(input.readInt());
            case PotassiumValue.QNAME -> decodeQName();
            case PotassiumValue.QNAME_REF_1B -> decodeQNameRef1();
            case PotassiumValue.QNAME_REF_2B -> decodeQNameRef2();
            case PotassiumValue.QNAME_REF_4B -> decodeQNameRef4();
            case PotassiumValue.BITS_0 -> ImmutableSet.of();
            case PotassiumValue.BITS_1B -> readBits(input.readUnsignedByte() + 29);
            case PotassiumValue.BITS_2B -> readBits(input.readUnsignedShort() + 285);
            case PotassiumValue.BITS_4B -> readBits(input.readInt());
            default -> {
                if (type > PotassiumValue.BINARY_0 && type <= PotassiumValue.BINARY_127) {
                    yield readBinary(type - PotassiumValue.BINARY_0);
                } else if (type > PotassiumValue.BITS_0 && type < PotassiumValue.BITS_1B) {
                    yield readBits(type - PotassiumValue.BITS_0);
                } else if (type > PotassiumValue.YIID_0) {
                    // Note 'byte' is range limited, so it is always '&& type <= PotassiumValue.YIID_31'
                    yield readYangInstanceIdentifier(type - PotassiumValue.YIID_0);
                } else {
                    throw new InvalidNormalizedNodeStreamException("Invalid value type " + type);
                }
            }
        };
    }

    private byte @NonNull [] readBinary(final int size) throws IOException {
        if (size > 0) {
            final var ret = new byte[size];
            input.readFully(ret);
            return ret;
        } else if (size == 0) {
            return BINARY_0;
        } else {
            throw new InvalidNormalizedNodeStreamException("Invalid binary length " + size);
        }
    }

    private @NonNull ImmutableSet<String> readBits(final int size) throws IOException {
        if (size > 0) {
            final var builder = ImmutableSet.<String>builderWithExpectedSize(size);
            for (int i = 0; i < size; ++i) {
                builder.add(readRefString());
            }
            return builder.build();
        } else if (size == 0) {
            return ImmutableSet.of();
        } else {
            throw new InvalidNormalizedNodeStreamException("Invalid bits length " + size);
        }
    }

    private static byte mask(final byte header, final byte mask) {
        return (byte) (header & mask);
    }

    private static int rshift(final byte header, final byte shift) {
        return (header & 0xFF) >>> shift;
    }
}
