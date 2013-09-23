/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.SchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.AnyXmlBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.AugmentationSchemaBuilderImpl;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceCaseBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ConstraintsBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ContainerSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.GroupingBuilderImpl;
import org.opendaylight.yangtools.yang.parser.builder.impl.IdentityrefTypeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.LeafListSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.LeafSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ListSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.TypeDefinitionBuilderImpl;
import org.opendaylight.yangtools.yang.parser.builder.impl.UnionTypeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.UnknownSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.UsesNodeBuilderImpl;

public final class CopyUtils {

    private CopyUtils() {
    }

    /**
     * Create copy of DataSchemaNodeBuilder with new parent. If updateQName is
     * true, qname of node will be corrected based on new parent.
     * 
     * @param old
     * @param newParent
     * @param updateQName
     * @return copy
     */
    public static DataSchemaNodeBuilder copy(DataSchemaNodeBuilder old, Builder newParent, boolean updateQName) {
        if (old instanceof AnyXmlBuilder) {
            return copy((AnyXmlBuilder) old, newParent, updateQName);
        } else if (old instanceof ChoiceBuilder) {
            return copy((ChoiceBuilder) old, newParent, updateQName);
        } else if (old instanceof ContainerSchemaNodeBuilder) {
            return copy((ContainerSchemaNodeBuilder) old, newParent, updateQName);
        } else if (old instanceof LeafSchemaNodeBuilder) {
            return copy((LeafSchemaNodeBuilder) old, newParent, updateQName);
        } else if (old instanceof LeafListSchemaNodeBuilder) {
            return copy((LeafListSchemaNodeBuilder) old, newParent, updateQName);
        } else if (old instanceof ListSchemaNodeBuilder) {
            return copy((ListSchemaNodeBuilder) old, newParent, updateQName);
        } else if (old instanceof ChoiceCaseBuilder) {
            return copy((ChoiceCaseBuilder) old, newParent, updateQName);
        } else {
            throw new YangParseException(old.getModuleName(), old.getLine(),
                    "Failed to copy node: Unknown type of DataSchemaNode: " + old);
        }
    }

    private static AnyXmlBuilder copy(AnyXmlBuilder old, Builder newParent, boolean updateQName) {
        DataBean data = getdata(old, newParent, updateQName);
        QName newQName = data.qname;
        SchemaPath newSchemaPath = data.schemaPath;

        AnyXmlBuilder copy = new AnyXmlBuilder(newParent.getModuleName(), newParent.getLine(), newQName, newSchemaPath);
        copyConstraints(copy.getConstraints(), old.getConstraints());
        copy.setParent(newParent);
        copy.setPath(newSchemaPath);
        copy.setDescription(old.getDescription());
        copy.setReference(old.getReference());
        copy.setStatus(old.getStatus());
        copy.setAugmenting(old.isAugmenting());
        copy.setAddedByUses(old.isAddedByUses());
        copy.setConfiguration(old.isConfiguration());
        for (UnknownSchemaNodeBuilder un : old.getUnknownNodeBuilders()) {
            copy.addUnknownNodeBuilder((copy(un, copy, updateQName)));
        }

        return copy;
    }

    private static ChoiceBuilder copy(ChoiceBuilder old, Builder newParent, boolean updateQName) {
        DataBean data = getdata(old, newParent, updateQName);
        QName newQName = data.qname;
        SchemaPath newSchemaPath = data.schemaPath;

        ChoiceBuilder copy = new ChoiceBuilder(newParent.getModuleName(), newParent.getLine(), newQName);
        copyConstraints(copy.getConstraints(), old.getConstraints());
        copy.setParent(newParent);
        copy.setPath(newSchemaPath);
        copy.setDescription(old.getDescription());
        copy.setReference(old.getReference());
        copy.setStatus(old.getStatus());
        copy.setAugmenting(old.isAugmenting());
        copy.setAddedByUses(old.isAddedByUses());
        copy.setConfiguration(old.isConfiguration());
        for (ChoiceCaseBuilder childNode : old.getCases()) {
            copy.addCase(copy(childNode, copy, updateQName));
        }
        for (AugmentationSchemaBuilder augment : old.getAugmentations()) {
            copy.addAugmentation(copyAugment(augment, copy));
        }
        for (UnknownSchemaNodeBuilder un : old.getUnknownNodeBuilders()) {
            copy.addUnknownNodeBuilder((copy(un, copy, updateQName)));
        }

        return copy;
    }

    private static ChoiceCaseBuilder copy(ChoiceCaseBuilder old, Builder newParent, boolean updateQName) {
        DataBean data = getdata(old, newParent, updateQName);
        QName newQName = data.qname;
        SchemaPath newSchemaPath = data.schemaPath;

        ChoiceCaseBuilder copy = new ChoiceCaseBuilder(newParent.getModuleName(), newParent.getLine(), newQName);
        copyConstraints(copy.getConstraints(), old.getConstraints());
        copy.setParent(newParent);
        copy.setPath(newSchemaPath);
        copy.setDescription(old.getDescription());
        copy.setReference(old.getReference());
        copy.setStatus(old.getStatus());
        copy.setAugmenting(old.isAugmenting());
        copy.getChildNodes().addAll(old.getChildNodes());
        for (DataSchemaNodeBuilder childNode : old.getChildNodeBuilders()) {
            copy.addChildNode(copy(childNode, copy, updateQName));
        }
        copy.getGroupings().addAll(old.getGroupings());
        for (GroupingBuilder grouping : old.getGroupingBuilders()) {
            copy.addGrouping(copy(grouping, copy, updateQName));
        }
        for (TypeDefinitionBuilder tdb : old.getTypeDefinitionBuilders()) {
            copy.addTypedef(copy(tdb, copy, updateQName));
        }
        for (UsesNodeBuilder oldUses : old.getUsesNodes()) {
            copy.addUsesNode(copyUses(oldUses, copy));
        }
        for (UnknownSchemaNodeBuilder un : old.getUnknownNodeBuilders()) {
            copy.addUnknownNodeBuilder((copy(un, copy, updateQName)));
        }

        return copy;
    }

    private static ContainerSchemaNodeBuilder copy(ContainerSchemaNodeBuilder old, Builder newParent,
            boolean updateQName) {
        DataBean data = getdata(old, newParent, updateQName);
        QName newQName = data.qname;
        SchemaPath newSchemaPath = data.schemaPath;

        ContainerSchemaNodeBuilder copy = new ContainerSchemaNodeBuilder(newParent.getModuleName(),
                newParent.getLine(), newQName, newSchemaPath);
        copyConstraints(copy.getConstraints(), old.getConstraints());
        copy.setParent(newParent);
        copy.setPath(newSchemaPath);
        copy.setDescription(old.getDescription());
        copy.setReference(old.getReference());
        copy.setStatus(old.getStatus());
        copy.setPresence(old.isPresence());
        copy.setAugmenting(old.isAugmenting());
        copy.setAddedByUses(old.isAddedByUses());
        copy.setConfiguration(old.isConfiguration());
        copy.setChildNodes(old.getChildNodes());
        for (DataSchemaNodeBuilder childNode : old.getChildNodeBuilders()) {
            copy.addChildNode(copy(childNode, copy, updateQName));
        }
        copy.getGroupings().addAll(old.getGroupings());
        for (GroupingBuilder grouping : old.getGroupingBuilders()) {
            copy.addGrouping(copy(grouping, copy, updateQName));
        }
        for (TypeDefinitionBuilder tdb : old.getTypeDefinitionBuilders()) {
            copy.addTypedef(copy(tdb, copy, updateQName));
        }
        for (UsesNodeBuilder oldUses : old.getUsesNodes()) {
            copy.addUsesNode(copyUses(oldUses, copy));
        }
        for (AugmentationSchemaBuilder augment : old.getAugmentations()) {
            copy.addAugmentation(copyAugment(augment, copy));
        }
        for (UnknownSchemaNodeBuilder un : old.getUnknownNodeBuilders()) {
            copy.addUnknownNodeBuilder((copy(un, copy, updateQName)));
        }

        return copy;
    }

    private static LeafSchemaNodeBuilder copy(LeafSchemaNodeBuilder old, Builder newParent, boolean updateQName) {
        DataBean data = getdata(old, newParent, updateQName);
        QName newQName = data.qname;
        SchemaPath newSchemaPath = data.schemaPath;

        LeafSchemaNodeBuilder copy = new LeafSchemaNodeBuilder(newParent.getModuleName(), newParent.getLine(),
                newQName, newSchemaPath);
        copyConstraints(copy.getConstraints(), old.getConstraints());
        copy.setParent(newParent);
        copy.setPath(newSchemaPath);
        copy.setDescription(old.getDescription());
        copy.setReference(old.getReference());
        copy.setStatus(old.getStatus());
        copy.setAugmenting(old.isAugmenting());
        copy.setAddedByUses(old.isAddedByUses());
        copy.setConfiguration(old.isConfiguration());
        for (UnknownSchemaNodeBuilder un : old.getUnknownNodeBuilders()) {
            copy.addUnknownNodeBuilder((copy(un, copy, updateQName)));
        }

        if (old.getType() == null) {
            copy.setTypedef(copy(old.getTypedef(), copy, updateQName));
        } else {
            copy.setType(old.getType());
        }

        copy.setDefaultStr(old.getDefaultStr());
        copy.setUnits(old.getUnits());

        return copy;
    }

    public static LeafListSchemaNodeBuilder copy(LeafListSchemaNodeBuilder old, Builder newParent, boolean updateQName) {
        DataBean data = getdata(old, newParent, updateQName);
        QName newQName = data.qname;
        SchemaPath newSchemaPath = data.schemaPath;

        LeafListSchemaNodeBuilder copy = new LeafListSchemaNodeBuilder(newParent.getModuleName(), newParent.getLine(),
                newQName, newSchemaPath);
        copyConstraints(copy.getConstraints(), old.getConstraints());
        copy.setParent(newParent);
        copy.setPath(newSchemaPath);
        copy.setDescription(old.getDescription());
        copy.setReference(old.getReference());
        copy.setStatus(old.getStatus());
        copy.setAugmenting(old.isAugmenting());
        copy.setAddedByUses(old.isAddedByUses());
        copy.setConfiguration(old.isConfiguration());
        for (UnknownSchemaNodeBuilder un : old.getUnknownNodeBuilders()) {
            copy.addUnknownNodeBuilder((copy(un, copy, updateQName)));
        }

        if (old.getType() == null) {
            copy.setTypedef(copy(old.getTypedef(), copy, updateQName));
        } else {
            copy.setType(old.getType());
        }

        copy.setUserOrdered(old.isUserOrdered());

        return copy;
    }

    private static ListSchemaNodeBuilder copy(ListSchemaNodeBuilder old, Builder newParent, boolean updateQName) {
        DataBean data = getdata(old, newParent, updateQName);
        QName newQName = data.qname;
        SchemaPath newSchemaPath = data.schemaPath;

        ListSchemaNodeBuilder copy = new ListSchemaNodeBuilder(newParent.getModuleName(), newParent.getLine(),
                newQName, newSchemaPath);
        copyConstraints(copy.getConstraints(), old.getConstraints());
        copy.setParent(newParent);
        copy.setPath(newSchemaPath);
        copy.setDescription(old.getDescription());
        copy.setReference(old.getReference());
        copy.setStatus(old.getStatus());
        copy.setAugmenting(old.isAugmenting());
        copy.setAddedByUses(old.isAddedByUses());
        copy.setConfiguration(old.isConfiguration());
        copy.setChildNodes(old.getChildNodes());
        for (DataSchemaNodeBuilder childNode : old.getChildNodeBuilders()) {
            copy.addChildNode(copy(childNode, copy, updateQName));
        }
        copy.getGroupings().addAll(old.getGroupings());
        for (GroupingBuilder grouping : old.getGroupingBuilders()) {
            copy.addGrouping(copy(grouping, copy, updateQName));
        }
        for (TypeDefinitionBuilder tdb : old.getTypeDefinitionBuilders()) {
            copy.addTypedef(copy(tdb, copy, updateQName));
        }
        for (UsesNodeBuilder oldUses : old.getUsesNodes()) {
            copy.addUsesNode(copyUses(oldUses, copy));
        }
        for (AugmentationSchemaBuilder augment : old.getAugmentations()) {
            copy.addAugmentation(copyAugment(augment, copy));
        }
        for (UnknownSchemaNodeBuilder un : old.getUnknownNodeBuilders()) {
            copy.addUnknownNodeBuilder((copy(un, copy, updateQName)));
        }

        copy.setUserOrdered(old.isUserOrdered());
        copy.setKeyDefinition(old.getKeyDefinition());

        return copy;
    }

    public static GroupingBuilder copy(GroupingBuilder old, Builder newParent, boolean updateQName) {
        DataBean data = getdata(old, newParent, updateQName);
        QName newQName = data.qname;
        SchemaPath newSchemaPath = data.schemaPath;

        GroupingBuilderImpl copy = new GroupingBuilderImpl(newParent.getModuleName(), newParent.getLine(), newQName);
        copy.setParent(newParent);
        copy.setPath(newSchemaPath);
        copy.setDescription(old.getDescription());
        copy.setReference(old.getReference());
        copy.setStatus(old.getStatus());
        copy.setAddedByUses(old.isAddedByUses());
        copy.setChildNodes(old.getChildNodes());
        for (DataSchemaNodeBuilder childNode : old.getChildNodeBuilders()) {
            copy.addChildNode(copy(childNode, copy, updateQName));
        }
        copy.getGroupings().addAll(old.getGroupings());
        for (GroupingBuilder grouping : old.getGroupingBuilders()) {
            copy.addGrouping(copy(grouping, copy, updateQName));
        }
        for (TypeDefinitionBuilder tdb : old.getTypeDefinitionBuilders()) {
            copy.addTypedef(copy(tdb, copy, updateQName));
        }
        for (UsesNodeBuilder oldUses : old.getUsesNodes()) {
            copy.addUsesNode(copyUses(oldUses, copy));
        }
        for (UnknownSchemaNodeBuilder un : old.getUnknownNodeBuilders()) {
            copy.addUnknownNodeBuilder((copy(un, copy, updateQName)));
        }

        return copy;
    }

    public static TypeDefinitionBuilder copy(TypeDefinitionBuilder old, Builder newParent, boolean updateQName) {
        DataBean data = getdata(old, newParent, updateQName);
        QName newQName = data.qname;
        SchemaPath newSchemaPath = data.schemaPath;
        TypeDefinitionBuilder type = null;

        if (old instanceof UnionTypeBuilder) {
            UnionTypeBuilder oldUnion = (UnionTypeBuilder) old;
            type = new UnionTypeBuilder(newParent.getModuleName(), newParent.getLine());
            type.setParent(newParent);
            for (TypeDefinition<?> td : oldUnion.getTypes()) {
                type.setType(td);
            }
            for (TypeDefinitionBuilder tdb : oldUnion.getTypedefs()) {
                type.setTypedef(copy(tdb, type, updateQName));
            }
        } else if (old instanceof IdentityrefTypeBuilder) {
            type = new IdentityrefTypeBuilder(newParent.getModuleName(), newParent.getLine(),
                    ((IdentityrefTypeBuilder) old).getBaseString(), newSchemaPath);
            type.setParent(newParent);
            type.setPath(newSchemaPath);
        } else {
            type = new TypeDefinitionBuilderImpl(old.getModuleName(), newParent.getLine(), newQName);
            type.setParent(newParent);
            // TODO
            // type.setPath(newSchemaPath);
            type.setPath(old.getPath());

            if (old.getType() == null) {
                type.setTypedef(copy(old.getTypedef(), type, updateQName));
            } else {
                type.setType(old.getType());
            }

            for (UnknownSchemaNodeBuilder un : old.getUnknownNodeBuilders()) {
                type.addUnknownNodeBuilder((copy(un, type, updateQName)));
            }

            type.setRanges(old.getRanges());
            type.setLengths(old.getLengths());
            type.setPatterns(old.getPatterns());
            type.setFractionDigits(old.getFractionDigits());
            type.setDescription(old.getDescription());
            type.setReference(old.getReference());
            type.setStatus(old.getStatus());
            type.setUnits(old.getUnits());
            type.setDefaultValue(old.getDefaultValue());
            type.setAddedByUses(old.isAddedByUses());
        }

        return type;
    }

    private static ConstraintsBuilder copyConstraints(ConstraintsBuilder newConstraints, ConstraintsBuilder old) {
        newConstraints.getMustDefinitions().addAll(old.getMustDefinitions());
        newConstraints.addWhenCondition(old.getWhenCondition());
        newConstraints.setMandatory(old.isMandatory());
        newConstraints.setMinElements(old.getMinElements());
        newConstraints.setMaxElements(old.getMaxElements());
        return newConstraints;
    }

    static UsesNodeBuilder copyUses(UsesNodeBuilder old, Builder newParent) {
        UsesNodeBuilder copy = new UsesNodeBuilderImpl(newParent.getModuleName(), newParent.getLine(),
                old.getGroupingPathAsString(), true);
        copy.setParent(newParent);
        copy.setGroupingDefinition(old.getGroupingDefinition());
        copy.setGrouping(old.getGroupingBuilder());
        copy.setAddedByUses(old.isAddedByUses());
        copy.getAugmentations().addAll(old.getAugmentations());
        copy.getRefineNodes().addAll(old.getRefineNodes());
        copy.getRefines().addAll(old.getRefines());
        copy.setAugmenting(old.isAugmenting());
        copy.setParentAugment(old.getParentAugment());

        // target child nodes
        Set<DataSchemaNodeBuilder> newTargetChildren = new HashSet<>();
        for (DataSchemaNodeBuilder dnb : old.getTargetChildren()) {
            newTargetChildren.add(copy(dnb, newParent, true));
        }
        copy.getTargetChildren().addAll(newTargetChildren);

        // target typedefs
        Set<TypeDefinitionBuilder> newTargetTypedefs = new HashSet<>();
        for (TypeDefinitionBuilder tdb : old.getTargetTypedefs()) {
            newTargetTypedefs.add(copy(tdb, newParent, true));
        }
        copy.getTargetTypedefs().addAll(newTargetTypedefs);

        // target groupings
        Set<GroupingBuilder> newTargetGroupings = new HashSet<>();
        for (GroupingBuilder gb : old.getTargetGroupings()) {
            newTargetGroupings.add(copy(gb, newParent, true));
        }
        copy.getTargetGroupings().addAll(newTargetGroupings);

        // target unknown nodes
        Set<UnknownSchemaNodeBuilder> newTargetUnknownNodes = new HashSet<>();
        for (UnknownSchemaNodeBuilder unb : old.getTargetUnknownNodes()) {
            newTargetUnknownNodes.add(copy(unb, newParent, true));
        }
        copy.getTargetUnknownNodes().addAll(newTargetUnknownNodes);

        // add new uses to collection of uses in module
        ModuleBuilder module = ParserUtils.getParentModule(newParent);
        module.getAllUsesNodes().add(copy);

        return copy;
    }

    private static AugmentationSchemaBuilder copyAugment(AugmentationSchemaBuilder old, Builder newParent) {
        AugmentationSchemaBuilderImpl copy = new AugmentationSchemaBuilderImpl(newParent.getModuleName(),
                newParent.getLine(), old.getTargetPathAsString());
        copy.setParent(newParent);

        copy.setDescription(old.getDescription());
        copy.setReference(old.getReference());
        copy.setStatus(old.getStatus());
        copy.addWhenCondition(old.getWhenCondition());
        copy.setChildNodes(old.getChildNodes());
        for (DataSchemaNodeBuilder childNode : old.getChildNodeBuilders()) {
            copy.addChildNode(copy(childNode, copy, false));
        }
        for (UsesNodeBuilder oldUses : old.getUsesNodes()) {
            copy.addUsesNode(copyUses(oldUses, copy));
        }
        for (UnknownSchemaNodeBuilder un : old.getUnknownNodeBuilders()) {
            copy.addUnknownNodeBuilder((copy(un, copy, false)));
        }

        return copy;
    }

    static UnknownSchemaNodeBuilder copy(UnknownSchemaNodeBuilder old, Builder newParent, boolean updateQName) {
        DataBean data = getdata(old, newParent, updateQName);
        QName newQName = data.qname;
        SchemaPath newSchemaPath = data.schemaPath;

        UnknownSchemaNodeBuilder c = new UnknownSchemaNodeBuilder(newParent.getModuleName(), newParent.getLine(),
                newQName);

        c.setParent(newParent);
        c.setPath(newSchemaPath);
        c.setDescription(old.getDescription());
        c.setReference(old.getReference());
        c.setStatus(old.getStatus());
        c.setAddedByUses(old.isAddedByUses());
        for (UnknownSchemaNodeBuilder un : old.getUnknownNodeBuilders()) {
            c.addUnknownNodeBuilder((copy(un, c, updateQName)));
        }

        return c;
    }

    private static DataBean getdata(SchemaNodeBuilder old, Builder newParent, boolean updateQName) {
        List<QName> newPath = null;
        QName newQName = null;
        if (newParent instanceof ModuleBuilder) {
            ModuleBuilder parent = (ModuleBuilder) newParent;
            if (updateQName) {
                newQName = new QName(parent.getNamespace(), parent.getRevision(), parent.getPrefix(), old.getQName()
                        .getLocalName());
                newPath = Collections.singletonList(newQName);
            } else {
                newQName = old.getQName();
                newPath = Collections.singletonList(newQName);
            }
        } else if (newParent instanceof AugmentationSchemaBuilder) {
            AugmentationSchemaBuilder augment = (AugmentationSchemaBuilder) newParent;
            ModuleBuilder parent = ParserUtils.getParentModule(newParent);
            if (updateQName) {
                newQName = new QName(parent.getNamespace(), parent.getRevision(), parent.getPrefix(), old.getQName()
                        .getLocalName());
                newPath = new ArrayList<>(augment.getTargetPath().getPath());
                newPath.add(newQName);
            } else {
                newQName = old.getQName();
                newPath = new ArrayList<>(augment.getTargetPath().getPath());
                newPath.add(newQName);
            }

        } else if (newParent instanceof SchemaNodeBuilder) {
            SchemaNodeBuilder parent = (SchemaNodeBuilder) newParent;
            QName parentQName = parent.getQName();
            if (updateQName) {
                newQName = new QName(parentQName.getNamespace(), parentQName.getRevision(), parentQName.getPrefix(),
                        old.getQName().getLocalName());
                newPath = new ArrayList<>(parent.getPath().getPath());
                newPath.add(newQName);
            } else {
                newQName = old.getQName();
                newPath = new ArrayList<>(parent.getPath().getPath());
                newPath.add(newQName);
            }
        }

        SchemaPath newSchemaPath = new SchemaPath(newPath, true);
        return new DataBean(newQName, newSchemaPath);
    }

    private static final class DataBean {
        private QName qname;
        private SchemaPath schemaPath;

        private DataBean(QName qname, SchemaPath schemaPath) {
            this.qname = qname;
            this.schemaPath = schemaPath;
        }
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
        builder.setRanges(typedef.getRangeConstraints());
        builder.setLengths(typedef.getLengthConstraints());
        builder.setPatterns(typedef.getPatternConstraints());
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

}
