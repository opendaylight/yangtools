/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.BinaryType;
import org.opendaylight.yangtools.yang.model.util.BitsType;
import org.opendaylight.yangtools.yang.model.util.BooleanType;
import org.opendaylight.yangtools.yang.model.util.Decimal64;
import org.opendaylight.yangtools.yang.model.util.EmptyType;
import org.opendaylight.yangtools.yang.model.util.EnumerationType;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;
import org.opendaylight.yangtools.yang.model.util.IdentityrefType;
import org.opendaylight.yangtools.yang.model.util.InstanceIdentifier;
import org.opendaylight.yangtools.yang.model.util.Leafref;
import org.opendaylight.yangtools.yang.model.util.UnionType;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationTargetBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataNodeContainerBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingMember;
import org.opendaylight.yangtools.yang.parser.builder.api.SchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeAwareBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.AnyXmlBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceBuilder.ChoiceNodeImpl;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceCaseBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceCaseBuilder.ChoiceCaseNodeImpl;
import org.opendaylight.yangtools.yang.parser.builder.impl.ConstraintsBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ContainerSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ContainerSchemaNodeBuilder.ContainerSchemaNodeImpl;
import org.opendaylight.yangtools.yang.parser.builder.impl.GroupingBuilderImpl;
import org.opendaylight.yangtools.yang.parser.builder.impl.IdentityrefTypeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.LeafListSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.LeafSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ListSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ListSchemaNodeBuilder.ListSchemaNodeImpl;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.NotificationBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.NotificationBuilder.NotificationDefinitionImpl;
import org.opendaylight.yangtools.yang.parser.builder.impl.TypeDefinitionBuilderImpl;
import org.opendaylight.yangtools.yang.parser.builder.impl.UnknownSchemaNodeBuilder;

public final class ParserUtils {

    private ParserUtils() {
    }

    /**
     * Create new SchemaPath from given path and qname.
     *
     * @param schemaPath
     * @param qname
     * @return
     */
    public static SchemaPath createSchemaPath(SchemaPath schemaPath, QName... qname) {
        List<QName> path = new ArrayList<>(schemaPath.getPath());
        path.addAll(Arrays.asList(qname));
        return new SchemaPath(path, schemaPath.isAbsolute());
    }

    /**
     * Get module import referenced by given prefix.
     *
     * @param builder
     *            module to search
     * @param prefix
     *            prefix associated with import
     * @return ModuleImport based on given prefix
     */
    public static ModuleImport getModuleImport(final ModuleBuilder builder, final String prefix) {
        ModuleImport moduleImport = null;
        for (ModuleImport mi : builder.getModuleImports()) {
            if (mi.getPrefix().equals(prefix)) {
                moduleImport = mi;
                break;
            }
        }
        return moduleImport;
    }

    /**
     * Find dependent module based on given prefix
     *
     * @param modules
     *            all available modules
     * @param module
     *            current module
     * @param prefix
     *            target module prefix
     * @param line
     *            current line in yang model
     * @return module builder if found, null otherwise
     */
    public static ModuleBuilder findDependentModuleBuilder(final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            final ModuleBuilder module, final String prefix, final int line) {
        ModuleBuilder dependentModule = null;
        Date dependentModuleRevision = null;

        if (prefix.equals(module.getPrefix())) {
            dependentModule = module;
        } else {
            final ModuleImport dependentModuleImport = getModuleImport(module, prefix);
            if (dependentModuleImport == null) {
                throw new YangParseException(module.getName(), line, "No import found with prefix '" + prefix + "'.");
            }
            final String dependentModuleName = dependentModuleImport.getModuleName();
            dependentModuleRevision = dependentModuleImport.getRevision();

            final TreeMap<Date, ModuleBuilder> moduleBuildersByRevision = modules.get(dependentModuleName);
            if (moduleBuildersByRevision == null) {
                return null;
            }
            if (dependentModuleRevision == null) {
                dependentModule = moduleBuildersByRevision.lastEntry().getValue();
            } else {
                dependentModule = moduleBuildersByRevision.get(dependentModuleRevision);
            }
        }
        return dependentModule;
    }

    /**
     * Find module from context based on prefix.
     *
     * @param context
     *            schema context
     * @param currentModule
     *            current module
     * @param prefix
     *            current prefix used to reference dependent module
     * @param line
     *            current line in yang model
     * @return module based on given prefix if found in context, null otherwise
     */
    public static Module findModuleFromContext(final SchemaContext context, final ModuleBuilder currentModule,
            final String prefix, final int line) {
        TreeMap<Date, Module> modulesByRevision = new TreeMap<Date, Module>();

        final ModuleImport dependentModuleImport = ParserUtils.getModuleImport(currentModule, prefix);
        if (dependentModuleImport == null) {
            throw new YangParseException(currentModule.getName(), line, "No import found with prefix '" + prefix + "'.");
        }
        final String dependentModuleName = dependentModuleImport.getModuleName();
        final Date dependentModuleRevision = dependentModuleImport.getRevision();

        for (Module contextModule : context.getModules()) {
            if (contextModule.getName().equals(dependentModuleName)) {
                Date revision = contextModule.getRevision();
                if (revision == null) {
                    revision = new Date(0L);
                }
                modulesByRevision.put(revision, contextModule);
                break;
            }
        }

        Module result = null;
        if (dependentModuleRevision == null) {
            result = modulesByRevision.get(modulesByRevision.firstKey());
        } else {
            result = modulesByRevision.get(dependentModuleRevision);
        }

        return result;
    }

    /**
     * Parse XPath string.
     *
     * @param xpathString
     *            as String
     * @return SchemaPath from given String
     */
    public static SchemaPath parseXPathString(final String xpathString) {
        final boolean absolute = xpathString.startsWith("/");
        final String[] splittedPath = xpathString.split("/");
        final List<QName> path = new ArrayList<QName>();
        QName name;
        for (String pathElement : splittedPath) {
            if (pathElement.length() > 0) {
                final String[] splittedElement = pathElement.split(":");
                if (splittedElement.length == 1) {
                    name = new QName(null, null, null, splittedElement[0]);
                } else {
                    name = new QName(null, null, splittedElement[0], splittedElement[1]);
                }
                path.add(name);
            }
        }
        return new SchemaPath(path, absolute);
    }

    /**
     * Add all augment's child nodes to given target.
     *
     * @param augment
     *            builder of augment statement
     * @param target
     *            augmentation target node
     */
    public static void fillAugmentTarget(final AugmentationSchemaBuilder augment, final DataNodeContainerBuilder target) {
        for (DataSchemaNodeBuilder child : augment.getChildNodeBuilders()) {
            DataSchemaNodeBuilder childCopy = CopyUtils.copy(child, target, false);
            childCopy.setAugmenting(true);
            correctNodePath(child, target.getPath());
            correctNodePath(childCopy, target.getPath());
            try {
                target.addChildNode(childCopy);
            } catch(YangParseException e) {
                // more descriptive message
                throw new YangParseException(augment.getModuleName(), augment.getLine(), "Failed to perform augmentation: "+ e.getMessage());
            }

        }
        for (UsesNodeBuilder usesNode : augment.getUsesNodes()) {
            target.addUsesNode(CopyUtils.copyUses(usesNode, target));
        }
    }

    /**
     * Add all augment's child nodes to given target.
     *
     * @param augment
     *            builder of augment statement
     * @param target
     *            augmentation target choice node
     */
    public static void fillAugmentTarget(final AugmentationSchemaBuilder augment, final ChoiceBuilder target) {
        for (DataSchemaNodeBuilder builder : augment.getChildNodeBuilders()) {
            DataSchemaNodeBuilder childCopy = CopyUtils.copy(builder, target, false);
            childCopy.setAugmenting(true);
            correctNodePath(builder, target.getPath());
            correctNodePath(childCopy, target.getPath());
            target.addCase(childCopy);
        }
        for (UsesNodeBuilder usesNode : augment.getUsesNodes()) {
            if (usesNode != null) {
                throw new YangParseException(augment.getModuleName(), augment.getLine(),
                        "Error in augment parsing: cannot augment uses to choice");
            }
        }

    }

    static void correctNodePath(final SchemaNodeBuilder node, final SchemaPath parentSchemaPath) {
        // set correct path
        List<QName> targetNodePath = new ArrayList<QName>(parentSchemaPath.getPath());
        targetNodePath.add(node.getQName());
        node.setPath(new SchemaPath(targetNodePath, true));

        // set correct path for all child nodes
        if (node instanceof DataNodeContainerBuilder) {
            DataNodeContainerBuilder dataNodeContainer = (DataNodeContainerBuilder) node;
            for (DataSchemaNodeBuilder child : dataNodeContainer.getChildNodeBuilders()) {
                correctNodePath(child, node.getPath());
            }
        }

        // set correct path for all cases
        if (node instanceof ChoiceBuilder) {
            ChoiceBuilder choiceBuilder = (ChoiceBuilder) node;
            for (ChoiceCaseBuilder choiceCaseBuilder : choiceBuilder.getCases()) {
                correctNodePath(choiceCaseBuilder, node.getPath());
            }
        }

        // if node can contains type, correct path for this type too
        if (node instanceof TypeAwareBuilder) {
            TypeAwareBuilder nodeBuilder = (TypeAwareBuilder) node;
            correctTypeAwareNodePath(nodeBuilder, node.getPath());
        }
    }

    /**
     * Repair schema path of node type.
     *
     * @param node
     *            node which contains type statement
     * @param parentSchemaPath
     *            schema path of parent node
     */
    private static void correctTypeAwareNodePath(final TypeAwareBuilder node, final SchemaPath parentSchemaPath) {
        final QName nodeBuilderQName = node.getQName();
        final TypeDefinition<?> nodeType = node.getType();

        Integer fd = null;
        List<LengthConstraint> lengths = null;
        List<PatternConstraint> patterns = null;
        List<RangeConstraint> ranges = null;

        if (nodeType != null) {
            if (nodeType instanceof ExtendedType) {
                ExtendedType et = (ExtendedType) nodeType;
                if (nodeType.getQName().getLocalName().equals(nodeType.getBaseType().getQName().getLocalName())) {
                    fd = et.getFractionDigits();
                    lengths = et.getLengths();
                    patterns = et.getPatterns();
                    ranges = et.getRanges();
                    if (!hasConstraints(fd, lengths, patterns, ranges)) {
                        return;
                    }
                }
            }
            TypeDefinition<?> newType = createCorrectTypeDefinition(parentSchemaPath, nodeBuilderQName, nodeType);
            node.setType(newType);
        } else {
            TypeDefinitionBuilder nodeBuilderTypedef = node.getTypedef();

            fd = nodeBuilderTypedef.getFractionDigits();
            lengths = nodeBuilderTypedef.getLengths();
            patterns = nodeBuilderTypedef.getPatterns();
            ranges = nodeBuilderTypedef.getRanges();

            String tdbTypeName = nodeBuilderTypedef.getQName().getLocalName();
            String baseTypeName = null;
            if (nodeBuilderTypedef.getType() == null) {
                baseTypeName = nodeBuilderTypedef.getTypedef().getQName().getLocalName();
            } else {
                baseTypeName = nodeBuilderTypedef.getType().getQName().getLocalName();
            }
            if (!(tdbTypeName.equals(baseTypeName))) {
                return;
            }

            if (!hasConstraints(fd, lengths, patterns, ranges)) {
                return;
            }

            SchemaPath newSchemaPath = createSchemaPath(nodeBuilderTypedef.getPath(), nodeBuilderQName,
                    nodeBuilderTypedef.getQName());
            nodeBuilderTypedef.setPath(newSchemaPath);
        }
    }

    /**
     * Check if there are some constraints.
     *
     * @param fd
     *            fraction digits
     * @param lengths
     *            length constraints
     * @param patterns
     *            pattern constraints
     * @param ranges
     *            range constraints
     * @return true, if any of constraints are present, false otherwise
     */
    private static boolean hasConstraints(final Integer fd, final List<LengthConstraint> lengths,
            final List<PatternConstraint> patterns, final List<RangeConstraint> ranges) {
        if (fd == null && (lengths == null || lengths.isEmpty()) && (patterns == null || patterns.isEmpty())
                && (ranges == null || ranges.isEmpty())) {
            return false;
        } else {
            return true;
        }
    }

    private static TypeDefinition<?> createCorrectTypeDefinition(SchemaPath parentSchemaPath, QName nodeQName,
            TypeDefinition<?> nodeType) {
        TypeDefinition<?> result = null;

        if (nodeType != null) {
            QName nodeTypeQName = nodeType.getQName();
            SchemaPath newSchemaPath = createSchemaPath(parentSchemaPath, nodeQName, nodeTypeQName);

            if (nodeType instanceof BinaryTypeDefinition) {
                BinaryTypeDefinition binType = (BinaryTypeDefinition) nodeType;

                // List<Byte> bytes = (List<Byte>) binType.getDefaultValue();
                // workaround to get rid of 'Unchecked cast' warning
                List<Byte> bytes = new ArrayList<Byte>();
                Object defaultValue = binType.getDefaultValue();
                if (defaultValue instanceof List) {
                    for (Object o : List.class.cast(defaultValue)) {
                        if (o instanceof Byte) {
                            bytes.add((Byte) o);
                        }
                    }
                }
                result = new BinaryType(newSchemaPath, bytes);
            } else if (nodeType instanceof BitsTypeDefinition) {
                BitsTypeDefinition bitsType = (BitsTypeDefinition) nodeType;
                result = new BitsType(newSchemaPath, bitsType.getBits());
            } else if (nodeType instanceof BooleanTypeDefinition) {
                result = new BooleanType(newSchemaPath);
            } else if (nodeType instanceof DecimalTypeDefinition) {
                DecimalTypeDefinition decimalType = (DecimalTypeDefinition) nodeType;
                result = new Decimal64(newSchemaPath, decimalType.getFractionDigits());
            } else if (nodeType instanceof EmptyTypeDefinition) {
                result = new EmptyType(newSchemaPath);
            } else if (nodeType instanceof EnumTypeDefinition) {
                EnumTypeDefinition enumType = (EnumTypeDefinition) nodeType;
                result = new EnumerationType(newSchemaPath, (EnumPair) enumType.getDefaultValue(), enumType.getValues());
            } else if (nodeType instanceof IdentityrefTypeDefinition) {
                IdentityrefTypeDefinition idrefType = (IdentityrefTypeDefinition) nodeType;
                result = new IdentityrefType(idrefType.getIdentity(), newSchemaPath);
            } else if (nodeType instanceof InstanceIdentifierTypeDefinition) {
                InstanceIdentifierTypeDefinition instIdType = (InstanceIdentifierTypeDefinition) nodeType;
                return new InstanceIdentifier(newSchemaPath, instIdType.getPathStatement(),
                        instIdType.requireInstance());
            } else if (nodeType instanceof StringTypeDefinition) {
                result = TypeUtils.createNewStringType(parentSchemaPath, nodeQName, (StringTypeDefinition) nodeType);
            } else if (nodeType instanceof IntegerTypeDefinition) {
                result = TypeUtils.createNewIntType(parentSchemaPath, nodeQName, (IntegerTypeDefinition) nodeType);
            } else if (nodeType instanceof UnsignedIntegerTypeDefinition) {
                result = TypeUtils.createNewUintType(parentSchemaPath, nodeQName,
                        (UnsignedIntegerTypeDefinition) nodeType);
            } else if (nodeType instanceof LeafrefTypeDefinition) {
                result = new Leafref(newSchemaPath, ((LeafrefTypeDefinition) nodeType).getPathStatement());
            } else if (nodeType instanceof UnionTypeDefinition) {
                UnionTypeDefinition unionType = (UnionTypeDefinition) nodeType;
                return new UnionType(newSchemaPath, unionType.getTypes());
            } else if (nodeType instanceof ExtendedType) {
                ExtendedType extType = (ExtendedType) nodeType;
                result = TypeUtils.createNewExtendedType(extType, newSchemaPath);
            }
        }
        return result;
    }

    /**
     * Create LeafSchemaNodeBuilder from given LeafSchemaNode.
     *
     * @param leaf
     *            leaf from which to create builder
     * @param qname
     * @param moduleName
     *            current module name
     * @param line
     *            line in module
     * @return leaf builder based on given leaf node
     */
    public static LeafSchemaNodeBuilder createLeafBuilder(LeafSchemaNode leaf, QName qname, String moduleName, int line) {
        final LeafSchemaNodeBuilder builder = new LeafSchemaNodeBuilder(moduleName, line, qname, leaf.getPath());
        convertDataSchemaNode(leaf, builder);
        builder.setConfiguration(leaf.isConfiguration());
        final TypeDefinition<?> type = leaf.getType();
        builder.setType(type);
        builder.setPath(leaf.getPath());
        builder.setUnknownNodes(leaf.getUnknownSchemaNodes());
        builder.setDefaultStr(leaf.getDefault());
        builder.setUnits(leaf.getUnits());
        return builder;
    }

    /**
     * Create ContainerSchemaNodeBuilder from given ContainerSchemaNode.
     *
     * @param container
     * @param qname
     * @param moduleName
     *            current module name
     * @param line
     *            current line in module
     * @return container builder based on given container node
     */
    public static ContainerSchemaNodeBuilder createContainer(ContainerSchemaNode container, QName qname,
            String moduleName, int line) {
        final ContainerSchemaNodeBuilder builder = new ContainerSchemaNodeBuilder(moduleName, line, qname,
                container.getPath());
        convertDataSchemaNode(container, builder);
        builder.setConfiguration(container.isConfiguration());
        builder.setUnknownNodes(container.getUnknownSchemaNodes());
        builder.setChildNodes(container.getChildNodes());
        builder.setGroupings(container.getGroupings());
        builder.setTypedefs(container.getTypeDefinitions());
        builder.setAugmentations(container.getAvailableAugmentations());
        builder.setUsesnodes(container.getUses());
        builder.setPresence(container.isPresenceContainer());
        return builder;
    }

    /**
     * Create ListSchemaNodeBuilder from given ListSchemaNode.
     *
     * @param list
     * @param qname
     * @param moduleName
     *            current module name
     * @param line
     *            current line in module
     * @return list builder based on given list node
     */
    public static ListSchemaNodeBuilder createList(ListSchemaNode list, QName qname, String moduleName, int line) {
        ListSchemaNodeBuilder builder = new ListSchemaNodeBuilder(moduleName, line, qname, list.getPath());
        convertDataSchemaNode(list, builder);
        builder.setConfiguration(list.isConfiguration());
        builder.setUnknownNodes(list.getUnknownSchemaNodes());
        builder.setTypedefs(list.getTypeDefinitions());
        builder.setChildNodes(list.getChildNodes());
        builder.setGroupings(list.getGroupings());
        builder.setAugmentations(list.getAvailableAugmentations());
        builder.setUsesnodes(list.getUses());
        builder.setUserOrdered(builder.isUserOrdered());
        return builder;
    }

    /**
     * Create LeafListSchemaNodeBuilder from given LeafListSchemaNode.
     *
     * @param leafList
     * @param qname
     * @param moduleName
     *            current module name
     * @param line
     *            current line in module
     * @return leaf-list builder based on given leaf-list node
     */
    public static LeafListSchemaNodeBuilder createLeafList(LeafListSchemaNode leafList, QName qname, String moduleName,
            int line) {
        final LeafListSchemaNodeBuilder builder = new LeafListSchemaNodeBuilder(moduleName, line, qname,
                leafList.getPath());
        convertDataSchemaNode(leafList, builder);
        builder.setConfiguration(leafList.isConfiguration());
        builder.setType(leafList.getType());
        builder.setUnknownNodes(leafList.getUnknownSchemaNodes());
        builder.setUserOrdered(leafList.isUserOrdered());
        return builder;
    }

    /**
     * Create ChoiceBuilder from given ChoiceNode.
     *
     * @param choice
     * @param qname
     * @param moduleName
     *            current module name
     * @param line
     *            current line in module
     * @return choice builder based on given choice node
     */
    public static ChoiceBuilder createChoice(ChoiceNode choice, QName qname, String moduleName, int line) {
        final ChoiceBuilder builder = new ChoiceBuilder(moduleName, line, qname);
        convertDataSchemaNode(choice, builder);
        builder.setConfiguration(choice.isConfiguration());
        builder.setCases(choice.getCases());
        builder.setUnknownNodes(choice.getUnknownSchemaNodes());
        builder.setDefaultCase(choice.getDefaultCase());
        return builder;
    }

    /**
     * Create AnyXmlBuilder from given AnyXmlSchemaNode.
     *
     * @param anyxml
     * @param qname
     * @param moduleName
     *            current module name
     * @param line
     *            current line in module
     * @return anyxml builder based on given anyxml node
     */
    public static AnyXmlBuilder createAnyXml(AnyXmlSchemaNode anyxml, QName qname, String moduleName, int line) {
        final AnyXmlBuilder builder = new AnyXmlBuilder(moduleName, line, qname, anyxml.getPath());
        convertDataSchemaNode(anyxml, builder);
        builder.setConfiguration(anyxml.isConfiguration());
        builder.setUnknownNodes(anyxml.getUnknownSchemaNodes());
        return builder;
    }

    /**
     * Create GroupingBuilder from given GroupingDefinition.
     *
     * @param grouping
     * @param qname
     * @param moduleName
     *            current module name
     * @param line
     *            current line in module
     * @return grouping builder based on given grouping node
     */
    public static GroupingBuilder createGrouping(GroupingDefinition grouping, QName qname, String moduleName, int line) {
        final GroupingBuilderImpl builder = new GroupingBuilderImpl(moduleName, line, qname);
        builder.setPath(grouping.getPath());
        builder.setChildNodes(grouping.getChildNodes());
        builder.setGroupings(grouping.getGroupings());
        builder.setTypedefs(grouping.getTypeDefinitions());
        builder.setUsesnodes(grouping.getUses());
        builder.setUnknownNodes(grouping.getUnknownSchemaNodes());
        builder.setDescription(grouping.getDescription());
        builder.setReference(grouping.getReference());
        builder.setStatus(grouping.getStatus());
        return builder;
    }

    /**
     * Create TypeDefinitionBuilder from given ExtendedType.
     *
     * @param typedef
     * @param qname
     * @param moduleName
     *            current module name
     * @param line
     *            current line in module
     * @return typedef builder based on given typedef node
     */
    public static TypeDefinitionBuilder createTypedef(ExtendedType typedef, QName qname, String moduleName, int line) {
        final TypeDefinitionBuilderImpl builder = new TypeDefinitionBuilderImpl(moduleName, line, qname);
        builder.setPath(typedef.getPath());
        builder.setDefaultValue(typedef.getDefaultValue());
        builder.setUnits(typedef.getUnits());
        builder.setDescription(typedef.getDescription());
        builder.setReference(typedef.getReference());
        builder.setStatus(typedef.getStatus());
        builder.setRanges(typedef.getRanges());
        builder.setLengths(typedef.getLengths());
        builder.setPatterns(typedef.getPatterns());
        builder.setFractionDigits(typedef.getFractionDigits());
        final TypeDefinition<?> type = typedef.getBaseType();
        builder.setType(type);
        builder.setUnits(typedef.getUnits());
        builder.setUnknownNodes(typedef.getUnknownSchemaNodes());
        return builder;
    }

    /**
     * Create UnknownSchemaNodeBuilder from given UnknownSchemaNode.
     *
     * @param unknownNode
     * @param qname
     * @param moduleName
     *            current module name
     * @param line
     *            current line in module
     * @return unknown node builder based on given unknown node
     */
    public static UnknownSchemaNodeBuilder createUnknownSchemaNode(UnknownSchemaNode unknownNode, QName qname,
            String moduleName, int line) {
        final UnknownSchemaNodeBuilder builder = new UnknownSchemaNodeBuilder(moduleName, line, qname);
        builder.setPath(unknownNode.getPath());
        builder.setUnknownNodes(unknownNode.getUnknownSchemaNodes());
        builder.setDescription(unknownNode.getDescription());
        builder.setReference(unknownNode.getReference());
        builder.setStatus(unknownNode.getStatus());
        builder.setAddedByUses(unknownNode.isAddedByUses());
        builder.setNodeType(unknownNode.getNodeType());
        builder.setNodeParameter(unknownNode.getNodeParameter());
        return builder;
    }

    /**
     * Set DataSchemaNode arguments to builder object
     *
     * @param node
     *            node from which arguments should be read
     * @param builder
     *            builder to which arguments should be set
     */
    private static void convertDataSchemaNode(DataSchemaNode node, DataSchemaNodeBuilder builder) {
        builder.setPath(node.getPath());
        builder.setDescription(node.getDescription());
        builder.setReference(node.getReference());
        builder.setStatus(node.getStatus());
        builder.setAugmenting(node.isAugmenting());
        copyConstraintsFromDefinition(node.getConstraints(), builder.getConstraints());
    }

    /**
     * Copy constraints from constraints definition to constraints builder.
     *
     * @param nodeConstraints
     *            definition from which constraints will be copied
     * @param constraints
     *            builder to which constraints will be added
     */
    private static void copyConstraintsFromDefinition(final ConstraintDefinition nodeConstraints,
            final ConstraintsBuilder constraints) {
        final RevisionAwareXPath when = nodeConstraints.getWhenCondition();
        final Set<MustDefinition> must = nodeConstraints.getMustConstraints();

        if (when != null) {
            constraints.addWhenCondition(when.toString());
        }
        if (must != null) {
            for (MustDefinition md : must) {
                constraints.addMustDefinition(md);
            }
        }
        constraints.setMandatory(nodeConstraints.isMandatory());
        constraints.setMinElements(nodeConstraints.getMinElements());
        constraints.setMaxElements(nodeConstraints.getMaxElements());
    }

    /**
     * Find augment target node and perform augmentation.
     *
     * @param augment
     * @param firstNodeParent
     *            parent of first node in path
     * @param path
     *            path to augment target
     * @param isUsesAugment
     *            if this augment is defined under uses node
     * @return true if augment process succeed, false otherwise
     */
    public static boolean processAugmentation(final AugmentationSchemaBuilder augment, final Builder firstNodeParent,
            final List<QName> path, boolean isUsesAugment) {
        // traverse augment target path and try to reach target node
        String currentName = null;
        Builder currentParent = firstNodeParent;

        for (int i = 0; i < path.size(); i++) {
            QName qname = path.get(i);

            currentName = qname.getLocalName();
            if (currentParent instanceof DataNodeContainerBuilder) {
                DataSchemaNodeBuilder nodeFound = ((DataNodeContainerBuilder) currentParent)
                        .getDataChildByName(currentName);
                // if not found as regular child, search in uses
                if (nodeFound == null) {
                    boolean found = false;
                    for (UsesNodeBuilder unb : ((DataNodeContainerBuilder) currentParent).getUsesNodes()) {
                        DataSchemaNodeBuilder result = findNodeInUses(currentName, unb);
                        if (result != null) {
                            currentParent = result;
                            found = true;
                            break;
                        }
                    }
                    // if not found even in uses nodes, return false
                    if (!found) {
                        return false;
                    }
                } else {
                    currentParent = nodeFound;
                }
            } else if (currentParent instanceof ChoiceBuilder) {
                currentParent = ((ChoiceBuilder) currentParent).getCaseNodeByName(currentName);
            } else {
                throw new YangParseException(augment.getModuleName(), augment.getLine(),
                        "Error in augment parsing: failed to find node " + currentName);
            }

            // if node in path not found, return false
            if (currentParent == null) {
                return false;
            }
        }
        if (!(currentParent instanceof DataSchemaNodeBuilder)) {
            throw new YangParseException(
                    augment.getModuleName(),
                    augment.getLine(),
                    "Error in augment parsing: The target node MUST be either a container, list, choice, case, input, output, or notification node.");
        }

        if (currentParent instanceof ChoiceBuilder) {
            fillAugmentTarget(augment, (ChoiceBuilder) currentParent);
        } else {
            fillAugmentTarget(augment, (DataNodeContainerBuilder) currentParent);
        }
        ((AugmentationTargetBuilder) currentParent).addAugmentation(augment);
        SchemaPath oldPath = ((DataSchemaNodeBuilder) currentParent).getPath();
        augment.setTargetPath(new SchemaPath(oldPath.getPath(), oldPath.isAbsolute()));
        augment.setResolved(true);

        return true;
    }

    private static DataSchemaNodeBuilder findNodeInUses(String localName, UsesNodeBuilder uses) {
        Set<DataSchemaNodeBuilder> usesTargetChildren = uses.getTargetChildren();
        if (usesTargetChildren != null) {
            for (DataSchemaNodeBuilder child : uses.getTargetChildren()) {
                if (child.getQName().getLocalName().equals(localName)) {
                    return child;
                }
            }
        }
        for (UsesNodeBuilder usesNode : uses.getTargetGroupingUses()) {
            DataSchemaNodeBuilder result = findNodeInUses(localName, usesNode);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Find augment target node in given context and perform augmentation.
     *
     * @param augment
     * @param path
     *            path to augment target
     * @param module
     *            current module
     * @param prefix
     *            current prefix of target module
     * @param context
     *            SchemaContext containing already resolved modules
     * @return true if augment process succeed, false otherwise
     */
    public static boolean processAugmentationOnContext(final AugmentationSchemaBuilder augment, final List<QName> path,
            final ModuleBuilder module, final String prefix, final SchemaContext context) {
        final int line = augment.getLine();
        final Module dependentModule = findModuleFromContext(context, module, prefix, line);
        if (dependentModule == null) {
            throw new YangParseException(module.getName(), line,
                    "Error in augment parsing: failed to find module with prefix " + prefix + ".");
        }

        String currentName = path.get(0).getLocalName();
        SchemaNode currentParent = dependentModule.getDataChildByName(currentName);
        if (currentParent == null) {
            Set<NotificationDefinition> notifications = dependentModule.getNotifications();
            for (NotificationDefinition ntf : notifications) {
                if (ntf.getQName().getLocalName().equals(currentName)) {
                    currentParent = ntf;
                    break;
                }
            }
        }
        if (currentParent == null) {
            throw new YangParseException(module.getName(), line, "Error in augment parsing: failed to find node "
                    + currentName + ".");
        }

        for (int i = 1; i < path.size(); i++) {
            currentName = path.get(i).getLocalName();
            if (currentParent instanceof DataNodeContainer) {
                currentParent = ((DataNodeContainer) currentParent).getDataChildByName(currentName);
            } else if (currentParent instanceof ChoiceNode) {
                currentParent = ((ChoiceNode) currentParent).getCaseNodeByName(currentName);
            } else {
                throw new YangParseException(augment.getModuleName(), line,
                        "Error in augment parsing: failed to find node " + currentName);
            }
            // if node in path not found, return false
            if (currentParent == null) {
                throw new YangParseException(module.getName(), line, "Error in augment parsing: failed to find node "
                        + currentName + ".");
            }
        }

        if (currentParent instanceof ContainerSchemaNodeImpl) {
            // includes container, input and output statement
            ContainerSchemaNodeImpl c = (ContainerSchemaNodeImpl) currentParent;
            ContainerSchemaNodeBuilder cb = c.toBuilder();
            fillAugmentTarget(augment, cb);
            ((AugmentationTargetBuilder) cb).addAugmentation(augment);
            SchemaPath oldPath = cb.getPath();
            cb.rebuild();
            augment.setTargetPath(new SchemaPath(oldPath.getPath(), oldPath.isAbsolute()));
            augment.setResolved(true);
        } else if (currentParent instanceof ListSchemaNodeImpl) {
            ListSchemaNodeImpl l = (ListSchemaNodeImpl) currentParent;
            ListSchemaNodeBuilder lb = l.toBuilder();
            fillAugmentTarget(augment, lb);
            ((AugmentationTargetBuilder) lb).addAugmentation(augment);
            SchemaPath oldPath = lb.getPath();
            lb.rebuild();
            augment.setTargetPath(new SchemaPath(oldPath.getPath(), oldPath.isAbsolute()));
            augment.setResolved(true);
        } else if (currentParent instanceof ChoiceNodeImpl) {
            ChoiceNodeImpl ch = (ChoiceNodeImpl) currentParent;
            ChoiceBuilder chb = ch.toBuilder();
            fillAugmentTarget(augment, chb);
            ((AugmentationTargetBuilder) chb).addAugmentation(augment);
            SchemaPath oldPath = chb.getPath();
            chb.rebuild();
            augment.setTargetPath(new SchemaPath(oldPath.getPath(), oldPath.isAbsolute()));
            augment.setResolved(true);
        } else if (currentParent instanceof ChoiceCaseNodeImpl) {
            ChoiceCaseNodeImpl chc = (ChoiceCaseNodeImpl) currentParent;
            ChoiceCaseBuilder chcb = chc.toBuilder();
            fillAugmentTarget(augment, chcb);
            ((AugmentationTargetBuilder) chcb).addAugmentation(augment);
            SchemaPath oldPath = chcb.getPath();
            chcb.rebuild();
            augment.setTargetPath(new SchemaPath(oldPath.getPath(), oldPath.isAbsolute()));
            augment.setResolved(true);
        } else if (currentParent instanceof NotificationDefinitionImpl) {
            NotificationDefinitionImpl nd = (NotificationDefinitionImpl) currentParent;
            NotificationBuilder nb = nd.toBuilder();
            fillAugmentTarget(augment, nb);
            ((AugmentationTargetBuilder) nb).addAugmentation(augment);
            SchemaPath oldPath = nb.getPath();
            nb.rebuild();
            augment.setTargetPath(new SchemaPath(oldPath.getPath(), oldPath.isAbsolute()));
            augment.setResolved(true);
        } else {
            throw new YangParseException(module.getName(), line, "Target of type " + currentParent.getClass()
                    + " cannot be augmented.");
        }

        return true;
    }

    public static QName findFullQName(final Map<String, TreeMap<Date, ModuleBuilder>> modules,
            final ModuleBuilder module, final IdentityrefTypeBuilder idref) {
        QName result = null;
        String baseString = idref.getBaseString();
        if (baseString.contains(":")) {
            String[] splittedBase = baseString.split(":");
            if (splittedBase.length > 2) {
                throw new YangParseException(module.getName(), idref.getLine(), "Failed to parse identityref base: "
                        + baseString);
            }
            String prefix = splittedBase[0];
            String name = splittedBase[1];
            ModuleBuilder dependentModule = findDependentModuleBuilder(modules, module, prefix, idref.getLine());
            result = new QName(dependentModule.getNamespace(), dependentModule.getRevision(), prefix, name);
        } else {
            result = new QName(module.getNamespace(), module.getRevision(), module.getPrefix(), baseString);
        }
        return result;
    }

    /**
     * Load uses target nodes and all uses target uses target nodes. Set this
     * collection as uses final children.
     *
     * @param module
     *            current module
     * @param usesNode
     */
    public static void processUsesNode(final UsesNodeBuilder usesNode) {
        ModuleBuilder module = getParentModule(usesNode);
        DataNodeContainerBuilder parent = usesNode.getParent();
        URI namespace = null;
        Date revision = null;
        String prefix = null;
        if (parent instanceof ModuleBuilder || parent instanceof AugmentationSchemaBuilder) {
            namespace = module.getNamespace();
            revision = module.getRevision();
            prefix = module.getPrefix();
        } else {
            QName parentQName = parent.getQName();
            namespace = parentQName.getNamespace();
            revision = parentQName.getRevision();
            prefix = parentQName.getPrefix();
        }
        SchemaPath parentPath = parent.getPath();

        // child nodes
        Set<DataSchemaNodeBuilder> finalChildren = new HashSet<>();
        Set<DataSchemaNodeBuilder> newChildren = GroupingUtils.copyUsesTargetNodesWithNewPath(usesNode, parent);
        finalChildren.addAll(newChildren);
        usesNode.getFinalChildren().addAll(finalChildren);

        // groupings
        Set<GroupingBuilder> finalGroupings = new HashSet<>();
        Set<GroupingBuilder> newGroupings = GroupingUtils.copyUsesTargetGroupingsWithNewPath(usesNode, parentPath,
                namespace, revision, prefix);
        finalGroupings.addAll(newGroupings);
        usesNode.getFinalGroupings().addAll(finalGroupings);

        // typedefs
        Set<TypeDefinitionBuilder> finalTypedefs = new HashSet<>();
        Set<TypeDefinitionBuilder> newTypedefs = GroupingUtils.copyUsesTargetTypedefsWithNewPath(usesNode, parentPath,
                namespace, revision, prefix);
        finalTypedefs.addAll(newTypedefs);
        usesNode.getFinalTypedefs().addAll(finalTypedefs);

        // unknown nodes
        List<UnknownSchemaNodeBuilder> finalUnknownNodes = new ArrayList<>();
        List<UnknownSchemaNodeBuilder> newUnknownNodes = GroupingUtils.copyUsesTargetUnknownNodesWithNewPath(usesNode,
                parentPath, namespace, revision, prefix);
        finalUnknownNodes.addAll(newUnknownNodes);
        usesNode.getFinalUnknownNodes().addAll(finalUnknownNodes);
    }

    /**
     * Add nodes defined in uses target grouping to uses parent.
     *
     * @param usesNode
     */
    public static void updateUsesParent(UsesNodeBuilder usesNode, DataNodeContainerBuilder parent) {
        // child nodes
        for (DataSchemaNodeBuilder child : usesNode.getFinalChildren()) {
            child.setParent(parent);
            parent.addChildNode(child);
        }
        for (UsesNodeBuilder uses : usesNode.getTargetGroupingUses()) {
            updateUsesParent(uses, parent);
        }

        // groupings
        for (GroupingBuilder gb : usesNode.getFinalGroupings()) {
            parent.addGrouping(gb);
        }
        // typedefs
        for (TypeDefinitionBuilder tdb : usesNode.getFinalTypedefs()) {
            parent.addTypedef(tdb);
        }
        // unknown nodes
        for (UnknownSchemaNodeBuilder un : usesNode.getFinalUnknownNodes()) {
            parent.addUnknownNodeBuilder(un);
        }
    }

    public static void fixUsesNodesPath(UsesNodeBuilder usesNode) {
        DataNodeContainerBuilder parent = usesNode.getParent();

        // child nodes
        Set<DataSchemaNodeBuilder> currentChildNodes = parent.getChildNodeBuilders();
        Set<DataSchemaNodeBuilder> toRemove = new HashSet<>();
        Set<DataSchemaNodeBuilder> toAdd = new HashSet<>();
        for (DataSchemaNodeBuilder child : currentChildNodes) {
            if (child instanceof GroupingMember) {
                GroupingMember gm = (GroupingMember) child;
                if (gm.isAddedByUses()) {
                    toRemove.add(child);
                    DataSchemaNodeBuilder copy = CopyUtils.copy(child, parent, true);
                    correctNodePath(copy, parent.getPath());
                    toAdd.add(copy);
                }
            }
        }
        currentChildNodes.removeAll(toRemove);
        currentChildNodes.addAll(toAdd);

        // groupings
        Set<GroupingBuilder> currentGroupings = parent.getGroupingBuilders();
        Set<GroupingBuilder> toRemoveG = new HashSet<>();
        Set<GroupingBuilder> toAddG = new HashSet<>();
        for (GroupingBuilder child : currentGroupings) {
            if (child.isAddedByUses()) {
                toRemoveG.add(child);
                GroupingBuilder copy = CopyUtils.copy(child, parent, true);
                correctNodePath(copy, parent.getPath());
                toAddG.add(copy);
            }

        }
        currentGroupings.removeAll(toRemoveG);
        currentGroupings.addAll(toAddG);

        // typedefs
        Set<TypeDefinitionBuilder> currentTypedefs = parent.getTypeDefinitionBuilders();
        Set<TypeDefinitionBuilder> toRemoveTD = new HashSet<>();
        Set<TypeDefinitionBuilder> toAddTD = new HashSet<>();
        for (TypeDefinitionBuilder child : currentTypedefs) {
            if (child.isAddedByUses()) {
                toRemoveTD.add(child);
                TypeDefinitionBuilder copy = CopyUtils.copy(child, parent, true);
                correctNodePath(copy, parent.getPath());
                toAddTD.add(copy);
            }

        }
        currentTypedefs.removeAll(toRemoveTD);
        currentTypedefs.addAll(toAddTD);

        // unknown nodes
        List<UnknownSchemaNodeBuilder> currentUN = parent.getUnknownNodeBuilders();
        List<UnknownSchemaNodeBuilder> toRemoveUN = new ArrayList<>();
        List<UnknownSchemaNodeBuilder> toAddUN = new ArrayList<>();
        for (UnknownSchemaNodeBuilder un : currentUN) {
            if (un.isAddedByUses()) {
                toRemoveUN.add(un);
                UnknownSchemaNodeBuilder copy = CopyUtils.copy(un, parent, true);
                correctNodePath(copy, parent.getPath());
                toAddUN.add(copy);
            }
        }
        currentUN.removeAll(toRemoveUN);
        currentUN.addAll(toAddUN);
    }

    /**
     * Perform refine process on uses children. It is expected that uses has
     * already resolved all dependencies.
     *
     * @param usesNode
     */
    public static void performRefine(UsesNodeBuilder usesNode) {
        for (RefineHolder refine : usesNode.getRefines()) {
            DataSchemaNodeBuilder nodeToRefine = null;
            for (DataSchemaNodeBuilder dataNode : usesNode.getFinalChildren()) {
                if (refine.getName().equals(dataNode.getQName().getLocalName())) {
                    nodeToRefine = dataNode;
                    break;
                }
            }
            if (nodeToRefine == null) {
                throw new YangParseException(refine.getModuleName(), refine.getLine(), "Refine target node '"
                        + refine.getName() + "' not found");
            }
            RefineUtils.performRefine(nodeToRefine, refine);
            usesNode.addRefineNode(nodeToRefine);
        }
    }

    /**
     * Get module in which this node is defined.
     *
     * @param node
     * @return builder of module where this node is defined
     */
    public static ModuleBuilder getParentModule(Builder node) {
        Builder parent = node.getParent();
        while (!(parent instanceof ModuleBuilder)) {
            parent = parent.getParent();
        }
        return (ModuleBuilder) parent;
    }

}
