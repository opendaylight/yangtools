/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl

import java.util.Collection
import java.util.Date
import java.util.List
import java.util.Map
import java.util.Set
import java.util.StringTokenizer
import org.opendaylight.yangtools.yang.common.QName
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode
import org.opendaylight.yangtools.yang.model.api.ChoiceNode
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode
import org.opendaylight.yangtools.yang.model.api.Deviation
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition
import org.opendaylight.yangtools.yang.model.api.FeatureDefinition
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode
import org.opendaylight.yangtools.yang.model.api.Module
import org.opendaylight.yangtools.yang.model.api.ModuleImport
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition
import org.opendaylight.yangtools.yang.model.api.RpcDefinition
import org.opendaylight.yangtools.yang.model.api.SchemaNode
import org.opendaylight.yangtools.yang.model.api.SchemaPath
import org.opendaylight.yangtools.yang.model.api.TypeDefinition
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode
import org.opendaylight.yangtools.yang.model.api.UsesNode
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil
import com.google.common.base.CharMatcher

class YangTemplate {

    // FIXME: this is not thread-safe and seems to be unused!
    private static var Module module = null

    private static val CharMatcher NEWLINE_OR_TAB = CharMatcher.anyOf("\n\t")

    def static String generateYangSnipet(SchemaNode schemaNode) {
        if (schemaNode == null)
            return ''

        '''
            «IF schemaNode instanceof DataSchemaNode»
            «writeDataSchemaNode(schemaNode as DataSchemaNode)»
            «ENDIF»
            «IF schemaNode instanceof EnumTypeDefinition.EnumPair»
            «writeEnumPair(schemaNode as EnumTypeDefinition.EnumPair)»
            «ENDIF»
            «IF schemaNode instanceof ExtensionDefinition»
            «writeExtension(schemaNode as ExtensionDefinition)»
            «ENDIF»
            «IF schemaNode instanceof FeatureDefinition»
            «writeFeature(schemaNode as FeatureDefinition)»
            «ENDIF»
            «IF schemaNode instanceof GroupingDefinition»
            «writeGroupingDef(schemaNode as GroupingDefinition)»
            «ENDIF»
            «IF schemaNode instanceof IdentitySchemaNode»
            «writeIdentity(schemaNode as IdentitySchemaNode)»
            «ENDIF»
            «IF schemaNode instanceof NotificationDefinition»
            «writeNotification(schemaNode as NotificationDefinition)»
            «ENDIF»
            «IF schemaNode instanceof RpcDefinition»
            «writeRPC(schemaNode as RpcDefinition)»
            «ENDIF»
            «IF schemaNode instanceof TypeDefinition<?>»
            «writeTypeDefinition(schemaNode as TypeDefinition<?>)»
            «ENDIF»
            «IF schemaNode instanceof UnknownSchemaNode»
            «writeUnknownSchemaNode(schemaNode as UnknownSchemaNode)»
            «ENDIF»
        '''
    }
    
    def static String generateYangSnipet(Set<? extends SchemaNode> nodes) {
        if (nodes.nullOrEmpty)
            return ''
        
        '''
            «FOR node : nodes»
                «IF node instanceof NotificationDefinition»
                «writeNotification(node as NotificationDefinition)»
                «ELSEIF node instanceof RpcDefinition»
                «writeRPC(node as RpcDefinition)»
                «ENDIF»
            «ENDFOR»
        '''
    }

    def static writeEnumPair(EnumPair pair) {
        var boolean hasEnumPairValue = pair.value != null
        '''
            enum «pair.name»«IF !hasEnumPairValue»;«ELSE»{
                value «pair.value»;
            }
            «ENDIF»
        '''
    }

    def static String writeModuleImports(Set<ModuleImport> moduleImports) {
        if (moduleImports.nullOrEmpty)
            return ''

        '''
            «FOR moduleImport : moduleImports SEPARATOR "\n"»
                «IF moduleImport != null && !moduleImport.moduleName.nullOrEmpty»
                import «moduleImport.moduleName» { prefix "«moduleImport.prefix»"; }
                «ENDIF»
            «ENDFOR»
        '''
    }

    def static writeRevision(Date moduleRevision, String moduleDescription) {
        val revisionIndent = 12

        '''
            revision «SimpleDateFormatUtil.getRevisionFormat.format(moduleRevision)» {
                description "«formatToParagraph(moduleDescription, revisionIndent)»";
            }
        '''
    }

    def static String generateYangSnipet(Module module) {

        '''
            module «module.name» {
                yang-version «module.yangVersion»;
                namespace "«module.QNameModule.namespace.toString»";
                prefix "«module.prefix»";

                «IF !module.imports.nullOrEmpty»
                «writeModuleImports(module.imports)»
                «ENDIF»
                «IF module.revision != null»
                «writeRevision(module.revision, module.description)»
                «ENDIF»
                «IF !module.childNodes.nullOrEmpty»

                «writeDataSchemaNodes(module.childNodes)»
                «ENDIF»
                «IF !module.groupings.nullOrEmpty»

                «writeGroupingDefs(module.groupings)»
                «ENDIF»
                «IF !module.augmentations.nullOrEmpty»

                «writeAugments(module.augmentations)»
                «ENDIF»
                «IF !module.deviations.nullOrEmpty»

                «writeDeviations(module.deviations)»
                «ENDIF»
                «IF !module.extensionSchemaNodes.nullOrEmpty»

                «writeExtensions(module.extensionSchemaNodes)»
                «ENDIF»
                «IF !module.features.nullOrEmpty»

                «writeFeatures(module.features)»
                «ENDIF»
                «IF !module.identities.nullOrEmpty»

                «writeIdentities(module.identities)»
                «ENDIF»
                «IF !module.notifications.nullOrEmpty»

                «writeNotifications(module.notifications)»
                «ENDIF»
                «IF !module.rpcs.nullOrEmpty»

                «writeRPCs(module.rpcs)»
                «ENDIF»
                «IF !module.unknownSchemaNodes.nullOrEmpty»

                «writeUnknownSchemaNodes(module.unknownSchemaNodes)»
                «ENDIF»
                «IF !module.uses.nullOrEmpty»

                «writeUsesNodes(module.uses)»
                «ENDIF»
            }
        '''
    }

    def static writeRPCs(Set<RpcDefinition> rpcDefs) {
        '''
            «FOR rpc : rpcDefs»
                «IF rpc != null»
                «writeRPC(rpc)»
                «ENDIF»
            «ENDFOR»
        '''
    }

    def static writeRPC(RpcDefinition rpc) {
        '''
            rpc «rpc.QName.localName» {
                «IF !rpc.description.nullOrEmpty»
                    "«rpc.description»";
                «ENDIF»
                «IF !rpc.groupings.nullOrEmpty»
                    «writeGroupingDefs(rpc.groupings)»
                «ENDIF»
                «IF rpc.input != null»
                    «writeRpcInput(rpc.input)»
                «ENDIF»
                «IF rpc.output != null»
                    «writeRpcOutput(rpc.output)»
                «ENDIF»
                «IF !rpc.reference.nullOrEmpty»
                reference
                    "«rpc.reference»";
                «ENDIF»
                «IF rpc.status != null»
                status «rpc.status»;
                «ENDIF»
            }
        '''
    }

    def static writeRpcInput(ContainerSchemaNode input) {
        if(input == null)
            return ''

        '''
            input {
                «IF !input.childNodes.nullOrEmpty»
                «writeDataSchemaNodes(input.childNodes)»
                «ENDIF»
            }

        '''
    }

    def static writeRpcOutput(ContainerSchemaNode output) {
        if(output == null)
            return ''

        '''
            output {
                «IF !output.childNodes.nullOrEmpty»
                «writeDataSchemaNodes(output.childNodes)»
                «ENDIF»
            }
        '''
    }

    def static writeNotifications(Set<NotificationDefinition> notifications) {
        '''
            «FOR notification : notifications»
                «IF notification != null»
                «writeNotification(notification)»
                «ENDIF»
            «ENDFOR»
        '''
    }

    def static writeNotification(NotificationDefinition notification) {
        '''
            notification «notification.QName.localName» {
                «IF !notification.description.nullOrEmpty»
                description
                    "«notification.description»";
                «ENDIF»
                «IF !notification.childNodes.nullOrEmpty»
                    «writeDataSchemaNodes(notification.childNodes)»
                «ENDIF»
                «IF !notification.availableAugmentations.nullOrEmpty»
                    «writeAugments(notification.availableAugmentations)»
                «ENDIF»
                «IF !notification.groupings.nullOrEmpty»
                    «writeGroupingDefs(notification.groupings)»
                «ENDIF»
                «IF !notification.uses.nullOrEmpty»
                    «writeUsesNodes(notification.uses)»
                «ENDIF»
                «IF !notification.reference.nullOrEmpty»
                reference
                    "«notification.reference»";
                «ENDIF»
                «IF notification.status != null»
                status «notification.status»;
                «ENDIF»
            }
        '''
    }

    def static writeUnknownSchemaNodes(List<UnknownSchemaNode> unknownSchemaNodes) {
        if (unknownSchemaNodes.nullOrEmpty)
            return ''

        '''
            «FOR unknownSchemaNode : unknownSchemaNodes»
                «writeUnknownSchemaNode(unknownSchemaNode)»
            «ENDFOR»
        '''
    }

    def static writeUnknownSchemaNode(UnknownSchemaNode unknownSchemaNode) {
        if (unknownSchemaNode == null)
            return ''

        '''
            anyxml «unknownSchemaNode.QName.localName» {
                «IF !unknownSchemaNode.description.nullOrEmpty»
                description
                    "«unknownSchemaNode.description»";
                «ENDIF»
                «IF !unknownSchemaNode.reference.nullOrEmpty»
                reference
                    "«unknownSchemaNode.reference»";
                «ENDIF»
                «IF unknownSchemaNode.status != null»
                status «unknownSchemaNode.status»;
                «ENDIF»
            }
        '''
    }

    def static writeUsesNodes(Set<UsesNode> usesNodes) {
        if (usesNodes == null) {
            return ''
        }

        '''
            «FOR usesNode : usesNodes»
                «IF usesNode != null»
                «writeUsesNode(usesNode)»
                «ENDIF»
            «ENDFOR»
        '''
    }

    def static writeUsesNode(UsesNode usesNode) {
        val hasRefines = !usesNode.refines.empty

        '''
            uses «usesNode.groupingPath.pathFromRoot.head.localName»«IF !hasRefines»;«ELSE» {«ENDIF»
            «IF hasRefines»
                «writeRefines(usesNode.refines)»
            }
            «ENDIF»
        '''
    }

    def static writeRefines(Map<SchemaPath, SchemaNode> refines) {
        '''
            «FOR path : refines.keySet»
            «val schemaNode = refines.get(path)»
            «writeRefine(path, schemaNode)»
            «ENDFOR»
        '''
    }

    def static writeRefine(SchemaPath path, SchemaNode schemaNode) {
        '''
            refine «path.pathFromRoot.last» {
                «IF schemaNode instanceof DataSchemaNode»
                «writeDataSchemaNode(schemaNode as DataSchemaNode)»
                «ENDIF»
            }
        '''
    }

    def static writeTypeDefinitions(Set<TypeDefinition<?>> typeDefinitions) {
        '''
            «FOR typeDefinition : typeDefinitions»
                «IF typeDefinition != null»
                «writeTypeDefinition(typeDefinition)»
                «ENDIF»
            «ENDFOR»
        '''
    }

    def static writeTypeDefinition(TypeDefinition<?> typeDefinition) {
        '''
            type «typeDefinition.QName.localName»;
        '''
    }

    def static writeIdentities(Set<IdentitySchemaNode> identities) {
        if (identities.nullOrEmpty)
            return ''
        '''
            «FOR identity : identities»
                «writeIdentity(identity)»
            «ENDFOR»
        '''
    }

    def static writeIdentity(IdentitySchemaNode identity) {
        if (identity == null)
            return ''
        '''
            identity «identity.QName.localName» {
                «IF identity.baseIdentity != null»
                base "(«writeIdentityNs(identity.baseIdentity)»)«identity.baseIdentity»";
                «ENDIF»
                «IF !identity.description.nullOrEmpty»
                description
                    "«identity.description»";
                «ENDIF»
                «IF !identity.reference.nullOrEmpty»
                reference
                    "«identity.reference»";
                «ENDIF»
                «IF identity.status != null»
                status «identity.status»;
                «ENDIF»
            }
        '''
    }

    def static writeIdentityNs(IdentitySchemaNode identity) {
        if(module == null)
            return ''

        val identityNs = identity.QName.namespace

        if(!module.namespace.equals(identityNs))
            return identityNs + ":"
        return ''
    }

    def static writeFeatures(Set<FeatureDefinition> features) {
        '''
            «FOR feature : features»
                «IF feature != null»
                «writeFeature(feature)»
                «ENDIF»
            «ENDFOR»
        '''
    }

    def static writeFeature(FeatureDefinition featureDef) {
        '''
            feature «featureDef.QName.localName» {
                «IF !featureDef.description.nullOrEmpty»
                description
                    "«featureDef.description»";
                «ENDIF»
                «IF !featureDef.reference.nullOrEmpty»
                reference
                    "«featureDef.reference»";
                «ENDIF»
                «IF featureDef.status != null»
                status «featureDef.status»;
                «ENDIF»
            }
        '''
    }

    def static writeExtensions(List<ExtensionDefinition> extensions) {
        '''
            «FOR anExtension : extensions»
                «IF anExtension != null»
                «writeExtension(anExtension)»
                «ENDIF»
            «ENDFOR»
        '''
    }

    def static writeExtension(ExtensionDefinition extensionDef) {
        '''
            extension «extensionDef.QName.localName» {
                «IF !extensionDef.description.nullOrEmpty»
                description
                    "«extensionDef.description»";
                «ENDIF»
                «IF !extensionDef.argument.nullOrEmpty»
                argument "«extensionDef.argument»";
                «ENDIF»
                «IF !extensionDef.reference.nullOrEmpty»
                reference
                    "«extensionDef.reference»";
                «ENDIF»
                «IF extensionDef.status != null»
                status «extensionDef.status»;
                «ENDIF»
            }
        '''
    }

    def static writeDeviations(Set<Deviation> deviations) {
        '''
            «FOR deviation : deviations»
                «IF deviation != null»
                «writeDeviation(deviation)»
                «ENDIF»
            «ENDFOR»
        '''
    }

    def static writeDeviation(Deviation deviation) {
        '''
            deviation «deviation.targetPath» {
                «IF !deviation.reference.nullOrEmpty»
                    reference
                        "«deviation.reference»";
                «ENDIF»
                «IF deviation.deviate != null && !deviation.deviate.name.nullOrEmpty»
                    deviation «deviation.deviate.name»;
                «ENDIF»
            }
        '''
    }

    def static writeAugments(Set<AugmentationSchema> augments) {
        '''
            «FOR augment : augments»
                «IF augment != null»
                «writeAugment(augment)»
                «ENDIF»
            «ENDFOR»
        '''
    }

    def static writeDataSchemaNodes(Collection<DataSchemaNode> dataSchemaNodes) {
        '''
            «FOR schemaNode : dataSchemaNodes»
                «writeDataSchemaNode(schemaNode)»
            «ENDFOR»
        '''
    }

    def static CharSequence writeGroupingDefs(Set<GroupingDefinition> groupingDefs) {
        '''
            «FOR groupingDef : groupingDefs»
                «IF groupingDef != null»
                «writeGroupingDef(groupingDef)»
                «ENDIF»
            «ENDFOR»
        '''
    }

    def static writeAugment(AugmentationSchema augment) {
        '''
            augment «formatToAugmentPath(augment.targetPath.pathFromRoot)» {
                «IF augment.whenCondition != null && !augment.whenCondition.toString.nullOrEmpty»
                when "«augment.whenCondition.toString»";
                «ENDIF»
                «IF !augment.description.nullOrEmpty»
                description
                    "«augment.description»";
                «ENDIF»
                «IF !augment.reference.nullOrEmpty»
                reference
                    "«augment.reference»";
                «ENDIF»
                «IF augment.status != null»
                status «augment.status»;
                «ENDIF»
                «IF !augment.childNodes.nullOrEmpty»
                «writeDataSchemaNodes(augment.childNodes)»
                «ENDIF»
                «IF !augment.uses.nullOrEmpty»
                «writeUsesNodes(augment.uses)»
                «ENDIF»
            }
        '''
    }

    def static writeGroupingDef(GroupingDefinition groupingDef) {
        '''
            grouping «groupingDef.QName.localName» {
                «IF !groupingDef.groupings.nullOrEmpty»
                    «writeGroupingDefs(groupingDef.groupings)»
                «ENDIF»
                «IF !groupingDef.childNodes.nullOrEmpty»
                    «writeDataSchemaNodes(groupingDef.childNodes)»
                «ENDIF»
                «IF !groupingDef.unknownSchemaNodes.nullOrEmpty»
                    «writeUnknownSchemaNodes(groupingDef.unknownSchemaNodes)»
                «ENDIF»
            }
        '''
    }

    def static writeContSchemaNode(ContainerSchemaNode contSchemaNode) {
        '''
            container «contSchemaNode.getQName.localName» {
                «IF !contSchemaNode.childNodes.nullOrEmpty»
                «writeDataSchemaNodes(contSchemaNode.childNodes)»
                «ENDIF»
                «IF !contSchemaNode.availableAugmentations.nullOrEmpty»
                «writeAugments(contSchemaNode.availableAugmentations)»
                «ENDIF»
                «IF !contSchemaNode.groupings.nullOrEmpty»
                «writeGroupingDefs(contSchemaNode.groupings)»
                «ENDIF»
                «IF !contSchemaNode.uses.nullOrEmpty»
                «writeUsesNodes(contSchemaNode.uses)»
                «ENDIF»
                «IF !contSchemaNode.unknownSchemaNodes.nullOrEmpty»
                «writeUnknownSchemaNodes(contSchemaNode.unknownSchemaNodes)»
                «ENDIF»
            }
        '''
    }

    def static writeAnyXmlSchemaNode(AnyXmlSchemaNode anyXmlSchemaNode) {
        '''
            anyxml «anyXmlSchemaNode.getQName.localName»;
        '''
    }

    def static writeLeafSchemaNode(LeafSchemaNode leafSchemaNode) {
        '''
            leaf «leafSchemaNode.getQName.localName» {
                type «leafSchemaNode.type.getQName.localName»;
            }
        '''
    }

    def static writeLeafListSchemaNode(LeafListSchemaNode leafListSchemaNode) {
        '''
            leaf-list «leafListSchemaNode.getQName.localName» {
                type «leafListSchemaNode.type.getQName.localName»;
            }
        '''
    }

    def static writeChoiceCaseNode(ChoiceCaseNode choiceCaseNode) {
        '''
            case «choiceCaseNode.getQName.localName» {
                «FOR childNode : choiceCaseNode.childNodes»
                    «writeDataSchemaNode(childNode)»
                «ENDFOR»
            }
        '''
    }

    def static writeChoiceNode(ChoiceNode choiceNode) {
        '''
            choice «choiceNode.getQName.localName» {
                «FOR child : choiceNode.cases»
                    «writeDataSchemaNode(child)»
                «ENDFOR»
            }
        '''
    }

    def static writeListSchemaNode(ListSchemaNode listSchemaNode) {
        '''
            list «listSchemaNode.getQName.localName» {
                key «FOR listKey : listSchemaNode.keyDefinition SEPARATOR " "»"«listKey.localName»"
                «ENDFOR»
                «IF !listSchemaNode.childNodes.nullOrEmpty»
                    «writeDataSchemaNodes(listSchemaNode.childNodes)»
                «ENDIF»
                «IF !listSchemaNode.availableAugmentations.nullOrEmpty»
                    «writeAugments(listSchemaNode.availableAugmentations)»
                «ENDIF»
                «IF !listSchemaNode.groupings.nullOrEmpty»
                    «writeGroupingDefs(listSchemaNode.groupings)»
                «ENDIF»
                «IF !listSchemaNode.uses.nullOrEmpty»
                    «writeUsesNodes(listSchemaNode.uses)»
                «ENDIF»
                «IF !listSchemaNode.unknownSchemaNodes.nullOrEmpty»
                    «writeUnknownSchemaNodes(listSchemaNode.unknownSchemaNodes)»
                «ENDIF»
            }
        '''
    }

    def static CharSequence writeDataSchemaNode(DataSchemaNode child) {
        '''
            «IF child instanceof ContainerSchemaNode»
                «writeContSchemaNode(child as ContainerSchemaNode)»
            «ENDIF»
            «IF child instanceof AnyXmlSchemaNode»
                «writeAnyXmlSchemaNode(child as AnyXmlSchemaNode)»
            «ENDIF»
            «IF child instanceof LeafSchemaNode»
                «writeLeafSchemaNode(child as LeafSchemaNode)»
            «ENDIF»
            «IF child instanceof LeafListSchemaNode»
                «writeLeafListSchemaNode(child as LeafListSchemaNode)»
            «ENDIF»
            «IF child instanceof ChoiceCaseNode»
                «writeChoiceCaseNode(child as ChoiceCaseNode)»
            «ENDIF»
            «IF child instanceof ChoiceNode»
                «writeChoiceNode(child as ChoiceNode)»
            «ENDIF»
            «IF child instanceof ListSchemaNode»
                «writeListSchemaNode(child as ListSchemaNode)»
            «ENDIF»
        '''
    }
    
    static def String formatSchemaPath(String moduleName, Iterable<QName> schemaPath) {
        var currentElement = schemaPath.head
        val StringBuilder sb = new StringBuilder()
        sb.append(moduleName)

        for(pathElement : schemaPath) {
            if(!currentElement.namespace.equals(pathElement.namespace)) {
                currentElement = pathElement
                sb.append('/')
                sb.append(pathElement)
            }
            else {
                sb.append('/')
                sb.append(pathElement.localName)
            }
        }
        return sb.toString
    }

    static def String formatToParagraph(String text, int nextLineIndent) {
        if (text == null || text.isEmpty())
            return '';

        var String formattedText = text;
        val StringBuilder sb = new StringBuilder();
        val StringBuilder lineBuilder = new StringBuilder();
        var boolean isFirstElementOnNewLineEmptyChar = false;
        val lineIndent = computeNextLineIndent(nextLineIndent);

        formattedText = formattedText.replace("*/", "&#42;&#47;");
        formattedText = NEWLINE_OR_TAB.removeFrom(formattedText);
        formattedText = formattedText.replaceAll(" +", " ");

        val StringTokenizer tokenizer = new StringTokenizer(formattedText, " ", true);

        while (tokenizer.hasMoreElements()) {
            val String nextElement = tokenizer.nextElement().toString();

            if (lineBuilder.length() + nextElement.length() > 80) {
                if (lineBuilder.charAt(lineBuilder.length() - 1) == ' ') {
                    lineBuilder.setLength(0);
                    lineBuilder.append(lineBuilder.substring(0, lineBuilder.length() - 1));
                }
                if (lineBuilder.charAt(0) == ' ') {
                    lineBuilder.setLength(0);
                    lineBuilder.append(lineBuilder.substring(1));
                }

                sb.append(lineBuilder);
                lineBuilder.setLength(0);
                sb.append("\n");

                if (nextLineIndent > 0) {
                    sb.append(lineIndent)
                }

                if (nextElement.toString().equals(" "))
                    isFirstElementOnNewLineEmptyChar = !isFirstElementOnNewLineEmptyChar;
            }
            if (isFirstElementOnNewLineEmptyChar) {
                isFirstElementOnNewLineEmptyChar = !isFirstElementOnNewLineEmptyChar;
            } else {
                lineBuilder.append(nextElement);
            }
        }
        sb.append(lineBuilder);
        sb.append("\n");

        return sb.toString();
    }

    def private static formatToAugmentPath(Iterable<QName> schemaPath) {
        val StringBuilder sb = new StringBuilder();

        for(pathElement : schemaPath) {
            val ns = pathElement.namespace
            val localName = pathElement.localName

            sb.append("\\(")
            sb.append(ns)
            sb.append(')')
            sb.append(localName)
        }
        return sb.toString
    }

    private static def computeNextLineIndent(int nextLineIndent) {
        val StringBuilder sb = new StringBuilder()
        var i = 0
        while (i < nextLineIndent) {
            sb.append(' ')
            i = i + 1
        }
        return sb.toString
    }
}
