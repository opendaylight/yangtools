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
import org.opendaylight.yangtools.yang.parser.builder.impl.RpcDefinitionBuilder;
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
     * @return
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

        Date dependentModuleRevision = null;

        final ModuleImport dependentModuleImport = ParserUtils.getModuleImport(currentModule, prefix);
        if (dependentModuleImport == null) {
            throw new YangParseException(currentModule.getName(), line, "No import found with prefix '" + prefix + "'.");
        }
        final String dependentModuleName = dependentModuleImport.getModuleName();
        dependentModuleRevision = dependentModuleImport.getRevision();

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
     * Find grouping by name.
     *
     * @param groupings
     *            collection of grouping builders to search
     * @param name
     *            name of grouping
     * @return grouping with given name if present in collection, null otherwise
     */
    public static GroupingBuilder findGroupingBuilder(Set<GroupingBuilder> groupings, String name) {
        for (GroupingBuilder grouping : groupings) {
            if (grouping.getQName().getLocalName().equals(name)) {
                return grouping;
            }
        }
        return null;
    }

    /**
     * Find grouping by name.
     *
     * @param groupings
     *            collection of grouping definitions to search
     * @param name
     *            name of grouping
     * @return grouping with given name if present in collection, null otherwise
     */
    public static GroupingDefinition findGroupingDefinition(Set<GroupingDefinition> groupings, String name) {
        for (GroupingDefinition grouping : groupings) {
            if (grouping.getQName().getLocalName().equals(name)) {
                return grouping;
            }
        }
        return null;
    }

    public static Set<DataSchemaNodeBuilder> processUsesDataSchemaNode(UsesNodeBuilder usesNode,
            Set<DataSchemaNodeBuilder> children, SchemaPath parentPath, URI namespace, Date revision, String prefix) {
        Set<DataSchemaNodeBuilder> newChildren = new HashSet<>();
        for (DataSchemaNodeBuilder child : children) {
            if (child != null) {
                DataSchemaNodeBuilder newChild = null;
                QName qname = new QName(namespace, revision, prefix, child.getQName().getLocalName());
                if (child instanceof AnyXmlBuilder) {
                    newChild = new AnyXmlBuilder((AnyXmlBuilder) child, qname);
                } else if (child instanceof ChoiceBuilder) {
                    newChild = new ChoiceBuilder((ChoiceBuilder) child, qname);
                } else if (child instanceof ContainerSchemaNodeBuilder) {
                    newChild = new ContainerSchemaNodeBuilder((ContainerSchemaNodeBuilder) child, qname);
                } else if (child instanceof LeafListSchemaNodeBuilder) {
                    newChild = new LeafListSchemaNodeBuilder((LeafListSchemaNodeBuilder) child, qname);
                } else if (child instanceof LeafSchemaNodeBuilder) {
                    newChild = new LeafSchemaNodeBuilder((LeafSchemaNodeBuilder) child, qname);
                } else if (child instanceof ListSchemaNodeBuilder) {
                    newChild = new ListSchemaNodeBuilder((ListSchemaNodeBuilder) child, qname);
                }

                if (newChild == null) {
                    throw new YangParseException(usesNode.getModuleName(), usesNode.getLine(),
                            "Unknown member of target grouping while resolving uses node.");
                }
                if (newChild instanceof GroupingMember) {
                    ((GroupingMember) newChild).setAddedByUses(true);
                }

                correctNodePath(newChild, parentPath);
                newChildren.add(newChild);
            }
        }
        return newChildren;
    }

    /**
     * Traverse given groupings and create new collection of groupings with
     * schema path created based on current parent path.
     *
     * @param groupings
     * @param parentPath
     * @param namespace
     * @param revision
     * @param prefix
     * @return collection of new groupings with corrected path
     */
    public static Set<GroupingBuilder> processUsesGroupings(Set<GroupingBuilder> groupings, SchemaPath parentPath,
            URI namespace, Date revision, String prefix) {
        Set<GroupingBuilder> newGroupings = new HashSet<>();
        for (GroupingBuilder g : groupings) {
            QName qname = new QName(namespace, revision, prefix, g.getQName().getLocalName());
            GroupingBuilder newGrouping = new GroupingBuilderImpl(g, qname);
            newGrouping.setAddedByUses(true);
            correctNodePath(newGrouping, parentPath);
            newGroupings.add(newGrouping);
        }
        return newGroupings;
    }

    public static Set<TypeDefinitionBuilder> processUsesTypedefs(Set<TypeDefinitionBuilder> typedefs,
            SchemaPath parentPath, URI namespace, Date revision, String prefix) {
        Set<TypeDefinitionBuilder> newTypedefs = new HashSet<>();
        for (TypeDefinitionBuilder td : typedefs) {
            QName qname = new QName(namespace, revision, prefix, td.getQName().getLocalName());
            TypeDefinitionBuilder newType = new TypeDefinitionBuilderImpl(td, qname);
            newType.setAddedByUses(true);
            correctNodePath(newType, parentPath);
            newTypedefs.add(newType);
        }
        return newTypedefs;
    }

    public static List<UnknownSchemaNodeBuilder> processUsesUnknownNodes(List<UnknownSchemaNodeBuilder> unknownNodes,
            SchemaPath parentPath, URI namespace, Date revision, String prefix) {
        List<UnknownSchemaNodeBuilder> newUnknownNodes = new ArrayList<>();
        for (UnknownSchemaNodeBuilder un : unknownNodes) {
            QName qname = new QName(namespace, revision, prefix, un.getQName().getLocalName());
            UnknownSchemaNodeBuilder newUn = new UnknownSchemaNodeBuilder(un, qname);
            newUn.setAddedByUses(true);
            correctNodePath(newUn, parentPath);
            newUnknownNodes.add(newUn);
        }
        return newUnknownNodes;
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
        boolean usesAugment = augment.getParent() instanceof UsesNodeBuilder;
        for (DataSchemaNodeBuilder builder : augment.getChildNodeBuilders()) {
            builder.setAugmenting(true);
            if (usesAugment) {
                if (builder instanceof GroupingMember) {
                    ((GroupingMember) builder).setAddedByUses(true);
                }
            }
            correctNodePath(builder, target.getPath());
            target.addChildNode(builder);
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
        boolean usesAugment = augment.getParent() instanceof UsesNodeBuilder;
        for (DataSchemaNodeBuilder builder : augment.getChildNodeBuilders()) {
            builder.setAugmenting(true);
            if (usesAugment) {
                if (builder instanceof GroupingMember) {
                    ((GroupingMember) builder).setAddedByUses(true);
                }
            }
            correctNodePath(builder, target.getPath());
            target.addCase(builder);
        }
    }

    private static void correctNodePath(final SchemaNodeBuilder node, final SchemaPath parentSchemaPath) {
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
            correctTypeAwareNodePath(nodeBuilder, parentSchemaPath);
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

    public static void processAugmentationOnContext(final AugmentationSchemaBuilder augmentBuilder,
            final List<QName> path, final ModuleBuilder module, final String prefix, final int line,
            final SchemaContext context) {
        final Module dependentModule = findModuleFromContext(context, module, prefix, line);
        if (dependentModule == null) {
            throw new YangParseException(module.getName(), line, "Failed to find referenced module with prefix "
                    + prefix + ".");
        }
        SchemaNode node = dependentModule.getDataChildByName(path.get(0).getLocalName());
        if (node == null) {
            Set<NotificationDefinition> notifications = dependentModule.getNotifications();
            for (NotificationDefinition ntf : notifications) {
                if (ntf.getQName().getLocalName().equals(path.get(0).getLocalName())) {
                    node = ntf;
                    break;
                }
            }
        }
        if (node == null) {
            return;
        }

        for (int i = 1; i < path.size(); i++) {
            if (node instanceof DataNodeContainer) {
                DataNodeContainer ref = (DataNodeContainer) node;
                node = ref.getDataChildByName(path.get(i).getLocalName());
            }
        }
        if (node == null) {
            return;
        }

        if (node instanceof ContainerSchemaNodeImpl) {
            // includes container, input and output statement
            ContainerSchemaNodeImpl c = (ContainerSchemaNodeImpl) node;
            ContainerSchemaNodeBuilder cb = c.toBuilder();
            fillAugmentTarget(augmentBuilder, cb);
            ((AugmentationTargetBuilder) cb).addAugmentation(augmentBuilder);
            SchemaPath oldPath = cb.getPath();
            cb.rebuild();
            augmentBuilder.setTargetPath(new SchemaPath(oldPath.getPath(), oldPath.isAbsolute()));
            augmentBuilder.setResolved(true);
            module.augmentResolved();
        } else if (node instanceof ListSchemaNodeImpl) {
            ListSchemaNodeImpl l = (ListSchemaNodeImpl) node;
            ListSchemaNodeBuilder lb = l.toBuilder();
            fillAugmentTarget(augmentBuilder, lb);
            ((AugmentationTargetBuilder) lb).addAugmentation(augmentBuilder);
            SchemaPath oldPath = lb.getPath();
            lb.rebuild();
            augmentBuilder.setTargetPath(new SchemaPath(oldPath.getPath(), oldPath.isAbsolute()));
            augmentBuilder.setResolved(true);
            module.augmentResolved();
        } else if (node instanceof ChoiceNodeImpl) {
            ChoiceNodeImpl ch = (ChoiceNodeImpl) node;
            ChoiceBuilder chb = ch.toBuilder();
            fillAugmentTarget(augmentBuilder, chb);
            ((AugmentationTargetBuilder) chb).addAugmentation(augmentBuilder);
            SchemaPath oldPath = chb.getPath();
            chb.rebuild();
            augmentBuilder.setTargetPath(new SchemaPath(oldPath.getPath(), oldPath.isAbsolute()));
            augmentBuilder.setResolved(true);
            module.augmentResolved();
        } else if (node instanceof ChoiceCaseNodeImpl) {
            ChoiceCaseNodeImpl chc = (ChoiceCaseNodeImpl) node;
            ChoiceCaseBuilder chcb = chc.toBuilder();
            fillAugmentTarget(augmentBuilder, chcb);
            ((AugmentationTargetBuilder) chcb).addAugmentation(augmentBuilder);
            SchemaPath oldPath = chcb.getPath();
            chcb.rebuild();
            augmentBuilder.setTargetPath(new SchemaPath(oldPath.getPath(), oldPath.isAbsolute()));
            augmentBuilder.setResolved(true);
            module.augmentResolved();
        } else if (node instanceof NotificationDefinitionImpl) {
            NotificationDefinitionImpl nd = (NotificationDefinitionImpl) node;
            NotificationBuilder nb = nd.toBuilder();
            fillAugmentTarget(augmentBuilder, nb);
            ((AugmentationTargetBuilder) nb).addAugmentation(augmentBuilder);
            SchemaPath oldPath = nb.getPath();
            nb.rebuild();
            augmentBuilder.setTargetPath(new SchemaPath(oldPath.getPath(), oldPath.isAbsolute()));
            augmentBuilder.setResolved(true);
            module.augmentResolved();
        } else {
            throw new YangParseException(module.getName(), line, "Target of type " + node.getClass()
                    + " cannot be augmented.");
        }
    }

    public static void processAugmentation(final AugmentationSchemaBuilder augmentBuilder, final List<QName> path,
            final ModuleBuilder module, final ModuleBuilder dependentModuleBuilder) {
        DataSchemaNodeBuilder currentParent = null;
        for (DataSchemaNodeBuilder child : dependentModuleBuilder.getChildNodeBuilders()) {
            final QName childQName = child.getQName();
            if (childQName.getLocalName().equals(path.get(0).getLocalName())) {
                currentParent = child;
                break;
            }
        }

        if (currentParent == null) {
            return;
        }

        for (int i = 1; i < path.size(); i++) {
            final QName currentQName = path.get(i);
            DataSchemaNodeBuilder newParent = null;
            if (currentParent instanceof DataNodeContainerBuilder) {
                for (DataSchemaNodeBuilder child : ((DataNodeContainerBuilder) currentParent).getChildNodeBuilders()) {
                    final QName childQName = child.getQName();
                    if (childQName.getLocalName().equals(currentQName.getLocalName())) {
                        newParent = child;
                        break;
                    }
                }
            } else if (currentParent instanceof ChoiceBuilder) {
                for (ChoiceCaseBuilder caseBuilder : ((ChoiceBuilder) currentParent).getCases()) {
                    final QName caseQName = caseBuilder.getQName();
                    if (caseQName.getLocalName().equals(currentQName.getLocalName())) {
                        newParent = caseBuilder;
                        break;
                    }
                }
            }

            if (newParent == null) {
                break; // node not found, quit search
            } else {
                currentParent = newParent;
            }
        }

        final String currentName = currentParent.getQName().getLocalName();
        final String lastAugmentPathElementName = path.get(path.size() - 1).getLocalName();
        if (currentName.equals(lastAugmentPathElementName)) {

            if (currentParent instanceof ChoiceBuilder) {
                fillAugmentTarget(augmentBuilder, (ChoiceBuilder) currentParent);
            } else {
                fillAugmentTarget(augmentBuilder, (DataNodeContainerBuilder) currentParent);
            }
            ((AugmentationTargetBuilder) currentParent).addAugmentation(augmentBuilder);
            SchemaPath oldPath = currentParent.getPath();
            augmentBuilder.setTargetPath(new SchemaPath(oldPath.getPath(), oldPath.isAbsolute()));
            augmentBuilder.setResolved(true);
            module.augmentResolved();
        }
    }

    /**
     * Search given modules for grouping by name defined in uses node.
     *
     * @param usesBuilder
     *            builder of uses statement
     * @param modules
     *            all loaded modules
     * @param module
     *            current module
     * @return grouping with given name if found, null otherwise
     */
    public static GroupingBuilder getTargetGroupingFromModules(final UsesNodeBuilder usesBuilder,
            final Map<String, TreeMap<Date, ModuleBuilder>> modules, final ModuleBuilder module) {
        final int line = usesBuilder.getLine();
        final String groupingString = usesBuilder.getGroupingName();
        String groupingPrefix;
        String groupingName;

        if (groupingString.contains(":")) {
            String[] splitted = groupingString.split(":");
            if (splitted.length != 2 || groupingString.contains("/")) {
                throw new YangParseException(module.getName(), line, "Invalid name of target grouping");
            }
            groupingPrefix = splitted[0];
            groupingName = splitted[1];
        } else {
            groupingPrefix = module.getPrefix();
            groupingName = groupingString;
        }

        ModuleBuilder dependentModule = null;
        if (groupingPrefix.equals(module.getPrefix())) {
            dependentModule = module;
        } else {
            dependentModule = findDependentModuleBuilder(modules, module, groupingPrefix, line);
        }

        if (dependentModule == null) {
            return null;
        }

        GroupingBuilder result = null;
        Set<GroupingBuilder> groupings = dependentModule.getGroupingBuilders();
        result = findGroupingBuilder(groupings, groupingName);
        if (result != null) {
            return result;
        }

        Builder parent = usesBuilder.getParent();

        while (parent != null) {
            if (parent instanceof DataNodeContainerBuilder) {
                groupings = ((DataNodeContainerBuilder) parent).getGroupingBuilders();
            } else if (parent instanceof RpcDefinitionBuilder) {
                groupings = ((RpcDefinitionBuilder) parent).getGroupings();
            }
            result = findGroupingBuilder(groupings, groupingName);
            if (result == null) {
                parent = parent.getParent();
            } else {
                break;
            }
        }

        if (result == null) {
            throw new YangParseException(module.getName(), line, "Referenced grouping '" + groupingName
                    + "' not found.");
        }
        return result;
    }

    /**
     * Search context for grouping by name defined in uses node.
     *
     * @param usesBuilder
     *            builder of uses statement
     * @param module
     *            current module
     * @param context
     *            SchemaContext containing already resolved modules
     * @return grouping with given name if found, null otherwise
     */
    public static GroupingDefinition getTargetGroupingFromContext(final UsesNodeBuilder usesBuilder,
            final ModuleBuilder module, final SchemaContext context) {
        final int line = usesBuilder.getLine();
        String groupingString = usesBuilder.getGroupingName();
        String groupingPrefix;
        String groupingName;

        if (groupingString.contains(":")) {
            String[] splitted = groupingString.split(":");
            if (splitted.length != 2 || groupingString.contains("/")) {
                throw new YangParseException(module.getName(), line, "Invalid name of target grouping");
            }
            groupingPrefix = splitted[0];
            groupingName = splitted[1];
        } else {
            groupingPrefix = module.getPrefix();
            groupingName = groupingString;
        }

        Module dependentModule = findModuleFromContext(context, module, groupingPrefix, line);
        return findGroupingDefinition(dependentModule.getGroupings(), groupingName);
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

}
