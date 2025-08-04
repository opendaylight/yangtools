/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2026 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.source;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.spi.source.YinDOMSource;
import org.opendaylight.yangtools.yang.model.spi.source.YinDOMSource.SourceRefProvider;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

record YinDOMSourceWalker(
        @NonNull SourceRefProvider refProvider,
        @NonNull StatementWriter writer,
        @NonNull StatementDefinitionResolver resolver) {
    private static final Logger LOG = LoggerFactory.getLogger(YinDOMSourceWalker.class);

    YinDOMSourceWalker {
        requireNonNull(refProvider);
        requireNonNull(writer);
        requireNonNull(resolver);
    }

    static void walkSource(final YinDOMSource source, final StatementWriter writer,
            final StatementDefinitionResolver resolver) {
        new YinDOMSourceWalker(source.refProvider(), writer, resolver)
            .walkSource(source.domSource().getNode().getChildNodes());
    }

    private void walkSource(final NodeList children) {
        for (int childId = 0, i = 0, len = children.getLength(); i < len; ++i) {
            if (children.item(i) instanceof Element child) {
                processElement(childId++, child);
            }
        }
    }

    public static void visitRoot(final @NonNull YinDOMSource source, final @NonNull StatementWriter writer,
            final @NonNull StatementDefinitionResolver resolver) {
        new YinDOMSourceWalker(source.refProvider(), writer, resolver)
            .visitRoot(source.domSource().getNode().getChildNodes());
    }

    private void visitRoot(final NodeList rootNodes) {
        for (int i = 0, len = rootNodes.getLength(); i < len; ++i) {
            if (rootNodes.item(i) instanceof Element child) {
                final var ref = refProvider.getRefOf(child);
                final var def = getValidDefinition(child, ref);
                final var argDef = def.getArgumentDefinition();

                if (argDef != null) {
                    final QName argName = argDef.argumentName();
                    final String argument = getArgValue(child, argName, false);

                    writer.startStatement(0, def.getStatementName(), argument, ref);
                    writer.storeStatement(getSubstatementsCount(child), false);
                    return;
                }
            }
        }
    }

    public static void skipRootAndWalkSource(final @NonNull YinDOMSource source, final @NonNull StatementWriter writer,
        final @NonNull StatementDefinitionResolver resolver) {
        new YinDOMSourceWalker(source.refProvider(), writer, resolver)
            .skipRootAndWalkSource(source.domSource().getNode().getChildNodes());
    }

    private void skipRootAndWalkSource(final NodeList rootNodes) {
        int childCounter = 0;
        for (int i = 0, len = rootNodes.getLength(); i < len; ++i) {
            final Node rootNode = rootNodes.item(i);
            if (rootNode.getNodeType() == Node.ELEMENT_NODE) {
                final NodeList rootChildren = rootNode.getChildNodes();
                for (int childOffset = 0; childOffset < rootChildren.getLength(); childOffset++) {
                    final Node child = rootChildren.item(childOffset);
                    if (child.getNodeType() == Node.ELEMENT_NODE) {
                        processElement(childCounter++, (Element) child);
                    }
                }
            }
        }
    }

    private int getSubstatementsCount(final Node parent) {
        int count = 0;
        for (int i = 0, len = parent.getChildNodes().getLength(); i < len; ++i) {
            Node child = parent.getChildNodes().item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                count++;
            }
        }
        return count;
    }

    private boolean processElement(final int childId, final Element element) {
        final var resumed = writer.resumeStatement(childId);
        final StatementSourceReference ref;
        final QName argName;
        final boolean allAttrs;
        final boolean allElements;
        if (resumed != null) {
            if (resumed.isFullyDefined()) {
                return true;
            }

            final var def = resumed.getDefinition();
            ref = resumed.getSourceReference();
            final var argDef = def.argumentDefinition();
            if (argDef != null) {
                argName = argDef.argumentName();
                allAttrs = argDef.yinElement();
                allElements = !allAttrs;
            } else {
                argName = null;
                allAttrs = false;
                allElements = false;
            }
        } else {
            ref = refProvider.getRefOf(element);
            final var def = getValidDefinition(element, ref);
            if (def == null) {
                LOG.debug("Skipping element {}", element);
                return false;
            }

            final String argValue;
            final var argDef = def.argumentDefinition();
            if (argDef != null) {
                argName = argDef.argumentName();
                allAttrs = argDef.yinElement();
                allElements = !allAttrs;

                argValue = getArgValue(element, argName, allAttrs);
                if (argValue == null) {
                    throw new SourceException(ref, "Statement %s is missing mandatory argument %s", def.statementName(),
                        argName);
                }
            } else {
                argName = null;
                argValue = null;
                allAttrs = false;
                allElements = false;
            }

            writer.startStatement(childId, def.statementName(), argValue, ref);
        }

        // Child counter
        int childCounter = 0;
        boolean fullyDefined = true;

        // First process any statements defined as attributes. We need to skip argument, if present
        final var attributes = element.getAttributes();
        if (attributes != null) {
            for (int i = 0, len = attributes.getLength(); i < len; ++i) {
                final var attr = (Attr) attributes.item(i);
                if ((allAttrs || !isArgument(argName, attr)) && !processAttribute(childCounter++, attr, ref)) {
                    fullyDefined = false;
                }
            }
        }

        // Now process child elements, if present
        final var children = element.getChildNodes();
        for (int i = 0, len = children.getLength(); i < len; ++i) {
            if (children.item(i) instanceof Element child && (allElements || !isArgument(argName, child))
                && !processElement(childCounter++, child)) {
                fullyDefined = false;
            }
        }

        writer.storeStatement(childCounter, fullyDefined);
        writer.endStatement();
        return fullyDefined;
    }

    private boolean processAttribute(final int childId, final Attr attr, final StatementSourceReference ref) {
        final var resumed = writer.resumeStatement(childId);
        if (resumed != null) {
            checkState(resumed.isFullyDefined(), "Statement %s is not fully defined", resumed);
            return true;
        }

        final var def = getValidDefinition(attr, ref);
        if (def == null) {
            return false;
        }

        final var value = attr.getValue();
        writer.startStatement(childId, def.statementName(), value.isEmpty() ? null : value, ref);
        writer.storeStatement(0, true);
        writer.endStatement();
        return true;
    }

    private StatementDefinition<?, ?, ?> getValidDefinition(final Node node, final StatementSourceReference ref) {
        final var def = resolver.lookupDef(node.getNamespaceURI(), node.getLocalName());
        if (def == null && writer.getPhase().equals(ModelProcessingPhase.FULL_DECLARATION)) {
            throw new SourceException(ref, "%s is not a YIN statement or use of extension.", node.getLocalName());
        }
        return def;
    }

    private static boolean isArgument(final QName argName, final Node node) {
        return argName != null && argName.getLocalName().equals(node.getLocalName()) && node.getPrefix() == null;
    }

    private static String getArgValue(final Element element, final QName argName, final boolean yinElement) {
        if (yinElement) {
            final var children = element.getElementsByTagNameNS(argName.getNamespace().toString(),
                argName.getLocalName());
            if (children.getLength() == 0) {
                return null;
            }
            return children.item(0).getTextContent();
        }

        final Attr attr = element.getAttributeNode(argName.getLocalName());
        if (attr == null) {
            return null;
        }

        return attr.getValue();
    }
}
