/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.impl;

import java.util.List;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DocumentedNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.RefineBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.SchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;

/**
 * Utility class with helper methods to perform operations tied to refine
 * process.
 *
 * @deprecated Pre-Beryllium implementation, scheduled for removal.
 */
@Deprecated
public final class RefineUtils {

    private RefineUtils() {
    }

    private static void refineLeaf(final LeafSchemaNodeBuilder leaf, final RefineBuilder refine) {
        String defaultStr = refine.getDefaultStr();
        Boolean mandatory = refine.isMandatory();
        MustDefinition must = refine.getMust();
        List<UnknownSchemaNodeBuilder> unknownNodes = refine.getUnknownNodes();

        if (defaultStr != null && !defaultStr.isEmpty()) {
            leaf.setDefaultStr(defaultStr);
        }
        if (mandatory != null) {
            leaf.getConstraints().setMandatory(mandatory);
        }
        if (must != null) {
            leaf.getConstraints().addMustDefinition(must);
        }
        if (unknownNodes != null) {
            for (UnknownSchemaNodeBuilder unknown : unknownNodes) {
                unknown.setParent(leaf);
                leaf.addUnknownNodeBuilder(unknown);
            }
        }
    }

    private static void refineContainer(final ContainerSchemaNodeBuilder container, final RefineBuilder refine) {
        Boolean presence = refine.isPresence();
        MustDefinition must = refine.getMust();
        List<UnknownSchemaNodeBuilder> unknownNodes = refine.getUnknownNodes();

        if (presence != null) {
            container.setPresence(presence);
        }
        if (must != null) {
            container.getConstraints().addMustDefinition(must);
        }
        if (unknownNodes != null) {
            for (UnknownSchemaNodeBuilder unknown : unknownNodes) {
                unknown.setParent(container);
                container.addUnknownNodeBuilder(unknown);
            }
        }
    }

    private static void refineList(final ListSchemaNodeBuilder list, final RefineBuilder refine) {
        MustDefinition must = refine.getMust();
        Integer min = refine.getMinElements();
        Integer max = refine.getMaxElements();
        List<UnknownSchemaNodeBuilder> unknownNodes = refine.getUnknownNodes();

        if (must != null) {
            list.getConstraints().addMustDefinition(must);
        }
        if (min != null) {
            list.getConstraints().setMinElements(min);
        }
        if (max != null) {
            list.getConstraints().setMaxElements(max);
        }
        if (unknownNodes != null) {
            for (UnknownSchemaNodeBuilder unknown : unknownNodes) {
                unknown.setParent(list);
                list.addUnknownNodeBuilder(unknown);
            }
        }
    }

    public static void refineLeafList(final LeafListSchemaNodeBuilder leafList, final RefineBuilder refine) {
        MustDefinition must = refine.getMust();
        Integer min = refine.getMinElements();
        Integer max = refine.getMaxElements();
        List<UnknownSchemaNodeBuilder> unknownNodes = refine.getUnknownNodes();

        if (must != null) {
            leafList.getConstraints().addMustDefinition(must);
        }
        if (min != null) {
            leafList.getConstraints().setMinElements(min);
        }
        if (max != null) {
            leafList.getConstraints().setMaxElements(max);
        }
        if (unknownNodes != null) {
            for (UnknownSchemaNodeBuilder unknown : unknownNodes) {
                unknown.setParent(leafList);
                leafList.addUnknownNodeBuilder(unknown);
            }
        }
    }

    public static void refineChoice(final ChoiceBuilder choice, final RefineBuilder refine) {
        String defaultStr = refine.getDefaultStr();
        Boolean mandatory = refine.isMandatory();
        List<UnknownSchemaNodeBuilder> unknownNodes = refine.getUnknownNodes();

        if (defaultStr != null) {
            choice.setDefaultCase(defaultStr);
        }
        if (mandatory != null) {
            choice.getConstraints().setMandatory(mandatory);
        }
        if (unknownNodes != null) {
            for (UnknownSchemaNodeBuilder unknown : unknownNodes) {
                unknown.setParent(choice);
                choice.addUnknownNodeBuilder(unknown);
            }
        }
    }

    public static void refineAnyxml(final AnyXmlBuilder anyXml, final RefineBuilder refine) {
        Boolean mandatory = refine.isMandatory();
        MustDefinition must = refine.getMust();
        List<UnknownSchemaNodeBuilder> unknownNodes = refine.getUnknownNodes();

        if (mandatory != null) {
            anyXml.getConstraints().setMandatory(mandatory);
        }
        if (must != null) {
            anyXml.getConstraints().addMustDefinition(must);
        }
        if (unknownNodes != null) {
            for (UnknownSchemaNodeBuilder unknown : unknownNodes) {
                unknown.setParent(anyXml);
                anyXml.addUnknownNodeBuilder(unknown);
            }
        }
    }

    /**
     * Check if refine can be performed on given node.
     *
     * @param node
     *            node to refine
     * @param refine
     *            refine object containing information about refine process
     */
    private static void checkRefine(final SchemaNodeBuilder node, final RefineBuilder refine) {
        String moduleName = refine.getModuleName();
        int line = refine.getLine();
        String name = node.getQName().getLocalName();

        String defaultStr = refine.getDefaultStr();
        Boolean mandatory = refine.isMandatory();
        Boolean presence = refine.isPresence();
        MustDefinition must = refine.getMust();
        Integer min = refine.getMinElements();
        Integer max = refine.getMaxElements();

        if (node instanceof AnyXmlBuilder) {
            checkRefineDefault(node, defaultStr, moduleName, line);
            checkRefinePresence(node, presence, moduleName, line);
            checkRefineMinMax(name, min, max, moduleName, line);
        } else if (node instanceof ChoiceBuilder) {
            checkRefinePresence(node, presence, moduleName, line);
            checkRefineMust(node, must, moduleName, line);
            checkRefineMinMax(name, min, max, moduleName, line);
        } else if (node instanceof ContainerSchemaNodeBuilder) {
            checkRefineDefault(node, defaultStr, moduleName, line);
            checkRefineMandatory(node, mandatory, moduleName, line);
            checkRefineMinMax(name, min, max, moduleName, line);
        } else if (node instanceof LeafSchemaNodeBuilder) {
            checkRefinePresence(node, presence, moduleName, line);
            checkRefineMinMax(name, min, max, moduleName, line);
        } else if (node instanceof LeafListSchemaNodeBuilder || node instanceof ListSchemaNodeBuilder) {
            checkRefineDefault(node, defaultStr, moduleName, line);
            checkRefinePresence(node, presence, moduleName, line);
            checkRefineMandatory(node, mandatory, moduleName, line);
        } else if (node instanceof GroupingBuilder || node instanceof TypeDefinitionBuilder
                || node instanceof UsesNodeBuilder) {
            checkRefineDefault(node, defaultStr, moduleName, line);
            checkRefinePresence(node, presence, moduleName, line);
            checkRefineMandatory(node, mandatory, moduleName, line);
            checkRefineMust(node, must, moduleName, line);
            checkRefineMinMax(name, min, max, moduleName, line);
        }
    }

    private static void checkRefineDefault(final SchemaNodeBuilder node, final String defaultStr, final String moduleName, final int line) {
        if (defaultStr != null) {
            throw new YangParseException(moduleName, line, "Can not refine 'default' for '"
                    + node.getQName().getLocalName() + "'.");
        }
    }

    private static void checkRefineMandatory(final SchemaNodeBuilder node, final Boolean mandatory, final String moduleName, final int line) {
        if (mandatory != null) {
            throw new YangParseException(moduleName, line, "Can not refine 'mandatory' for '"
                    + node.getQName().getLocalName() + "'.");
        }
    }

    private static void checkRefinePresence(final SchemaNodeBuilder node, final Boolean presence, final String moduleName, final int line) {
        if (presence != null) {
            throw new YangParseException(moduleName, line, "Can not refine 'presence' for '"
                    + node.getQName().getLocalName() + "'.");
        }
    }

    private static void checkRefineMust(final SchemaNodeBuilder node, final MustDefinition must, final String moduleName, final int line) {
        if (must != null) {
            throw new YangParseException(moduleName, line, "Can not refine 'must' for '"
                    + node.getQName().getLocalName() + "'.");
        }
    }

    private static void checkRefineMinMax(final String refineTargetName, final Integer min, final Integer max, final String moduleName, final int line) {
        if (min != null || max != null) {
            throw new YangParseException(moduleName, line, "Can not refine 'min-elements' or 'max-elements' for '"
                    + refineTargetName + "'.");
        }
    }

    /**
     * Perform refine operation of following parameters:
     * <ul>
     * <li>description</li>
     * <li>reference</li>
     * <li>config</li>
     * </ul>
     *
     * These parameters may be refined for any node.
     *
     * @param node
     *            node to refine
     * @param refine
     *            refine object containing information about refine process
     */
    private static void refineDefault(final Builder node, final RefineBuilder refine) {
        final String moduleName = refine.getModuleName();
        final int line = refine.getLine();


        final DocumentedNodeBuilder documentedNode;
        if (node instanceof DocumentedNodeBuilder) {
            documentedNode = ((DocumentedNodeBuilder) node);
        } else {
            documentedNode = null;
        }

        String description = refine.getDescription();


        if (description != null) {
            if (documentedNode != null) {
                documentedNode.setDescription(description);
            } else {
                throw new YangParseException(moduleName, line, String.format("Cannot refine description in of target %s",refine.getTargetPathString()));
            }

        }

        String reference = refine.getReference();
        if (reference != null) {
            if (documentedNode != null) {
                documentedNode.setReference(reference);
            } else {
                throw new YangParseException(moduleName, line, String.format("Cannot refine reference in of target %s",refine.getTargetPathString()));
            }
        }

        Boolean config = refine.isConfiguration();
        if (config != null) {
            if (node instanceof DataSchemaNodeBuilder) {
                ((DataSchemaNodeBuilder) node).setConfiguration(config);
            } else {
                throw new YangParseException(moduleName, line, String.format("Cannot refine config of target %s ",refine.getTargetPathString()));
            }
        }
    }

    /**
     * Perform refine operation on given node.
     *
     * @param nodeToRefine
     *            builder of node to refine
     * @param refine
     *            refine object containing information about refine process
     */
    static void performRefine(final SchemaNodeBuilder nodeToRefine, final RefineBuilder refine) {
        checkRefine(nodeToRefine, refine);
        refineDefault(nodeToRefine, refine);
        if (nodeToRefine instanceof LeafSchemaNodeBuilder) {
            refineLeaf((LeafSchemaNodeBuilder) nodeToRefine, refine);
        } else if (nodeToRefine instanceof ContainerSchemaNodeBuilder) {
            refineContainer((ContainerSchemaNodeBuilder) nodeToRefine, refine);
        } else if (nodeToRefine instanceof ListSchemaNodeBuilder) {
            refineList((ListSchemaNodeBuilder) nodeToRefine, refine);
        } else if (nodeToRefine instanceof LeafListSchemaNodeBuilder) {
            refineLeafList((LeafListSchemaNodeBuilder) nodeToRefine, refine);
        } else if (nodeToRefine instanceof ChoiceBuilder) {
            refineChoice((ChoiceBuilder) nodeToRefine, refine);
        } else if (nodeToRefine instanceof AnyXmlBuilder) {
            refineAnyxml((AnyXmlBuilder) nodeToRefine, refine);
        }
    }

}
