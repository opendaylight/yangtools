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

import com.google.common.base.VerifyException;
import com.google.errorprone.annotations.DoNotCall;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import org.eclipse.jdt.annotation.NonNull;

/**
 *
 */
final class StackStreamReader implements XMLStreamReader {
    private abstract static class State implements NamespaceContext {
        private Map<String, String> prefixToNamespace;
        private String defaultNamespace;

        @Override
        public final String getNamespaceURI(final String prefix) {
            if (prefix == null) {
                throw new IllegalArgumentException("Missing prefix");
            }
            return switch (prefix) {
                case XMLConstants.DEFAULT_NS_PREFIX -> defaultNamespace();
                case XMLConstants.XML_NS_PREFIX -> XMLConstants.XML_NS_URI;
                case XMLConstants.XMLNS_ATTRIBUTE -> XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
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
            return it.hasNext() ? it.next().defaultNamespace(it) : XMLConstants.NULL_NS_URI;
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
            return it.hasNext() ? it.next().lookupNamespace(prefix, it) : XMLConstants.NULL_NS_URI;
        }

        abstract int currentEvent();
    }

    private static final class DocumentState extends State {
        // Internal-facing state
        final @NonNull Element documentElement;

        boolean started;

        DocumentState(final Element documentElement) {
            this.documentElement = requireNonNull(documentElement);
        }

        @Override
        String computeDefaultNamespace() {
            return XMLConstants.NULL_NS_URI;
        }

        @Override
        String computeNamespace(final String prefix) {
            return XMLConstants.NULL_NS_URI;
        }

        @Override
        int currentEvent() {
            return started ? XMLStreamConstants.START_DOCUMENT : XMLStreamConstants.END_DOCUMENT;
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
            var ret = element.attributeValue(null, XMLConstants.XMLNS_ATTRIBUTE);
            if (ret == null) {
                ret = computeDefaultNamespace(stash.iterator());
            }
            return ret;
        }

        @Override
        String computeNamespace(final String prefix) {
            var ret = element.attributeValue(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, prefix);
            if (ret == null) {
                final var it = stash.iterator();
                it.next();
                ret = it.hasNext() ? it.next().lookupNamespace(prefix, it) : XMLConstants.NULL_NS_URI;
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

    StackStreamReader(final Element element) {
        current = new DocumentState(element);
    }

    @Override
    public Object getProperty(final String name) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int next() throws XMLStreamException {
        var state = current;
        if (state == null) {
            throw new NoSuchElementException();
        }

        while (true) {
            if (state instanceof DocumentState document) {
                if (!document.started) {
                    stash.push(state);
                    current = new ElementState(document.documentElement);
                    return XMLStreamConstants.START_ELEMENT;
                } else {
                    return XMLStreamConstants.END_DOCUMENT;
                }
            } else if (!(state instanceof ElementState elem)) {
                // We currently have only DocumentState and ElementState
                throw new VerifyException("Unexpected state " + state);
            }

            var attrs = elem.attrs;
            if (attrs == null) {
                elem.attrs = attrs = elem.element.attributes().iterator();
            }
            if (attrs.hasNext()) {
                elem.attr = attrs.next();
                return XMLStreamConstants.ATTRIBUTE;
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
                return XMLStreamConstants.START_ELEMENT;
            }
            if (!elem.end) {
                elem.end = true;
                return XMLStreamConstants.END_ELEMENT;
            }

            // tail recursion
            current = state = stash.pop();
        }
    }

    @Override
    public void require(final int type, final String namespaceURI, final String localName) throws XMLStreamException {
        final var event = current.currentEvent();
        if (event != type) {
            throw new XMLStreamException("Even type is " + event + ", not " + type);
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
        final var state = current;
        if (!(state instanceof ElementState element)) {
            throw new XMLStreamException("parser must be on START_ELEMENT to read next text");
        }

        if (state instanceof TextElement text) {
            return text.text();
        } else if (state instanceof ContainerElement container) {
            if (container.children().isEmpty()) {
                return "";
            }
            throw new XMLStreamException("element text content may not contain START_ELEMENT");
        } else {
            throw new VerifyException("Unhandled state " + state);
        }
    }

    @Override
    public int nextTag() throws XMLStreamException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean hasNext() throws XMLStreamException {
        return current.currentEvent() != XMLStreamConstants.END_DOCUMENT;
    }

    @Override
    public void close() throws XMLStreamException {
        // TODO Auto-generated method stub
    }

    @Override
    public String getNamespaceURI(final String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException();
        }
        return prefix.isEmpty() ? current.defaultNamespace() : current.lookupNamespace(prefix);
    }

    @Override
    public boolean isStartElement() {
        return current.currentEvent() == XMLEvent.START_ELEMENT;
    }

    @Override
    public boolean isEndElement() {
        return current.currentEvent() == XMLEvent.END_ELEMENT;
    }

    @Override
    public boolean isCharacters() {
        return current.currentEvent() == XMLEvent.CHARACTERS;
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
    public String getAttributeValue(final int index) {
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
    public String getNamespaceURI(final int index) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return verifyNotNull(current);
    }

    @Override
    public int getEventType() {
        return current.currentEvent();
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
            final var event = elem.currentEvent();
            if (event == XMLEvent.START_ELEMENT || event == XMLEvent.END_ELEMENT) {
                return new QName(elem.element.namespace(), elem.element.localName());
            }
        }
        throw new IllegalStateException("Illegal to call getName()");
    }

    @Override
    public String getLocalName() {
        if (current instanceof ElementState elem) {
            final var event = elem.currentEvent();
            if (event == XMLEvent.START_ELEMENT || event == XMLEvent.END_ELEMENT) {
                return elem.element.localName();
            }
        }
        throw new IllegalStateException("Method getLocalName() cannot be called");
    }

    @Override
    public boolean hasName() {
        return switch (current.currentEvent()) {
            case XMLEvent.START_ELEMENT, XMLEvent.END_ELEMENT -> true;
            default -> false;
        };
    }

    @Override
    public String getNamespaceURI() {
        if (current instanceof ElementState elem) {
            return switch (current.currentEvent()) {
                case XMLEvent.START_ELEMENT, XMLEvent.END_ELEMENT -> elem.element.namespace();
                default -> null;
            };
        }
        return null;
    }

    @Override
    public String getPrefix() {
        if (current instanceof ElementState elem) {
            return switch (current.currentEvent()) {
                case XMLEvent.START_ELEMENT, XMLEvent.END_ELEMENT -> XMLConstants.DEFAULT_NS_PREFIX;
                default -> null;
            };
        }
        return null;
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
