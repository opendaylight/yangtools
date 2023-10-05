/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.xml.minidom;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;
import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
import static javax.xml.XMLConstants.NULL_NS_URI;
import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE;
import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
import static javax.xml.XMLConstants.XML_NS_PREFIX;
import static javax.xml.XMLConstants.XML_NS_URI;

import com.google.common.base.VerifyException;
import com.google.errorprone.annotations.DoNotCall;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A {@link XMLStreamReader} backed by a {@link Document}.
 */
public final class DocumentStreamReader implements XMLStreamReader {
    private abstract static sealed class State implements NamespaceContext {
        private Map<String, String> prefixToNamespace;
        private String defaultNamespace;

        @Override
        public final String getNamespaceURI(final String prefix) {
            if (prefix == null) {
                throw new IllegalArgumentException("Missing prefix");
            }
            return switch (prefix) {
                case DEFAULT_NS_PREFIX -> defaultNamespace();
                case XML_NS_PREFIX -> XML_NS_URI;
                case XMLNS_ATTRIBUTE -> XMLNS_ATTRIBUTE_NS_URI;
                default -> lookupNamespace(prefix);
            };
        }

        @Override
        public final String getPrefix(final String namespaceURI) {
            // FIXME: implement this
            throw new UnsupportedOperationException();
        }

        @Override
        public final Iterator<String> getPrefixes(final String namespaceURI) {
            // FIXME: implement this
            throw new UnsupportedOperationException();
        }

        final @NonNull String defaultNamespace() {
            var ret = defaultNamespace;
            if (ret == null) {
                defaultNamespace = ret = computeDefaultNamespace();
            }
            return ret;
        }

        final @NonNull String defaultNamespace(final Iterator<State> it) {
            var ret = defaultNamespace;
            if (ret == null) {
                defaultNamespace = ret = computeDefaultNamespace(it);
            }
            return ret;
        }

        abstract @NonNull String computeDefaultNamespace();

        static final @NonNull String computeDefaultNamespace(final Iterator<State> it) {
            return it.hasNext() ? it.next().defaultNamespace(it) : NULL_NS_URI;
        }

        final @NonNull String lookupNamespace(final @NonNull String prefix) {
            var map = prefixToNamespace;
            if (map == null) {
                // inflate to a singleton map
                final var namespace = computeNamespace(prefix);
                prefixToNamespace = map = Map.of(prefix, namespace);
                return namespace;
            }
            final var existing = map.get(prefix);
            if (existing != null) {
                return existing;
            }
            if (map.size() == 1) {
                prefixToNamespace = map = inflate(map);
            }
            return map.computeIfAbsent(prefix, this::computeNamespace);
        }

        final @NonNull String lookupNamespace(final @NonNull String prefix, final Iterator<State> it) {
            var map = prefixToNamespace;
            if (map == null) {
                // inflate to a singleton map
                final var namespace = computeNamespace(prefix, it);
                prefixToNamespace = map = Map.of(prefix, namespace);
                return namespace;
            }
            final var existing = map.get(prefix);
            if (existing != null) {
                return existing;
            }
            if (map.size() == 1) {
                prefixToNamespace = map = inflate(map);
            }
            return map.computeIfAbsent(prefix, p -> computeNamespace(p, it));
        }


        // inflate to hold up to 3 mappings without resize
        private static Map<String, String> inflate(final Map<String, String> map) {
            final var ret = new HashMap<String, String>(4);
            ret.putAll(map);
            return ret;
        }

        abstract @NonNull String computeNamespace(@NonNull String prefix);

        static final @NonNull String computeNamespace(final @NonNull String prefix, final Iterator<State> it) {
            return it.hasNext() ? it.next().lookupNamespace(prefix, it) : NULL_NS_URI;
        }
    }

    private static final class DocumentState extends State {
        // Internal-facing state
        final @NonNull Document document;

        boolean started;

        DocumentState(final Document document) {
            this.document = requireNonNull(document);
        }

        @Override
        String computeDefaultNamespace() {
            return NULL_NS_URI;
        }

        @Override
        String computeNamespace(final String prefix) {
            return NULL_NS_URI;
        }
    }

    private final class ElementState extends State {
        // Internal-facing state
        final @NonNull Element element;

        Iterator<Attribute> attrs;
        Iterator<Element> elements;
        Attribute attr;
        TextElement text;
        boolean end;

        ElementState(final Element element) {
            this.element = requireNonNull(element);
        }

        @Override String computeDefaultNamespace() {
            var ret = element.attributeValue(null, XMLNS_ATTRIBUTE);
            if (ret == null) {
                ret = computeDefaultNamespace(stash.iterator());
            }
            return ret;
        }

        @Override
        String computeNamespace(final String prefix) {
            var ret = element.attributeValue(XMLNS_ATTRIBUTE_NS_URI, prefix);
            if (ret == null) {
                final var it = stash.iterator();
                it.next();
                ret = it.hasNext() ? it.next().lookupNamespace(prefix, it) : NULL_NS_URI;
            }
            return ret;
        }
    }

    private static final Location UNKNOWN = new Location() {
        @Override
        public String getSystemId() {
            return null;
        }

        @Override
        public String getPublicId() {
            return null;
        }

        @Override
        public int getLineNumber() {
            return -1;
        }

        @Override
        public int getColumnNumber() {
            return -1;
        }

        @Override
        public int getCharacterOffset() {
            return -1;
        }
    };

    // Acts as a stack, i.e. operations are peek()/push()/pop() and iteration order is LIFO
    private final Deque<State> stash = new ArrayDeque<>();

    private State current;
    private final int currentEvent;

    public DocumentStreamReader(final Document document) {
        current = new DocumentState(document);
        currentEvent = START_DOCUMENT;
    }

    @Override
    public Object getProperty(final String name) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int next() throws XMLStreamException {
        var state = current;
        if (state == null || currentEvent == END_DOCUMENT) {
            throw new NoSuchElementException();
        }

        while (true) {
            if (state instanceof DocumentState document) {
                if (document.started) {
                    return END_DOCUMENT;
                }
                document.started = true;
                stash.push(document);
                current = new ElementState(document.document.element());
                return START_ELEMENT;
            }
            if (!(state instanceof ElementState elem)) {
                // We currently have only DocumentState and ElementState
                throw new VerifyException("Unexpected state " + state);
            }

            var attrs = elem.attrs;
            if (attrs == null) {
                elem.attrs = attrs = elem.element.attributes().iterator();
            }
            if (attrs.hasNext()) {
                elem.attr = attrs.next();
                return ATTRIBUTE;
            } else {
                elem.attr = null;
            }

            var children = elem.elements;
            if (children == null) {
                if (elem.element instanceof TextElement text) {
                    elem.text = text;
                    elem.elements = children = Collections.emptyIterator();
                } else if (elem.element instanceof ContainerElement container) {
                    elem.elements = children = container.children().iterator();
                } else {
                    throw new VerifyException("Unexpected element " + elem.element);
                }
            }
            if (children.hasNext()) {
                stash.push(state);
                current = new ElementState(children.next());
                return START_ELEMENT;
            }
            if (!elem.end) {
                elem.end = true;
                return END_ELEMENT;
            }

            // tail recursion
            current = state = stash.pop();
        }
    }

    @Override
    public void require(final int type, final String namespaceURI, final String localName) throws XMLStreamException {
        if (currentEvent != type) {
            throw new XMLStreamException("Even type is " + currentEvent + ", not " + type);
        }
        if (namespaceURI != null) {
            // TODO Auto-generated method stub
        }
        if (localName != null) {
            // TODO Auto-generated method stub
        }
    }

    @Override
    public String getElementText() throws XMLStreamException {
        if (!(current instanceof ElementState elem)) {
            throw new XMLStreamException("parser must be on START_ELEMENT to read next text");
        }

        if (elem.element instanceof TextElement text) {
            return text.text();
        } else if (elem.element instanceof ContainerElement container) {
            if (container.children().isEmpty()) {
                return "";
            }
            throw new XMLStreamException("element text content may not contain START_ELEMENT");
        } else {
            throw new VerifyException("Unhandled element " + elem.element);
        }
    }

    @Override
    public int nextTag() throws XMLStreamException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean hasNext() throws XMLStreamException {
        return currentEvent != END_DOCUMENT;
    }

    @Override
    public String getNamespaceURI() {
        if (current instanceof ElementState elem) {
            switch (currentEvent) {
                case START_ELEMENT, END_ELEMENT:
                    return elem.element.namespace();
                default:
                    // fall-through
            }
        }
        return null;
    }

    @Override
    public String getNamespaceURI(final String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException();
        }
        return prefix.isEmpty() ? current.defaultNamespace() : current.lookupNamespace(prefix);
    }

    @Override
    public String getNamespaceURI(final int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isStartElement() {
        return currentEvent == START_ELEMENT;
    }

    @Override
    public boolean isEndElement() {
        return currentEvent == END_ELEMENT;
    }

    @Override
    public boolean isCharacters() {
        return currentEvent == CHARACTERS;
    }

    @Override
    public boolean isWhiteSpace() {
        return false;
    }

    @Override
    public String getAttributeValue(final String namespaceURI, final String localName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getAttributeValue(final int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getAttributeCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public QName getAttributeName(final int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getAttributeNamespace(final int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getAttributeLocalName(final int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getAttributePrefix(final int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getAttributeType(final int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isAttributeSpecified(final int index) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getNamespaceCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getNamespacePrefix(final int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return verifyNotNull(current);
    }

    @Override
    public int getEventType() {
        return currentEvent;
    }

    @Override
    public String getText() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public char[] getTextCharacters() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getTextCharacters(final int sourceStart, final char[] target, final int targetStart, final int length)
            throws XMLStreamException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getTextStart() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getTextLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getEncoding() {
        return null;
    }

    @Override
    public boolean hasText() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Location getLocation() {
        return UNKNOWN;
    }

    @Override
    public QName getName() {
        if (current instanceof ElementState elem) {
            switch (currentEvent) {
                case START_ELEMENT, END_ELEMENT:
                    return new QName(elem.element.namespace(), elem.element.localName());
                default:
                    // fall-through
            }
        }
        throw new IllegalStateException("Illegal to call getName()");
    }

    @Override
    public String getLocalName() {
        if (current instanceof ElementState elem) {
            switch (currentEvent) {
                case START_ELEMENT, END_ELEMENT:
                    return elem.element.localName();
                default:
                    // fall-through
            }
        }
        throw new IllegalStateException("Method getLocalName() cannot be called");
    }

    @Override
    public boolean hasName() {
        return switch (currentEvent) {
            case START_ELEMENT, END_ELEMENT -> true;
            default -> false;
        };
    }

    @Override
    public String getPrefix() {
        if (current instanceof ElementState elem) {
            switch (currentEvent) {
                case START_ELEMENT, END_ELEMENT:
                    return DEFAULT_NS_PREFIX;
                default:
                    // fall-through
            }
        }
        return null;
    }

    @Override
    public void close() throws XMLStreamException {
        // TODO Auto-generated method stub
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public boolean isStandalone() {
        return false;
    }

    @Override
    public boolean standaloneSet() {
        return false;
    }

    @Override
    public String getCharacterEncodingScheme() {
        return null;
    }

    @Override
    @DoNotCall
    @Deprecated(forRemoval = true)
    public String getPITarget() {
        return null;
    }

    @Override
    @DoNotCall
    @Deprecated(forRemoval = true)
    public String getPIData() {
        return null;
    }
}
