package org.opendaylight.yangtools.yang.model.export;

import com.google.common.annotations.Beta;
import com.google.common.primitives.UnsignedInteger;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.FeatureDefinition;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;

@Beta
@NotThreadSafe
class Emitter {

    private final YangModuleWriter writer;
    private final boolean emitInstantiated;
    private final boolean emitUses;


    Emitter(final YangModuleWriter writer) {
        this(writer,false,true);
    }

    Emitter(final YangModuleWriter writer, final boolean emitInstantiated, final boolean emitUses) {
        super();
        this.writer = writer;
        this.emitInstantiated = emitInstantiated;
        this.emitUses = emitUses;
    }

    void emitModule(final Module input) {
        writer.startModuleNode(input.getName());
        emitModuleHeader(input);
        emitLinkageNodes(input);
        emitMetaNodes(input);
        emitRevisionNodes(input);
        emitBodyNodes(input);
        writer.endNode();
    }

    private void emitModuleHeader(final Module input) {
        emitYangVersionNode(input.getYangVersion());
        emitNamespace(input.getNamespace());
        emitPrefixNode(input.getPrefix());
    }

    private void emitSubmodule(final String input) {
        // FIXME:writer.startSubmoduleNode(IdentifierHelper.getIdentifier(String
        // FIXME:input));
        //
        //
        // FIXME:submoduleHeaderNodes
        // FIXME:linkageNodes
        // FIXME:metaNodes
        // FIXME:revisionNodes
        // FIXME:bodyNodes
        // FIXME:writer.endNode();
    }

    private void emitSubmoduleHeaderNodes(final Module input) {
        // FIXME://FIXME:these Nodes can appear in any order
        // FIXME:yangVersionNode //FIXME:Optional
        // FIXME:belongsToNode
    }

    private void emitMetaNodes(final Module input) {
        // FIXME://FIXME:these Nodes can appear in any order
        emitOrganizationNode(input.getOrganization()); // FIXME:Optional
        emitContact(input.getContact()); // FIXME:Optional
        emitDescriptionNode(input.getDescription());
        emitReferenceNode(input.getReference());
    }

    private void emitLinkageNodes(final Module input) {
        for (final ModuleImport importNode : input.getImports()) {
            emitImport(importNode);
        }
        // FIXME:FIXME: includes(includeNode )

    }

    private void emitRevisionNodes(final Module input) {
        // FIXME:FIXME: emit revisions properly, when parsed model will provide
        // FIXME:enough information
        emitRevision(input.getRevision());

    }

    private void emitBodyNodes(final Module input) {

        for (final ExtensionDefinition extension : input.getExtensionSchemaNodes()) {
            emitExtension(extension);
        }
        for (final FeatureDefinition definition : input.getFeatures()) {
            emitFeature(definition);
        }
        for (final IdentitySchemaNode identity : input.getIdentities()) {
            emitIdentity(identity);
        }

        emitDataNodeContainer(input);

        for (final AugmentationSchema augmentation : input.getAugmentations()) {
            emitAugment(augmentation);
        }
        for (final RpcDefinition rpc : input.getRpcs()) {
            emitRpc(rpc);
        }
        for (final NotificationDefinition notification : input.getNotifications()) {
            emitNotificationNode(notification);
        }
        for (final Deviation deviation : input.getDeviations()) {
            emitDeviation(deviation);
        }

    }

    private void emitDataNodeContainer(final DataNodeContainer input) {
        for (final TypeDefinition<?> typedef : input.getTypeDefinitions()) {
            emitTypedef(typedef);
        }
        for (final GroupingDefinition grouping : input.getGroupings()) {
            emitGrouping(grouping);
        }
        for (final DataSchemaNode child : input.getChildNodes()) {
            emitDataSchemaNode(child);
        }
        for (final UsesNode usesNode : input.getUses()) {
            emitUses(usesNode);
        }
    }

    private void emitDataSchemaNode(final DataSchemaNode child) {
        if(!emitInstantiated && (child.isAddedByUses() || child.isAugmenting())) {
            // We skip instantiated nodes.
            return;
        }

        if (child instanceof ContainerSchemaNode) {
            emitContainer((ContainerSchemaNode) child);
        } else if (child instanceof LeafSchemaNode) {
            emitLeaf((LeafSchemaNode) child);
        } else if (child instanceof LeafListSchemaNode) {
            emitLeafList((LeafListSchemaNode) child);
        } else if (child instanceof ListSchemaNode) {
            emitList((ListSchemaNode) child);
        } else if (child instanceof ChoiceNode) {
            emitChoice((ChoiceNode) child);
        } else if (child instanceof AnyXmlSchemaNode) {
            emitAnyxml((AnyXmlSchemaNode) child);
        } else {
            throw new UnsupportedOperationException("Not supported DataSchemaNode type " + child.getClass());
        }
    }

    private void emitDataDef(final String input) {

        // FIXME:usesNode
    }

    private void emitYangVersionNode(final String input) {
        writer.startYangVersionNode(input);
        writer.endNode();
    }

    private void emitImport(final ModuleImport importNode) {
        writer.startImportNode(importNode.getModuleName());
        // input));
        //
        emitPrefixNode(importNode.getPrefix());
        emitRevisionDateNode(importNode.getRevision());

        writer.endNode();

    }

    private void emitInclude(final String input) {
        // FIXME:writer.startIncludeNode(IdentifierHelper.getIdentifier(String
        // FIXME:input));
        // FIXME:(";"
        //
        // FIXME:revisionDateNode //FIXME:Optional
        // FIXME:writer.endNode();)

    }

    private void emitNamespace(final URI uri) {
        writer.startNamespaceNode(uri);
        writer.endNode();

    }

    private void emitPrefixNode(final String input) {
        writer.startPrefixNode(input);
        writer.endNode();

    }

    private void emitBelongsTo(final String input) {
        // FIXME:writer.startBelongsToNode(IdentifierHelper.getIdentifier(String
        // FIXME:input));
        //
        //
        // FIXME:prefixNode
        // FIXME:writer.endNode();

    }

    private void emitOrganizationNode(final String input) {
        writer.startOrganizationNode(input);
        writer.endNode();

    }

    private void emitContact(final String input) {
        writer.startContactNode(input);
        writer.endNode();

    }

    private void emitDescriptionNode(@Nullable final String input) {
        if(input != null) {
            writer.startDescriptionNode(input);
            writer.endNode();
        }
    }

    private void emitReferenceNode(@Nullable final String input) {
        if(input != null) {
            writer.startReferenceNode(input);
            writer.endNode();
        }
    }

    private void emitUnitsNode(@Nullable final String input) {
        if(input != null) {
            writer.startUnitsNode(input);
            writer.endNode();
        }
    }

    private void emitRevision(final Date date) {
        writer.startRevisionNode(date);
        // FIXME:(";"
        //
        // FIXME:FIXME:BUG-2417: descriptionNode //FIXME:Optional
        // FIXME:FIXME:BUG-2417: referenceNode //FIXME:Optional
        writer.endNode();

    }

    private void emitRevisionDateNode(@Nullable final Date date) {
        if(date != null) {
            writer.startRevisionDateNode(date);
            writer.endNode();
        }
    }

    private void emitExtension(final ExtensionDefinition extension) {
        writer.startExtensionNode(extension.getQName());
        emitArgument(extension.getArgument());
        emitStatusNode(extension.getStatus());
        emitDescriptionNode(extension.getDescription());
        emitReferenceNode(extension.getReference());
        writer.endNode();

    }

    private void emitArgument(final @Nullable String input) {
        if(input != null) {
        writer.startArgumentNode(input);
        // FIXME:FIXME: emit YIN element
        writer.endNode();
        }

    }

    private void emitYinElement(final String input) {
        // FIXME:writer.startYinElementNode(yinElementArgStr));
        // FIXME:Nodeend

    }

    private void emitIdentity(final IdentitySchemaNode identity) {
        writer.startIdentityNode(identity.getQName());
        if(identity.getBaseIdentity() != null) {
        emitBase(identity.getBaseIdentity().getQName());
        }
        emitStatusNode(identity.getStatus());
        emitDescriptionNode(identity.getDescription());
        emitReferenceNode(identity.getReference());
        writer.endNode();

    }

    private void emitBase(final QName qName) {
        writer.startBaseNode(qName);
        writer.endNode();

    }

    private void emitFeature(final FeatureDefinition definition) {
        writer.startFeatureNode(definition.getQName());

        // FIXME:FIXME: Expose ifFeature *(ifFeatureNode )
        emitStatusNode(definition.getStatus());
        emitDescriptionNode(definition.getDescription());
        emitReferenceNode(definition.getReference());
        writer.endNode();

    }

    private void emitIfFeature(final String input) {
        // FIXME:writer.startIfFeatureNode(identifierRefArgStr));
        // FIXME:Nodeend

    }

    private void emitTypedef(final TypeDefinition<?> typedef) {
        writer.startTypedefNode(typedef.getQName());
        // FIXME:FIXME: Differentiate between derived type and existing type
        // name.
        emitTypeNodeDerived(typedef);
        emitUnitsNode(typedef.getUnits());
        emitDefault(typedef.getDefaultValue());
        emitStatusNode(typedef.getStatus());
        emitDescriptionNode(typedef.getDescription());
        emitReferenceNode(typedef.getReference());
        writer.endNode();

    }

    private void emitTypeNode(final SchemaPath parentPath, final TypeDefinition<?> subtype) {
        final SchemaPath path = subtype.getPath();
        if(IterablesUtils.isPrefix(parentPath.getPathFromRoot(),path.getPathFromRoot())) {
            emitTypeNodeDerived(subtype);
        } else {
            emitTypeNodeReferenced(subtype);
        }
    }

    private void emitType(final TypeDefinition<?> typeDefinition) {
        writer.startTypeNode(typeDefinition.getQName());
        emitTypeBodyNodes(typeDefinition);
        writer.endNode();

    }

    private void emitTypeNodeReferenced(final TypeDefinition<?> typeDefinition) {
        writer.startTypeNode(typeDefinition.getQName());
        //emitTypeBodyNodes(typeDefinition);
        writer.endNode();

    }

    private void emitTypeNodeDerived(final TypeDefinition<?> typeDefinition) {
        final TypeDefinition<?> baseType;
        if(typeDefinition.getBaseType() != null) {
            baseType = typeDefinition.getBaseType();
        } else {
            baseType = typeDefinition;
        }
        writer.startTypeNode(baseType.getQName());
        emitTypeBodyNodes(typeDefinition);
        writer.endNode();

    }

    private void emitTypeBodyNodes(final TypeDefinition<?> typeDef) {
        if (typeDef instanceof ExtendedType) {
            emitTypeBodyNodes(NormalizatedDerivedType.from((ExtendedType) typeDef));
        } else if (typeDef instanceof UnsignedIntegerTypeDefinition) {
            // FIXME: Unsigned Type definition
        } else if (typeDef instanceof IntegerTypeDefinition) {
            // FIXME:numericalRestrictions
        } else if (typeDef instanceof DecimalTypeDefinition) {
            emitDecimal64Specification((DecimalTypeDefinition) typeDef);
        } else if (typeDef instanceof StringTypeDefinition) {
            emitStringRestrictions((StringTypeDefinition) typeDef);
        } else if (typeDef instanceof EnumTypeDefinition) {
            emitEnumSpecification((EnumTypeDefinition) typeDef);
        } else if (typeDef instanceof LeafrefTypeDefinition) {
            emitLeafrefSpecification((LeafrefTypeDefinition) typeDef);
        } else if (typeDef instanceof IdentityrefTypeDefinition) {
            emitIdentityrefSpecification((IdentityrefTypeDefinition) typeDef);
        } else if (typeDef instanceof InstanceIdentifierTypeDefinition) {
            emitInstanceIdentifierSpecification((InstanceIdentifierTypeDefinition) typeDef);
        } else if (typeDef instanceof BitsTypeDefinition) {
            emitBitsSpecification((BitsTypeDefinition) typeDef);
        } else if (typeDef instanceof UnionTypeDefinition) {
            emitUnionSpecification((UnionTypeDefinition) typeDef);
        } else if (typeDef instanceof BinaryTypeDefinition) {
            // FIXME:FIXME: Is this realy NOOP?
        } else if (typeDef instanceof BooleanTypeDefinition) {
            // NOOP ?
        } else if (typeDef instanceof EmptyTypeDefinition) {
            // NOOP
        } else {
            throw new IllegalArgumentException("Not supported type " + typeDef.getClass());
        }
    }

    private void emitNumericalRestrictions(final String input) {
        // FIXME:rangeNode

    }

    private void emitRange(final String input) {
        // FIXME:writer.startRangeNode(rangeArgStr));
        // FIXME:(";"
        //
        // FIXME://FIXME:these Nodes can appear in any order
        // FIXME:errorMessageNode //FIXME:Optional
        // FIXME:errorAppTagNode //FIXME:Optional
        // FIXME:descriptionNode //FIXME:Optional
        // FIXME:referenceNode //FIXME:Optional
        // FIXME:writer.endNode();)

    }

    private void emitDecimal64Specification(final DecimalTypeDefinition typeDefinition) {

        // FIXME:fractionDigitsNode
        // FIXME:rangeNode //FIXME:Optional

    }

    private void emitStringRestrictions(final StringTypeDefinition typeDef) {
        // FIXME://FIXME:these Nodes can appear in any order
        // FIXME:lengthNode //FIXME:Optional

        for (final PatternConstraint pattern : typeDef.getPatternConstraints()) {
            emitPatternNode(pattern);
        }
        // FIXME:*(patternNode )

    }

    private void emitLength(final String input) {
        // FIXME:writer.startLengthNode(lengthArgStr));
        // FIXME:(";"
        //
        // FIXME://FIXME:these Nodes can appear in any order
        // FIXME:errorMessageNode //FIXME:Optional
        // FIXME:errorAppTagNode //FIXME:Optional
        // FIXME:descriptionNode //FIXME:Optional
        // FIXME:referenceNode //FIXME:Optional
        // FIXME:writer.endNode();)

    }

    private void emitPatternNode(final PatternConstraint pattern) {
        writer.startPatternNode(pattern.getRegularExpression());
        emitErrorMessageNode(pattern.getErrorMessage()); // FIXME:Optional
        emitErrorAppTagNode(pattern.getErrorAppTag()); // FIXME:Optional
        emitDescriptionNode(pattern.getDescription());
        emitReferenceNode(pattern.getReference()); // FIXME:Optional
        writer.endNode();

    }

    private void emitDefault(@Nullable final Object object) {
        if(object != null) {
            writer.startDefaultNode(object.toString());
            writer.endNode();
        }

    }

    private void emitEnumSpecification(final EnumTypeDefinition typeDefinition) {

        for (final EnumPair enumValue : typeDefinition.getValues()) {
            emitEnumNode(enumValue);
        }
        // FIXME:1*(enumNode )

    }

    private void emitEnumNode(final EnumPair enumValue) {
        writer.startEnumNode(enumValue.getName());
        emitValueNode(enumValue.getValue()); // FIXME:Optional
        emitStatusNode(enumValue.getStatus());
        emitDescriptionNode(enumValue.getDescription());
        emitReferenceNode(enumValue.getReference()); // FIXME:Optional
        writer.endNode();
    }

    //
    // FIXME:leafrefSpecification =
    // FIXME://FIXME:these Nodes can appear in any order
    // FIXME:pathNode
    // FIXME:requireInstanceNode //FIXME:Optional
    // FIXME:REPLACED with (RFC6020 - Errata ID 2949)

    private void emitLeafrefSpecification(final LeafrefTypeDefinition typeDefinition) {
        // FIXME: Path must no t
        emitPathNode(typeDefinition.getPathStatement());
        // FIXME:pathNode

    }

    private void emitPathNode(final RevisionAwareXPath revisionAwareXPath) {
        writer.startPathNode(revisionAwareXPath);
        writer.endNode();

    }

    private void emitRequireInstanceNode(final boolean require) {
        writer.startRequireInstanceNode(require);
        writer.endNode();

    }

    private void emitInstanceIdentifierSpecification(final InstanceIdentifierTypeDefinition typeDefinition) {
        emitRequireInstanceNode(typeDefinition.requireInstance());
    }

    private void emitIdentityrefSpecification(final IdentityrefTypeDefinition typeDefinition) {
        emitBase(typeDefinition.getQName());

    }

    private void emitUnionSpecification(final UnionTypeDefinition typeDefinition) {
        for (final TypeDefinition<?> subtype : typeDefinition.getTypes()) {
            // FIXME: What if we have locally modified types here?
            // is solution to look-up in schema path?
            emitTypeNode(typeDefinition.getPath(),subtype);
        }
    }

    private void emitBitsSpecification(final BitsTypeDefinition typeDefinition) {
        for (final Bit bit : typeDefinition.getBits()) {
            emitBit(bit);
        }

    }

    private void emitBit(final Bit bit) {
        writer.startBitNode(bit.getName());
        // FIXME:(";"
        //
        // FIXME://FIXME:these Nodes can appear in any order
        emitPositionNode(bit.getPosition()); // FIXME:Optional
        emitStatusNode(bit.getStatus());
        emitDescriptionNode(bit.getDescription());
        emitReferenceNode(bit.getReference()); // FIXME:Optional
        writer.endNode();

    }

    private void emitPositionNode(@Nullable final Long position) {
        if(position != null) {
            writer.startPositionNode(UnsignedInteger.valueOf(position));
            writer.endNode();
        }
    }

    private void emitStatusNode(@Nullable final Status status) {
        if (status != null) {
            writer.startStatusNode(status);
            writer.endNode();
        }
    }

    private void emitConfig(final String input) {
        // FIXME:writer.startConfigNode());
        // FIXME:configArgStr Nodeend

    }

    private void emitConfigArgStr(final String input) {
        // FIXME:< a string that matches the rule
        // FIXME:configArg >

    }

    private void emitConfigArg(final String input) {
        // FIXME:writer.startTrueNode());/ writer.startFalseNode());

    }

    private void emitMandatory(final String input) {
        // FIXME:writer.startMandatoryNode());
        // FIXME:mandatoryArgStr Nodeend

    }

    private void emitMandatoryArgStr(final String input) {
        // FIXME:< a string that matches the rule
        // FIXME:mandatoryArg >

    }

    private void emitMandatoryArg(final String input) {
        // FIXME:writer.startTrueNode());/ writer.startFalseNode());

    }

    private void emitPresence(final String input) {
        // FIXME:writer.startPresenceNode(string)); Nodeend

    }

    private void emitOrderedBy(final String input) {
        // FIXME:writer.startOrderedByNode());
        // FIXME:orderedByArgStr Nodeend

    }

    private void emitOrderedByArgStr(final String input) {
        // FIXME:< a string that matches the rule
        // FIXME:orderedByArg >

    }

    private void emitOrderedByArg(final String input) {
        // FIXME:writer.startUserNode());/ writer.startSystemNode());

    }

    private void emitMust(final MustDefinition mustCondition) {
        writer.startMustNode(mustCondition.getXpath());
        emitErrorMessageNode(mustCondition.getErrorMessage()); // FIXME:Optional
        emitErrorAppTagNode(mustCondition.getErrorAppTag()); // FIXME:Optional
        emitDescriptionNode(mustCondition.getDescription());
        emitReferenceNode(mustCondition.getReference()); // FIXME:Optional
        writer.endNode();

    }

    private void emitErrorMessageNode(@Nullable final String input) {
        if(input != null && !input.isEmpty()) {
            writer.startErrorMessageNode(input);
            writer.endNode();
        }

    }

    private void emitErrorAppTagNode(final String input) {
        if(input != null && !input.isEmpty()) {
            writer.startErrorAppTagNode(input);
            writer.endNode();
        }
    }

    private void emitMinElementsNode(final String input) {
        // FIXME:writer.startMinElementsNode());
        // FIXME:minValueArgStr Nodeend

    }

    private void emitMinValueArgStr(final String input) {
        // FIXME:< a string that matches the rule
        // FIXME:minValueArg >

    }

    private void emitMinValueArg(final String input) {
        // FIXME:nonNegativeIntegerValue

    }

    private void emitMaxElementsNode(final String input) {
        // FIXME:writer.startMaxElementsNode());
        // FIXME:maxValueArgStr Nodeend

    }

    private void emitMaxValueArgStr(final String input) {
        // FIXME:< a string that matches the rule
        // FIXME:maxValueArg >

    }

    private void emitMaxValueArg(final String input) {
        // FIXME:writer.startUnboundedNode());
        // FIXME:positiveIntegerValue

    }

    private void emitValueNode(final Integer integer) {
        writer.startValueNode(integer);
        writer.endNode();
    }

    private void emitDocumentedNode(final DocumentedNode input) {
        emitStatusNode(input.getStatus());
        emitDescriptionNode(input.getDescription());
        emitReferenceNode(input.getReference());
    }

    private void emitGrouping(final GroupingDefinition grouping) {
        writer.startGroupingNode(grouping.getQName());
        emitDocumentedNode(grouping);
        emitDataNodeContainer(grouping);
        writer.endNode();

    }

    private void emitContainer(final ContainerSchemaNode child) {
        writer.startContainerNode(child.getQName());
        // FIXME:(";"
        //
        // FIXME://FIXME:these Nodes can appear in any order
        emitConstraints(child.getConstraints());
        // FIXME:whenNode //FIXME:Optional
        // FIXME:*(ifFeatureNode )
        // FIXME:*(mustNode )
        // FIXME:presenceNode //FIXME:Optional
        // FIXME:configNode //FIXME:Optional
        emitDocumentedNode(child);
        emitDataNodeContainer(child);
        writer.endNode();

    }

    private void emitConstraints(final ConstraintDefinition constraints) {
        emitWhen(constraints.getWhenCondition());

        for (final MustDefinition mustCondition : constraints.getMustConstraints()) {
            emitMust(mustCondition);
        }

    }

    private void emitLeaf(final LeafSchemaNode child) {
        writer.startLeafNode(child.getQName());
        //
        // FIXME://FIXME:these Nodes can appear in any order
        // FIXME:whenNode //FIXME:Optional
        // FIXME:*(ifFeatureNode )
        emitType(child.getType());
        // FIXME:unitsNode //FIXME:Optional
        // FIXME:*(mustNode )
        // FIXME:defaultNode //FIXME:Optional
        // FIXME:configNode //FIXME:Optional
        // FIXME:mandatoryNode //FIXME:Optional
        emitDocumentedNode(child);
        writer.endNode();

    }

    private void emitLeafList(final LeafListSchemaNode child) {
        writer.startLeafListNode(child.getQName());
        //
        // FIXME://FIXME:these Nodes can appear in any order
        // FIXME:whenNode //FIXME:Optional
        // FIXME:*(ifFeatureNode )
        // FIXME:typeNode
        // FIXME:unitsNode //FIXME:Optional
        // FIXME:*(mustNode )
        // FIXME:configNode //FIXME:Optional
        // FIXME:minElementsNode //FIXME:Optional
        // FIXME:maxElementsNode //FIXME:Optional
        // FIXME:orderedByNode //FIXME:Optional
        emitDocumentedNode(child);
        writer.endNode();

    }

    private void emitList(final ListSchemaNode child) {
        writer.startListNode(child.getQName());
        //
        // FIXME://FIXME:these Nodes can appear in any order
        // FIXME:whenNode //FIXME:Optional
        // FIXME:*(ifFeatureNode )
        // FIXME:*(mustNode )
        emitKey(child.getKeyDefinition());
        // FIXME:keyNode //FIXME:Optional
        // FIXME:*(uniqueNode )
        // FIXME:configNode //FIXME:Optional
        // FIXME:minElementsNode //FIXME:Optional
        // FIXME:maxElementsNode //FIXME:Optional
        // FIXME:orderedByNode //FIXME:Optional
        emitDocumentedNode(child);
        emitDataNodeContainer(child);
        writer.endNode();

    }

    private void emitKey(final List<QName> keyList) {
        writer.startKeyNode(keyList);
        writer.endNode();
    }

    private void emitUnique(final String input) {
        // FIXME:writer.startUniqueNode(uniqueArgStr)); Nodeend

    }

    private void emitChoice(final ChoiceNode choice) {
        writer.startChoiceNode(choice.getQName());
        // FIXME:(";"
        //
        // FIXME://FIXME:these Nodes can appear in any order
        // FIXME:whenNode //FIXME:Optional
        // FIXME:*(ifFeatureNode )
        // FIXME:defaultNode //FIXME:Optional
        // FIXME:configNode //FIXME:Optional
        // FIXME:mandatoryNode //FIXME:Optional
        emitDocumentedNode(choice);
        for (final ChoiceCaseNode caze : choice.getCases()) {
            // FIXME: emit short case
            emitCase(caze);
        }
        writer.endNode();

    }

    private void emitShortCase(final String input) {
        // FIXME:containerNode
        // FIXME:leafNode
        // FIXME:leafListNode
        // FIXME:listNode
        // FIXME:anyxmlNode

    }

    private void emitCase(final ChoiceCaseNode caze) {
        if(!emitInstantiated && caze.isAugmenting()) {
            return;
        }

        writer.startCaseNode(caze.getQName());
        // FIXME:(";"
        //
        // FIXME://FIXME:these Nodes can appear in any order
        // FIXME:whenNode //FIXME:Optional
        // FIXME:*(ifFeatureNode )
        emitDataNodeContainer(caze);
        // FIXME:statusNode //FIXME:Optional
        // FIXME:descriptionNode //FIXME:Optional
        // FIXME:referenceNode //FIXME:Optional
        // FIXME:*(dataDefNode )
        writer.endNode();

    }

    private void emitAnyxml(final AnyXmlSchemaNode child) {
        // FIXME:writer.startAnyxmlNode(IdentifierHelper.getIdentifier(String
        // input));
        // FIXME:(";"
        //
        // FIXME://FIXME:these Nodes can appear in any order
        // FIXME:whenNode //FIXME:Optional
        // FIXME:*(ifFeatureNode )
        // FIXME:*(mustNode )
        // FIXME:configNode //FIXME:Optional
        // FIXME:mandatoryNode //FIXME:Optional
        // FIXME:emitDocumentedNode(child);
        // FIXME:writer.endNode();)

    }

    private void emitUses(final UsesNode usesNode) {
        writer.startUsesNode(usesNode.getGroupingPath().getLastComponent());
        // FIXME:whenNode //FIXME:Optional
        // FIXME:*(ifFeatureNode )
        // FIXME:statusNode //FIXME:Optional

        // FIXME:descriptionNode //FIXME:Optional
        // FIXME:referenceNode //FIXME:Optional
        for(final Entry<SchemaPath, SchemaNode> refine : usesNode.getRefines().entrySet()) {
            emitRefine(refine);
        }
        for(final AugmentationSchema aug : usesNode.getAugmentations()) {
            emitUsesAugmentNode(aug);
        }
        // FIXME:*(refineNode )
        // FIXME:*(usesAugmentNode )

        writer.endNode();

    }

    private void emitRefine(final Entry<SchemaPath, SchemaNode> refine) {
        // FIXME:writer.startRefineNode(refineArgStr));
        // FIXME:(";"
        //
        // FIXME:(refineContainerNodes
        // FIXME:refineLeafNodes
        // FIXME:refineLeafListNodes
        // FIXME:refineListNodes
        // FIXME:refineChoiceNodes
        // FIXME:refineCaseNodes
        // FIXME:refineAnyxmlNodes)
        // FIXME:writer.endNode();)

    }

    private void emitRefineArgStr(final String input) {
        // FIXME:< a string that matches the rule
        // FIXME:refineArg >

    }

    private void emitRefineArg(final String input) {
        // FIXME:descendantSchemaNodeid

    }

    private void emitRefineContainerNodes(final String input) {
        // FIXME://FIXME:these Nodes can appear in any order
        // FIXME:*(mustNode )
        // FIXME:presenceNode //FIXME:Optional
        // FIXME:configNode //FIXME:Optional
        // FIXME:descriptionNode //FIXME:Optional
        // FIXME:referenceNode //FIXME:Optional

    }

    private void emitRefineLeafNodes(final String input) {
        // FIXME://FIXME:these Nodes can appear in any order
        // FIXME:*(mustNode )
        // FIXME:defaultNode //FIXME:Optional
        // FIXME:configNode //FIXME:Optional
        // FIXME:mandatoryNode //FIXME:Optional
        // FIXME:descriptionNode //FIXME:Optional
        // FIXME:referenceNode //FIXME:Optional

    }

    private void emitRefineLeafListNodes(final String input) {
        // FIXME://FIXME:these Nodes can appear in any order
        // FIXME:*(mustNode )
        // FIXME:configNode //FIXME:Optional
        // FIXME:minElementsNode //FIXME:Optional
        // FIXME:maxElementsNode //FIXME:Optional
        // FIXME:descriptionNode //FIXME:Optional
        // FIXME:referenceNode //FIXME:Optional

    }

    private void emitRefineListNodes(final String input) {
        // FIXME://FIXME:these Nodes can appear in any order
        // FIXME:*(mustNode )
        // FIXME:configNode //FIXME:Optional
        // FIXME:minElementsNode //FIXME:Optional
        // FIXME:maxElementsNode //FIXME:Optional
        // FIXME:descriptionNode //FIXME:Optional
        // FIXME:referenceNode //FIXME:Optional

    }

    private void emitRefineChoiceNodes(final String input) {
        // FIXME://FIXME:these Nodes can appear in any order
        // FIXME:defaultNode //FIXME:Optional
        // FIXME:configNode //FIXME:Optional
        // FIXME:mandatoryNode //FIXME:Optional
        // FIXME:descriptionNode //FIXME:Optional
        // FIXME:referenceNode //FIXME:Optional

    }

    private void emitRefineCaseNodes(final String input) {
        // FIXME://FIXME:these Nodes can appear in any order
        // FIXME:descriptionNode //FIXME:Optional
        // FIXME:referenceNode //FIXME:Optional

    }

    private void emitRefineAnyxmlNodes(final String input) {
        // FIXME://FIXME:these Nodes can appear in any order
        // FIXME:*(mustNode )
        // FIXME:configNode //FIXME:Optional
        // FIXME:mandatoryNode //FIXME:Optional
        // FIXME:descriptionNode //FIXME:Optional
        // FIXME:referenceNode //FIXME:Optional

    }

    private void emitUsesAugmentNode(final AugmentationSchema aug) {
        // FIXME:writer.startAugmentNode(usesAugmentArgStr));
        //
        // FIXME://FIXME:these Nodes can appear in any order
        // FIXME:whenNode //FIXME:Optional
        // FIXME:*(ifFeatureNode )
        // FIXME:statusNode //FIXME:Optional
        // FIXME:descriptionNode //FIXME:Optional
        // FIXME:referenceNode //FIXME:Optional
        // FIXME:1*((dataDefNode )
        // FIXME:(caseNode ))
        // FIXME:writer.endNode();

    }

    private void emitUsesAugmentArgStr(final String input) {
        // FIXME:< a string that matches the rule
        // FIXME:usesAugmentArg >

    }

    private void emitUsesAugmentArg(final String input) {
        // FIXME:descendantSchemaNodeid

    }

    private void emitAugment(final AugmentationSchema augmentation) {
        // FIXME:writer.startAugmentNode(augmentArgStr));
        //
        // FIXME://FIXME:these Nodes can appear in any order
        // FIXME:whenNode //FIXME:Optional
        // FIXME:*(ifFeatureNode )
        // FIXME:statusNode //FIXME:Optional
        // FIXME:descriptionNode //FIXME:Optional
        // FIXME:referenceNode //FIXME:Optional
        // FIXME:1*((dataDefNode )
        // FIXME:(caseNode ))
        // FIXME:writer.endNode();
    }

    private void emitAugmentArgStr(final String input) {
        // FIXME:< a string that matches the rule
        // FIXME:augmentArg >

    }

    private void emitAugmentArg(final String input) {
        // FIXME:absoluteSchemaNodeid

    }

    private void emitUnknownStatement(final String input) {
        // FIXME:prefix ":" identifier [ string]
        // FIXME:(";" / *unknownStatement2 writer.endNode();)

    }

    private void emitUnknownStatement2(final String input) {
        // FIXME:[prefix ":"] identifier [ string]
        // FIXME:(";" / *unknownStatement2 writer.endNode();)

    }

    private void emitWhen(final RevisionAwareXPath revisionAwareXPath) {
        // FIXME:writer.startWhenNode(string));
        // FIXME:(";"
        //
        // FIXME://FIXME:these Nodes can appear in any order
        // FIXME:descriptionNode //FIXME:Optional
        // FIXME:referenceNode //FIXME:Optional
        // FIXME:writer.endNode();)

    }

    private void emitRpc(final RpcDefinition rpc) {
        writer.startRpcNode(rpc.getQName());
        // FIXME:*(ifFeatureNode )
        // FIXME:statusNode //FIXME:Optional

        // FIXME:descriptionNode //FIXME:Optional
        // FIXME:referenceNode //FIXME:Optional
        // FIXME:*((typedefNode
        // FIXME:groupingNode) )
        emitInput(rpc.getInput());
        emitOutput(rpc.getOutput());
        writer.endNode();

    }

    private void emitInput(@Nullable final ContainerSchemaNode input) {
        if(input != null) {
            writer.startInputNode();
            emitDataNodeContainer(input);
            writer.endNode();
        }

    }

    private void emitOutput(@Nullable final ContainerSchemaNode input) {
        if(input != null) {
            writer.startOutputNode();
            emitDataNodeContainer(input);
            writer.endNode();
        }

    }

    private void emitNotificationNode(final NotificationDefinition notification) {
        writer.startNotificationNode(notification.getQName());
        // FIXME:*(ifFeatureNode )
        emitDocumentedNode(notification);
        emitDataNodeContainer(notification);
        writer.endNode();

    }

    private void emitDeviation(final Deviation deviation) {
        // FIXME:writer.startDeviationNode());
        // FIXME:deviationArgStr
        //
        // FIXME://FIXME:these Nodes can appear in any order
        // FIXME:descriptionNode //FIXME:Optional
        // FIXME:referenceNode //FIXME:Optional
        // FIXME:(deviateNotSupportedNode
        // FIXME:1*(deviateAddNode
        // FIXME:deviateReplaceNode
        // FIXME:deviateDeleteNode))
        // FIXME:writer.endNode();

    }

    private void emitDeviateNotSupportedNode(final String input) {
        // FIXME:writer.startDeviateNode());
        // FIXME:writer.startNotSupportedNode());
        // FIXME:(";"
        //
        // FIXME:writer.endNode();)

    }

    private void emitDeviateAdd(final String input) {
        // FIXME:writer.startDeviateNode(addKeyword));
        // FIXME:(";"
        //
        // FIXME:unitsNode //FIXME:Optional
        // FIXME:*(mustNode )
        // FIXME:*(uniqueNode )
        // FIXME:defaultNode //FIXME:Optional
        // FIXME:configNode //FIXME:Optional
        // FIXME:mandatoryNode //FIXME:Optional
        // FIXME:minElementsNode //FIXME:Optional
        // FIXME:maxElementsNode //FIXME:Optional
        // FIXME:writer.endNode();)

    }

    private void emitDeviateDeleteNode(final String input) {
        // FIXME:writer.startDeviateNode(deleteKeyword));
        // FIXME:(";"
        //
        // FIXME:unitsNode //FIXME:Optional
        // FIXME:*(mustNode )
        // FIXME:*(uniqueNode )
        // FIXME:defaultNode //FIXME:Optional
        // FIXME:writer.endNode();)

    }

    private void emitDeviateReplaceNode(final String input) {
        // FIXME:writer.startDeviateNode(replaceKeyword));
        // FIXME:(";"
        //
        // FIXME:typeNode //FIXME:Optional
        // FIXME:unitsNode //FIXME:Optional
        // FIXME:defaultNode //FIXME:Optional
        // FIXME:configNode //FIXME:Optional
        // FIXME:mandatoryNode //FIXME:Optional
        // FIXME:minElementsNode //FIXME:Optional
        // FIXME:maxElementsNode //FIXME:Optional
        // FIXME:writer.endNode();)

        // FIXME:Ranges

    }

    private void emitRangeArgStr(final String input) {
        // FIXME:< a string that matches the rule
        // FIXME:rangeArg >

    }

    private void emitRangeArg(final String input) {
        // FIXME:rangePart *( "|" rangePart)

    }

    private void emitRangePart(final String input) {
        // FIXME:rangeBoundary
        // FIXME:[ ".." rangeBoundary]

    }

    private void emitRangeBoundary(final String input) {
        // FIXME:writer.startMinNode());/ writer.startMaxNode());
        // FIXME:integerValue / decimalValue

        // FIXME:Lengths

    }

    private void emitLengthArgStr(final String input) {
        // FIXME:< a string that matches the rule
        // FIXME:lengthArg >

    }

    private void emitLengthArg(final String input) {
        // FIXME:lengthPart *( "|" lengthPart)

    }

    private void emitLengthPart(final String input) {
        // FIXME:lengthBoundary
        // FIXME:[ ".." lengthBoundary]

    }

    private void emitLengthBoundary(final String input) {
        // FIXME:writer.startMinNode());/ writer.startMaxNode());
        // FIXME:nonNegativeIntegerValue

        // FIXME:Date

    }

}
