/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.binfmt;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.DataOutput;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.WritableObjects;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for NormalizedNodeDataOutput based on {@link PotassiumNode}, {@link PotassiumPathArgument} and
 * {@link PotassiumValue}.
 */
final class PotassiumDataOutput extends AbstractNormalizedNodeDataOutput {
    private static final Logger LOG = LoggerFactory.getLogger(PotassiumDataOutput.class);

    // Marker for encoding state when we have entered startLeafNode() within a startMapEntry() and that leaf corresponds
    // to a key carried within NodeIdentifierWithPredicates.
    private static final Object KEY_LEAF_STATE = new Object();
    // Marker for nodes which have simple content and do not use END_NODE marker to terminate
    private static final Object NO_ENDNODE_STATE = new Object();

    private static final TransformerFactory TF = TransformerFactory.newInstance();

    /**
     * Stack tracking encoding state. In general we track the node identifier of the currently-open element, but there
     * are a few other circumstances where we push other objects. See {@link #KEY_LEAF_STATE} and
     * {@link #NO_ENDNODE_STATE}.
     */
    private final Deque<Object> stack = new ArrayDeque<>();

    // Coding maps
    private final Map<QNameModule, Integer> moduleCodeMap = new HashMap<>();
    private final Map<String, Integer> stringCodeMap = new HashMap<>();
    private final Map<QName, Integer> qnameCodeMap = new HashMap<>();

    PotassiumDataOutput(final DataOutput output) {
        super(output);
    }

    @Override
    public void startLeafNode(final NodeIdentifier name) throws IOException {
        final Object current = stack.peek();
        if (current instanceof NodeIdentifierWithPredicates nip) {
            final QName qname = name.getNodeType();
            if (nip.containsKey(qname)) {
                writeQNameNode(PotassiumNode.NODE_LEAF | PotassiumNode.PREDICATE_ONE, qname);
                stack.push(KEY_LEAF_STATE);
                return;
            }
        }

        startSimpleNode(PotassiumNode.NODE_LEAF, name);
    }

    @Override
    public void startLeafSet(final NodeIdentifier name, final int childSizeHint) throws IOException {
        startQNameNode(PotassiumNode.NODE_LEAFSET, name);
    }

    @Override
    public void startOrderedLeafSet(final NodeIdentifier name, final int childSizeHint) throws IOException {
        startQNameNode(PotassiumNode.NODE_LEAFSET_ORDERED, name);
    }

    @Override
    public void startLeafSetEntryNode(final NodeWithValue<?> name) throws IOException {
        if (matchesParentQName(name.getNodeType())) {
            output.writeByte(PotassiumNode.NODE_LEAFSET_ENTRY);
            stack.push(NO_ENDNODE_STATE);
        } else {
            startSimpleNode(PotassiumNode.NODE_LEAFSET_ENTRY, name);
        }
    }

    @Override
    public void startContainerNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        startQNameNode(PotassiumNode.NODE_CONTAINER, name);
    }

    @Override
    public void startUnkeyedList(final NodeIdentifier name, final int childSizeHint) throws IOException {
        startQNameNode(PotassiumNode.NODE_LIST, name);
    }

    @Override
    public void startUnkeyedListItem(final NodeIdentifier name, final int childSizeHint) throws IOException {
        startInheritedNode(PotassiumNode.NODE_LIST_ENTRY, name);
    }

    @Override
    public void startMapNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        startQNameNode(PotassiumNode.NODE_MAP, name);
    }

    @Override
    public void startMapEntryNode(final NodeIdentifierWithPredicates identifier, final int childSizeHint)
            throws IOException {
        final int size = identifier.size();
        if (size == 1) {
            startInheritedNode((byte) (PotassiumNode.NODE_MAP_ENTRY | PotassiumNode.PREDICATE_ONE), identifier);
        } else if (size == 0) {
            startInheritedNode((byte) (PotassiumNode.NODE_MAP_ENTRY | PotassiumNode.PREDICATE_ZERO), identifier);
        } else if (size < 256) {
            startInheritedNode((byte) (PotassiumNode.NODE_MAP_ENTRY | PotassiumNode.PREDICATE_1B), identifier);
            output.writeByte(size);
        } else {
            startInheritedNode((byte) (PotassiumNode.NODE_MAP_ENTRY | PotassiumNode.PREDICATE_4B), identifier);
            output.writeInt(size);
        }

        writePredicates(identifier);
    }

    @Override
    public void startOrderedMapNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        startQNameNode(PotassiumNode.NODE_MAP_ORDERED, name);
    }

    @Override
    public void startChoiceNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        startQNameNode(PotassiumNode.NODE_CHOICE, name);
    }

    @Override
    public boolean startAnyxmlNode(final NodeIdentifier name, final Class<?> objectModel) throws IOException {
        if (DOMSource.class.isAssignableFrom(objectModel)) {
            startSimpleNode(PotassiumNode.NODE_ANYXML, name);
            return true;
        }
        return false;
    }

    @Override
    public void domSourceValue(final DOMSource value) throws IOException {
        final StringWriter writer = new StringWriter();
        try {
            TF.newTransformer().transform(value, new StreamResult(writer));
        } catch (TransformerException e) {
            throw new IOException("Error writing anyXml", e);
        }
        writeValue(writer.toString());
    }

    @Override
    public void endNode() throws IOException {
        if (stack.pop() instanceof PathArgument) {
            output.writeByte(PotassiumNode.NODE_END);
        }
    }

    @Override
    public void scalarValue(final Object value) throws IOException {
        if (KEY_LEAF_STATE.equals(stack.peek())) {
            LOG.trace("Inside a map entry key leaf, not emitting value {}", value);
        } else {
            writeObject(value);
        }
    }

    @Override
    short streamVersion() {
        return TokenTypes.POTASSIUM_VERSION;
    }

    @Override
    void writeQNameInternal(final QName qname) throws IOException {
        final Integer code = qnameCodeMap.get(qname);
        if (code == null) {
            output.writeByte(PotassiumValue.QNAME);
            encodeQName(qname);
        } else {
            writeQNameRef(code);
        }
    }

    @Override
    void writePathArgumentInternal(final PathArgument pathArgument) throws IOException {
        if (pathArgument instanceof NodeIdentifier nid) {
            writeNodeIdentifier(nid);
        } else if (pathArgument instanceof NodeIdentifierWithPredicates nip) {
            writeNodeIdentifierWithPredicates(nip);
        } else if (pathArgument instanceof NodeWithValue<?> niv) {
            writeNodeWithValue(niv);
        } else {
            throw new IOException("Unhandled PathArgument " + pathArgument);
        }
    }

    private void writeNodeIdentifier(final NodeIdentifier identifier) throws IOException {
        writePathArgumentQName(identifier.getNodeType(), PotassiumPathArgument.NODE_IDENTIFIER);
    }

    private void writeNodeIdentifierWithPredicates(final NodeIdentifierWithPredicates identifier) throws IOException {
        final int size = identifier.size();
        if (size < 13) {
            writePathArgumentQName(identifier.getNodeType(),
                (byte) (PotassiumPathArgument.NODE_IDENTIFIER_WITH_PREDICATES
                        | size << PotassiumPathArgument.SIZE_SHIFT));
        } else if (size < 256) {
            writePathArgumentQName(identifier.getNodeType(),
                (byte) (PotassiumPathArgument.NODE_IDENTIFIER_WITH_PREDICATES | PotassiumPathArgument.SIZE_1B));
            output.writeByte(size);
        } else if (size < 65536) {
            writePathArgumentQName(identifier.getNodeType(),
                (byte) (PotassiumPathArgument.NODE_IDENTIFIER_WITH_PREDICATES | PotassiumPathArgument.SIZE_2B));
            output.writeShort(size);
        } else {
            writePathArgumentQName(identifier.getNodeType(),
                (byte) (PotassiumPathArgument.NODE_IDENTIFIER_WITH_PREDICATES | PotassiumPathArgument.SIZE_4B));
            output.writeInt(size);
        }

        writePredicates(identifier);
    }

    private void writePredicates(final NodeIdentifierWithPredicates identifier) throws IOException {
        for (Entry<QName, Object> e : identifier.entrySet()) {
            writeQNameInternal(e.getKey());
            writeObject(e.getValue());
        }
    }

    private void writeNodeWithValue(final NodeWithValue<?> identifier) throws IOException {
        writePathArgumentQName(identifier.getNodeType(), PotassiumPathArgument.NODE_WITH_VALUE);
        writeObject(identifier.getValue());
    }

    private void writePathArgumentQName(final QName qname, final byte typeHeader) throws IOException {
        final Integer code = qnameCodeMap.get(qname);
        if (code != null) {
            final int val = code;
            if (val < 256) {
                output.writeByte(typeHeader | PotassiumPathArgument.QNAME_REF_1B);
                output.writeByte(val);
            } else if (val < 65792) {
                output.writeByte(typeHeader | PotassiumPathArgument.QNAME_REF_2B);
                output.writeShort(val - 256);
            } else {
                output.writeByte(typeHeader | PotassiumPathArgument.QNAME_REF_4B);
                output.writeInt(val);
            }
        } else {
            // implied '| PotassiumPathArgument.QNAME_DEF'
            output.writeByte(typeHeader);
            encodeQName(qname);
        }
    }

    @Override
    void writeYangInstanceIdentifierInternal(final YangInstanceIdentifier identifier) throws IOException {
        writeValue(identifier);
    }

    private void writeObject(final @NonNull Object value) throws IOException {
        if (value instanceof String str) {
            writeValue(str);
        } else if (value instanceof Boolean bool) {
            writeValue(bool);
        } else if (value instanceof Byte byteVal) {
            writeValue(byteVal);
        } else if (value instanceof Short shortVal) {
            writeValue(shortVal);
        } else if (value instanceof Integer intVal) {
            writeValue(intVal);
        } else if (value instanceof Long longVal) {
            writeValue(longVal);
        } else if (value instanceof Uint8 uint8) {
            writeValue(uint8);
        } else if (value instanceof Uint16 uint16) {
            writeValue(uint16);
        } else if (value instanceof Uint32 uint32) {
            writeValue(uint32);
        } else if (value instanceof Uint64 uint64) {
            writeValue(uint64);
        } else if (value instanceof QName qname) {
            writeQNameInternal(qname);
        } else if (value instanceof YangInstanceIdentifier id) {
            writeValue(id);
        } else if (value instanceof byte[] bytes) {
            writeValue(bytes);
        } else if (value instanceof Empty) {
            output.writeByte(PotassiumValue.EMPTY);
        } else if (value instanceof Set<?> set) {
            writeValue(set);
        } else if (value instanceof Decimal64 decimal) {
            output.writeByte(PotassiumValue.DECIMAL64);
            output.writeByte(decimal.scale());
            WritableObjects.writeLong(output, decimal.unscaledValue());
        } else {
            throw new IOException("Unhandled value type " + value.getClass());
        }
    }

    private void writeValue(final boolean value) throws IOException {
        output.writeByte(value ? PotassiumValue.BOOLEAN_TRUE : PotassiumValue.BOOLEAN_FALSE);
    }

    private void writeValue(final byte value) throws IOException {
        if (value != 0) {
            output.writeByte(PotassiumValue.INT8);
            output.writeByte(value);
        } else {
            output.writeByte(PotassiumValue.INT8_0);
        }
    }

    private void writeValue(final short value) throws IOException {
        if (value != 0) {
            output.writeByte(PotassiumValue.INT16);
            output.writeShort(value);
        } else {
            output.writeByte(PotassiumValue.INT16_0);
        }
    }

    private void writeValue(final int value) throws IOException {
        if ((value & 0xFFFF0000) != 0) {
            output.writeByte(PotassiumValue.INT32);
            output.writeInt(value);
        } else if (value != 0) {
            output.writeByte(PotassiumValue.INT32_2B);
            output.writeShort(value);
        } else {
            output.writeByte(PotassiumValue.INT32_0);
        }
    }

    private void writeValue(final long value) throws IOException {
        if ((value & 0xFFFFFFFF00000000L) != 0) {
            output.writeByte(PotassiumValue.INT64);
            output.writeLong(value);
        } else if (value != 0) {
            output.writeByte(PotassiumValue.INT64_4B);
            output.writeInt((int) value);
        } else {
            output.writeByte(PotassiumValue.INT64_0);
        }
    }

    private void writeValue(final Uint8 value) throws IOException {
        final byte b = value.byteValue();
        if (b != 0) {
            output.writeByte(PotassiumValue.UINT8);
            output.writeByte(b);
        } else {
            output.writeByte(PotassiumValue.UINT8_0);
        }
    }

    private void writeValue(final Uint16 value) throws IOException {
        final short s = value.shortValue();
        if (s != 0) {
            output.writeByte(PotassiumValue.UINT16);
            output.writeShort(s);
        } else {
            output.writeByte(PotassiumValue.UINT16_0);
        }
    }

    private void writeValue(final Uint32 value) throws IOException {
        final int i = value.intValue();
        if ((i & 0xFFFF0000) != 0) {
            output.writeByte(PotassiumValue.UINT32);
            output.writeInt(i);
        } else if (i != 0) {
            output.writeByte(PotassiumValue.UINT32_2B);
            output.writeShort(i);
        } else {
            output.writeByte(PotassiumValue.UINT32_0);
        }
    }

    private void writeValue(final Uint64 value) throws IOException {
        final long l = value.longValue();
        if ((l & 0xFFFFFFFF00000000L) != 0) {
            output.writeByte(PotassiumValue.UINT64);
            output.writeLong(l);
        } else if (l != 0) {
            output.writeByte(PotassiumValue.UINT64_4B);
            output.writeInt((int) l);
        } else {
            output.writeByte(PotassiumValue.UINT64_0);
        }
    }

    private void writeValue(final String value) throws IOException {
        if (value.isEmpty()) {
            output.writeByte(PotassiumValue.STRING_EMPTY);
        } else if (value.length() <= Short.MAX_VALUE / 2) {
            output.writeByte(PotassiumValue.STRING_UTF);
            output.writeUTF(value);
        } else if (value.length() <= 1048576) {
            final byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            if (bytes.length < 65536) {
                output.writeByte(PotassiumValue.STRING_2B);
                output.writeShort(bytes.length);
            } else {
                output.writeByte(PotassiumValue.STRING_4B);
                output.writeInt(bytes.length);
            }
            output.write(bytes);
        } else {
            output.writeByte(PotassiumValue.STRING_CHARS);
            output.writeInt(value.length());
            output.writeChars(value);
        }
    }

    private void writeValue(final byte[] value) throws IOException {
        if (value.length < 128) {
            output.writeByte(PotassiumValue.BINARY_0 + value.length);
        } else if (value.length < 384) {
            output.writeByte(PotassiumValue.BINARY_1B);
            output.writeByte(value.length - 128);
        } else if (value.length < 65920) {
            output.writeByte(PotassiumValue.BINARY_2B);
            output.writeShort(value.length - 384);
        } else {
            output.writeByte(PotassiumValue.BINARY_4B);
            output.writeInt(value.length);
        }
        output.write(value);
    }

    private void writeValue(final YangInstanceIdentifier value) throws IOException {
        final List<PathArgument> args = value.getPathArguments();
        final int size = args.size();
        if (size > 31) {
            output.writeByte(PotassiumValue.YIID);
            output.writeInt(size);
        } else {
            output.writeByte(PotassiumValue.YIID_0 + size);
        }
        for (PathArgument arg : args) {
            writePathArgumentInternal(arg);
        }
    }

    private void writeValue(final Set<?> value) throws IOException {
        final int size = value.size();
        if (size < 29) {
            output.writeByte(PotassiumValue.BITS_0 + size);
        } else if (size < 285) {
            output.writeByte(PotassiumValue.BITS_1B);
            output.writeByte(size - 29);
        } else if (size < 65821) {
            output.writeByte(PotassiumValue.BITS_2B);
            output.writeShort(size - 285);
        } else {
            output.writeByte(PotassiumValue.BITS_4B);
            output.writeInt(size);
        }

        for (Object bit : value) {
            checkArgument(bit instanceof String, "Expected value type to be String but was %s", bit);
            encodeString((String) bit);
        }
    }

    // Check if the proposed QName matches the parent. This is only effective if the parent is identified by
    // NodeIdentifier -- which is typically true
    private boolean matchesParentQName(final QName qname) {
        final Object current = stack.peek();
        return current instanceof NodeIdentifier nid && qname.equals(nid.getNodeType());
    }

    // Start an END_NODE-terminated node, which typically has a QName matching the parent. If that is the case we emit
    // a parent reference instead of an explicit QName reference -- saving at least one byte
    private void startInheritedNode(final byte type, final PathArgument name) throws IOException {
        final QName qname = name.getNodeType();
        if (matchesParentQName(qname)) {
            output.write(type);
        } else {
            writeQNameNode(type, qname);
        }
        stack.push(name);
    }

    // Start an END_NODE-terminated node, which needs its QName encoded
    private void startQNameNode(final byte type, final PathArgument name) throws IOException {
        writeQNameNode(type, name.getNodeType());
        stack.push(name);
    }

    // Start a simple node, which is not terminated through END_NODE and encode its QName
    private void startSimpleNode(final byte type, final PathArgument name) throws IOException {
        writeQNameNode(type, name.getNodeType());
        stack.push(NO_ENDNODE_STATE);
    }

    // Encode a QName-based (i.e. NodeIdentifier*) node with a particular QName. This will either result in a QName
    // definition, or a reference, where this is encoded along with the node type.
    private void writeQNameNode(final int type, final @NonNull QName qname) throws IOException {
        final Integer code = qnameCodeMap.get(qname);
        if (code == null) {
            output.writeByte(type | PotassiumNode.ADDR_DEFINE);
            encodeQName(qname);
        } else {
            writeNodeType(type, code);
        }
    }

    // Write a node type + lookup
    private void writeNodeType(final int type, final int code) throws IOException {
        if (code <= 255) {
            output.writeByte(type | PotassiumNode.ADDR_LOOKUP_1B);
            output.writeByte(code);
        } else {
            output.writeByte(type | PotassiumNode.ADDR_LOOKUP_4B);
            output.writeInt(code);
        }
    }

    // Encode a QName using lookup tables, resuling either in a reference to an existing entry, or emitting two
    // String values.
    private void encodeQName(final @NonNull QName qname) throws IOException {
        final Integer prev = qnameCodeMap.put(qname, qnameCodeMap.size());
        if (prev != null) {
            throw new IOException("Internal coding error: attempted to re-encode " + qname + "%s already encoded as "
                    + prev);
        }

        final QNameModule module = qname.getModule();
        final Integer code = moduleCodeMap.get(module);
        if (code == null) {
            moduleCodeMap.put(module, moduleCodeMap.size());
            encodeString(module.namespace().toString());
            final var rev = module.revision();
            if (rev != null) {
                encodeString(rev.toString());
            } else {
                output.writeByte(PotassiumValue.STRING_EMPTY);
            }
        } else {
            writeModuleRef(code);
        }
        encodeString(qname.getLocalName());
    }

    // Encode a String using lookup tables, resulting either in a reference to an existing entry, or emitting as
    // a literal value
    private void encodeString(final @NonNull String str) throws IOException {
        final Integer code = stringCodeMap.get(str);
        if (code != null) {
            writeRef(code);
        } else {
            stringCodeMap.put(str, stringCodeMap.size());
            writeValue(str);
        }
    }

    // Write a QName with a lookup table reference. This is a combination of asserting the value is a QName plus
    // the effects of writeRef()
    private void writeQNameRef(final int code) throws IOException {
        final int val = code;
        if (val < 256) {
            output.writeByte(PotassiumValue.QNAME_REF_1B);
            output.writeByte(val);
        } else if (val < 65792) {
            output.writeByte(PotassiumValue.QNAME_REF_2B);
            output.writeShort(val - 256);
        } else {
            output.writeByte(PotassiumValue.QNAME_REF_4B);
            output.writeInt(val);
        }
    }

    // Write a lookup table reference, which table is being referenced is implied by the caller
    private void writeRef(final int code) throws IOException {
        final int val = code;
        if (val < 256) {
            output.writeByte(PotassiumValue.STRING_REF_1B);
            output.writeByte(val);
        } else if (val < 65792) {
            output.writeByte(PotassiumValue.STRING_REF_2B);
            output.writeShort(val - 256);
        } else {
            output.writeByte(PotassiumValue.STRING_REF_4B);
            output.writeInt(val);
        }
    }

    // Write a lookup module table reference, which table is being referenced is implied by the caller
    private void writeModuleRef(final int code) throws IOException {
        final int val = code;
        if (val < 256) {
            output.writeByte(PotassiumValue.MODREF_1B);
            output.writeByte(val);
        } else if (val < 65792) {
            output.writeByte(PotassiumValue.MODREF_2B);
            output.writeShort(val - 256);
        } else {
            output.writeByte(PotassiumValue.MODREF_4B);
            output.writeInt(val);
        }
    }
}
