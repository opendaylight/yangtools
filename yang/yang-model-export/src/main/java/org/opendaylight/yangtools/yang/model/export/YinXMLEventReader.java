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
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Queue;
import javax.xml.XMLConstants;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;

final class YinXMLEventReader implements XMLEventReader {
    private static final class OpenElement {
        final Iterator<? extends DeclaredStatement<?>> children;
        final QName name;

        OpenElement(final QName name, final Iterator<? extends DeclaredStatement<?>> children) {
            this.name = requireNonNull(name);
            this.children = requireNonNull(children);
        }
    }

    private static final String DEFAULT_NS_STRING = YangConstants.RFC6020_YIN_NAMESPACE.toString();

    private final Deque<OpenElement> stack = new ArrayDeque<>(8);
    private final Queue<XMLEvent> events = new ArrayDeque<>();
    private final Map<QNameModule, @NonNull String> moduleToPrefix;
    private final XMLEventFactory eventFactory;

    YinXMLEventReader(final XMLEventFactory eventFactory, final ModuleStatement module,
            final Map<String, @NonNull ModuleEffectiveStatement> prefixToModule,
            final Map<QNameModule, @NonNull String> moduleToPrefix) {
        this.eventFactory = requireNonNull(eventFactory);
        this.moduleToPrefix = requireNonNull(moduleToPrefix);

        events.add(eventFactory.createStartDocument(StandardCharsets.UTF_8.name()));

        final StatementDefinition def = module.statementDefinition();
        final QName name = def.getStatementName();

        events.add(eventFactory.createStartElement(XMLConstants.DEFAULT_NS_PREFIX, name.getNamespace().toString(),
            name.getLocalName(), singletonIterator(attribute(def.getArgumentName(), module.argument())),
            transform(prefixToModule.entrySet().iterator(),
                e -> namespace(e.getKey(), e.getValue().localQNameModule()))));

        stack.push(new OpenElement(name, module.declaredSubstatements().iterator()));
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public XMLEvent nextTag() throws XMLStreamException {
        // TODO Auto-generated method stub
        return null;
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

    private Attribute attribute(final QName name, final String value) {
        return eventFactory.createAttribute(name.getLocalName(), value);
    }

    private Namespace namespace(final String prefix, final QNameModule module) {
        return eventFactory.createNamespace(prefix, module.getNamespace().toString());
    }

    private StartElement startElement(final QName qname) {
        final Optional<QNameModule> module = maskDefaultNamespace(qname.getModule());
        final String prefix = module.map(moduleToPrefix::get).orElse(XMLConstants.DEFAULT_NS_PREFIX);
        final String namespace = module.map(mod -> mod.getNamespace().toString()).orElse(DEFAULT_NS_STRING);
        return eventFactory.createStartElement(prefix, namespace, qname.getLocalName());
    }

    private EndElement endElement(final QName qname) {
        final Optional<QNameModule> module = maskDefaultNamespace(qname.getModule());
        final String prefix = module.map(moduleToPrefix::get).orElse(XMLConstants.DEFAULT_NS_PREFIX);
        final String namespace = module.map(mod -> mod.getNamespace().toString()).orElse(DEFAULT_NS_STRING);
        return eventFactory.createEndElement(prefix, namespace, qname.getLocalName());
    }

    private void nextStatement() {
        final OpenElement current = stack.peek();
        if (current == null) {
            return;
        }

        if (current.children.hasNext()) {
            addStatement(current.children.next());
        } else {
            events.add(endElement(current.name));
            stack.pop();
            if (stack.isEmpty()) {
                events.add(eventFactory.createEndDocument());
            }
        }
    }

    private void addStatement(final DeclaredStatement<?> statement) {
        final StatementDefinition def = statement.statementDefinition();
        final QName name = def.getStatementName();
        final QName argName = def.getArgumentName();
        if (argName != null) {
            if (def.isArgumentYinElement()) {
                events.addAll(Arrays.asList(startElement(name), startElement(argName),
                    eventFactory.createCharacters(statement.rawArgument()), endElement(argName)));
            } else {
                // FIXME: namespace is probably not right
                events.add(eventFactory.createStartElement(null, null, name.getLocalName(),
                    singletonIterator(attribute(argName, statement.rawArgument())), emptyIterator()));
            }
        } else {
            // No attributes: just emit a start
            events.add(startElement(name));
        }

        stack.push(new OpenElement(name, statement.declaredSubstatements().iterator()));
    }

    private static Optional<QNameModule> maskDefaultNamespace(final QNameModule module) {
        return YangConstants.RFC6020_YIN_MODULE.equals(module) ? Optional.empty() : Optional.of(module);
    }

}
