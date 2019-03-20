/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import static com.google.common.collect.Iterators.singletonIterator;
import static com.google.common.collect.Iterators.transform;
import static java.util.Collections.emptyIterator;
import static java.util.Objects.requireNonNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Queue;
import javax.xml.XMLConstants;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

final class YinXMLEventReader implements XMLEventReader {
    private static final class OpenElement {
        final Iterator<? extends DeclaredStatement<?>> children;
        final QName name;

        OpenElement(final Iterator<? extends DeclaredStatement<?>> children) {
            this.children = requireNonNull(children);
            this.name = null;
        }

        OpenElement(final QName name, final Iterator<? extends DeclaredStatement<?>> children) {
            this.children = requireNonNull(children);
            this.name = requireNonNull(name);
        }
    }

    private final Deque<OpenElement> stack = new ArrayDeque<>(8);
    private final Queue<XMLEvent> events = new ArrayDeque<>();
    private final ModuleNamespaceContext namespaceContext;
    private final XMLEventFactory eventFactory;

    YinXMLEventReader(final XMLEventFactory eventFactory, final ModuleNamespaceContext namespaceContext,
            final DeclaredStatement<?> root) {
        this.eventFactory = requireNonNull(eventFactory);
        this.namespaceContext = requireNonNull(namespaceContext);

        events.add(eventFactory.createStartDocument(StandardCharsets.UTF_8.name()));

        final StatementDefinition def = root.statementDefinition();
        final QName name = def.getStatementName();
        final ArgumentDefinition arg = def.getArgumentDefinition().get();

        events.add(eventFactory.createStartElement(XMLConstants.DEFAULT_NS_PREFIX, name.getNamespace().toString(),
            name.getLocalName(), singletonIterator(attribute(arg.getArgumentName(), root.rawArgument())),
            transform(namespaceContext.prefixesAndNamespaces().entrySet().iterator(),
                e -> eventFactory.createNamespace(e.getKey(), e.getValue())),
            namespaceContext));

        stack.push(new OpenElement(name, root.declaredSubstatements().iterator()));
    }

    @Override
    public XMLEvent next() {
        XMLEvent event = events.poll();
        if (event != null) {
            return event;
        }

        nextStatement();
        event = events.poll();
        if (event == null) {
            throw new NoSuchElementException("All events have been processed");
        }
        return event;
    }

    @Override
    public XMLEvent nextEvent() {
        return next();
    }

    @Override
    public boolean hasNext() {
        if (events.isEmpty()) {
            nextStatement();
            return events.isEmpty();
        }
        return true;
    }

    @Override
    public XMLEvent peek() throws XMLStreamException {
        if (events.isEmpty()) {
            nextStatement();
        }

        return events.peek();
    }

    @Override
    public String getElementText() throws XMLStreamException {
        XMLEvent current = peek();
        if (current == null) {
            throw new XMLStreamException("End of event stream");
        }
        if (!(current instanceof StartElement)) {
            throw new XMLStreamException("Current event is " + current);
        }

        current = next();
        if (!(current instanceof Characters)) {
            throw new XMLStreamException("Encountered non-text event " + current);
        }
        final String ret = ((Characters)current).getData();

        current = next();
        if (!(current instanceof EndElement)) {
            throw new XMLStreamException("Encountered unexpected event " + current);
        }
        return ret;
    }

    @Override
    public XMLEvent nextTag() throws XMLStreamException {
        final XMLEvent next = next();
        if (next instanceof Characters) {
            throw new XMLStreamException("Significant characters encountered: " + next);
        }
        return next;
    }

    @Override
    public Object getProperty(final String name) {
        throw new IllegalArgumentException("Property " + name + " not supported");
    }

    @Override
    public void close() {
        events.clear();
        stack.clear();
    }

    private Attribute attribute(final QName qname, final String value) {
        final Entry<String, String> ns = namespaceContext.prefixAndNamespaceFor(qname.getModule());
        return eventFactory.createAttribute(ns.getKey(), ns.getValue(), qname.getLocalName(), value);
    }

    private StartElement startElement(final QName qname) {
        final Entry<String, String> ns = namespaceContext.prefixAndNamespaceFor(qname.getModule());
        return eventFactory.createStartElement(ns.getKey(), ns.getValue(), qname.getLocalName(), emptyIterator(),
            emptyIterator(), namespaceContext);
    }

    private EndElement endElement(final QName qname) {
        final Entry<String, String> ns = namespaceContext.prefixAndNamespaceFor(qname.getModule());
        return eventFactory.createEndElement(ns.getKey(), ns.getValue(), qname.getLocalName());
    }

    private void nextStatement() {
        OpenElement current = stack.peek();
        if (current == null) {
            return;
        }

        do {
            while (current.children.hasNext()) {
                // We have to mind child statement source and not emit empty implicit children
                final DeclaredStatement<?> child = current.children.next();
                switch (child.getStatementSource()) {
                    case CONTEXT:
                        final Iterator<? extends DeclaredStatement<?>> it = child.declaredSubstatements().iterator();
                        if (it.hasNext()) {
                            current = new OpenElement(it);
                        }
                        break;
                    case DECLARATION:
                        addStatement(child);
                        return;
                    default:
                        throw new IllegalStateException("Unhandled statement source " + child.getStatementSource());
                }
            }

            if (current.name != null) {
                events.add(endElement(current.name));
            }
            stack.pop();
            if (stack.isEmpty()) {
                events.add(eventFactory.createEndDocument());
            }
        } while (events.isEmpty());
    }

    private void addStatement(final DeclaredStatement<?> statement) {
        final StatementDefinition def = statement.statementDefinition();
        final QName name = def.getStatementName();
        final Optional<ArgumentDefinition> optArgDef = def.getArgumentDefinition();
        if (optArgDef.isPresent()) {
            final ArgumentDefinition argDef = optArgDef.get();
            final QName argName = argDef.getArgumentName();
            if (argDef.isYinElement()) {
                events.addAll(Arrays.asList(startElement(name), startElement(argName),
                    eventFactory.createCharacters(statement.rawArgument()), endElement(argName)));
            } else {
                final Entry<String, String> ns = namespaceContext.prefixAndNamespaceFor(name.getModule());
                events.add(eventFactory.createStartElement(ns.getKey(), ns.getValue(), name.getLocalName(),
                    singletonIterator(attribute(argName, statement.rawArgument())), emptyIterator(), namespaceContext));
            }
        } else {
            // No attributes: just emit a start
            events.add(startElement(name));
        }

        stack.push(new OpenElement(name, statement.declaredSubstatements().iterator()));
    }
}
