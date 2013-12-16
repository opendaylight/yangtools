/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.util;

import java.lang.reflect.Method;
import java.util.List;

import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.SchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.AnyXmlBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ContainerSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.LeafListSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.LeafSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ListSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.UnknownSchemaNodeBuilder;

/**
 * Utility class with helper methods to perform operations tied to refine
 * process.
 */
public final class RefineUtils {

    private RefineUtils() {
    }

    public static void refineLeaf(LeafSchemaNodeBuilder leaf, RefineHolder refine) {
        String defaultStr = refine.getDefaultStr();
        Boolean mandatory = refine.isMandatory();
        MustDefinition must = refine.getMust();
        List<UnknownSchemaNodeBuilder> unknownNodes = refine.getUnknownNodeBuilders();

        if (defaultStr != null && !("".equals(defaultStr))) {
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

    public static void refineContainer(ContainerSchemaNodeBuilder container, RefineHolder refine) {
        Boolean presence = refine.isPresence();
        MustDefinition must = refine.getMust();
        List<UnknownSchemaNodeBuilder> unknownNodes = refine.getUnknownNodeBuilders();

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

    public static void refineList(ListSchemaNodeBuilder list, RefineHolder refine) {
        MustDefinition must = refine.getMust();
        Integer min = refine.getMinElements();
        Integer max = refine.getMaxElements();
        List<UnknownSchemaNodeBuilder> unknownNodes = refine.getUnknownNodeBuilders();

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

    public static void refineLeafList(LeafListSchemaNodeBuilder leafList, RefineHolder refine) {
        MustDefinition must = refine.getMust();
        Integer min = refine.getMinElements();
        Integer max = refine.getMaxElements();
        List<UnknownSchemaNodeBuilder> unknownNodes = refine.getUnknownNodeBuilders();

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

    public static void refineChoice(ChoiceBuilder choice, RefineHolder refine) {
        String defaultStr = refine.getDefaultStr();
        Boolean mandatory = refine.isMandatory();
        List<UnknownSchemaNodeBuilder> unknownNodes = refine.getUnknownNodeBuilders();

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

    public static void refineAnyxml(AnyXmlBuilder anyXml, RefineHolder refine) {
        Boolean mandatory = refine.isMandatory();
        MustDefinition must = refine.getMust();
        List<UnknownSchemaNodeBuilder> unknownNodes = refine.getUnknownNodeBuilders();

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
    private static void checkRefine(SchemaNodeBuilder node, RefineHolder refine) {
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
            checkRefineMust(node, must, moduleName, line);
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

    private static void checkRefineDefault(SchemaNodeBuilder node, String defaultStr, String moduleName, int line) {
        if (defaultStr != null) {
            throw new YangParseException(moduleName, line, "Can not refine 'default' for '"
                    + node.getQName().getLocalName() + "'.");
        }
    }

    private static void checkRefineMandatory(SchemaNodeBuilder node, Boolean mandatory, String moduleName, int line) {
        if (mandatory != null) {
            throw new YangParseException(moduleName, line, "Can not refine 'mandatory' for '"
                    + node.getQName().getLocalName() + "'.");
        }
    }

    private static void checkRefinePresence(SchemaNodeBuilder node, Boolean presence, String moduleName, int line) {
        if (presence != null) {
            throw new YangParseException(moduleName, line, "Can not refine 'presence' for '"
                    + node.getQName().getLocalName() + "'.");
        }
    }

    private static void checkRefineMust(SchemaNodeBuilder node, MustDefinition must, String moduleName, int line) {
        if (must != null) {
            throw new YangParseException(moduleName, line, "Can not refine 'must' for '"
                    + node.getQName().getLocalName() + "'.");
        }
    }

    private static void checkRefineMinMax(String refineTargetName, Integer min, Integer max, String moduleName, int line) {
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
    private static void refineDefault(final Builder node, final RefineHolder refine) {
        final String moduleName = refine.getModuleName();
        final int line = refine.getLine();
        Class<? extends Builder> cls = node.getClass();

        String description = refine.getDescription();
        if (description != null) {
            try {
                Method method = cls.getDeclaredMethod("setDescription", String.class);
                method.invoke(node, description);
            } catch (Exception e) {
                throw new YangParseException(moduleName, line, "Cannot refine description in " + cls.getName(), e);
            }
        }

        String reference = refine.getReference();
        if (reference != null) {
            try {
                Method method = cls.getDeclaredMethod("setReference", String.class);
                method.invoke(node, reference);
            } catch (Exception e) {
                throw new YangParseException(moduleName, line, "Cannot refine reference in " + cls.getName(), e);
            }
        }

        Boolean config = refine.isConfiguration();
        if (config != null) {
            try {
                Method method = cls.getDeclaredMethod("setConfiguration", Boolean.class);
                method.invoke(node, config);
            } catch (Exception e) {
                throw new YangParseException(moduleName, line, "Cannot refine config in " + cls.getName(), e);
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
    static void performRefine(SchemaNodeBuilder nodeToRefine, RefineHolder refine) {
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
