/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.unified.doc.generator

import com.google.common.collect.Iterables
import java.io.BufferedWriter
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Collection
import java.util.HashMap
import java.util.HashSet
import java.util.LinkedHashMap
import java.util.List
import java.util.Map
import java.util.Set
import org.opendaylight.yangtools.yang.common.QName
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode
import org.opendaylight.yangtools.yang.model.api.ChoiceNode
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode
import org.opendaylight.yangtools.yang.model.api.Module
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition
import org.opendaylight.yangtools.yang.model.api.SchemaContext
import org.opendaylight.yangtools.yang.model.api.SchemaNode
import org.opendaylight.yangtools.yang.model.api.SchemaPath
import org.opendaylight.yangtools.yang.model.api.TypeDefinition
import org.opendaylight.yangtools.yang.model.api.UsesNode
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition
import org.opendaylight.yangtools.yang.model.util.ExtendedType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.sonatype.plexus.build.incremental.BuildContext
import org.sonatype.plexus.build.incremental.DefaultBuildContext

class GeneratorImpl {

    File path
    static val REVISION_FORMAT = new SimpleDateFormat("yyyy-MM-dd")
    static val Logger LOG = LoggerFactory.getLogger(GeneratorImpl)
    static val BuildContext CTX = new DefaultBuildContext();
    var Module currentModule;
    val Map<String, String> imports = new HashMap();
    var SchemaContext ctx;
    
    StringBuilder augmentChildNodesAsString
    
    DataSchemaNode lastNodeInTargetPath = null

    def generate(SchemaContext context, File targetPath, Set<Module> modulesToGen) throws IOException {
        path = targetPath;
        path.mkdirs();
        val it = new HashSet;
        for (module : modulesToGen) {
            add(generateDocumentation(module, context));
        }
        return it;
    }

    def generateDocumentation(Module module, SchemaContext ctx) {
        val destination = new File(path, '''«module.name».html''')
        this.ctx = ctx;
        module.imports.forEach[importModule | this.imports.put(importModule.prefix, importModule.moduleName)]
        try {
            val fw = new OutputStreamWriter(CTX.newFileOutputStream(destination))
            val bw = new BufferedWriter(fw)
            currentModule = module;
            bw.append(generate(module, ctx));
            bw.close();
            fw.close();
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        return destination;
    }

    def generate(Module module, SchemaContext ctx) '''
        <!DOCTYPE html>
        <html lang="en">
          <head>
            <title>«module.name»</title>
          </head>
          <body>
            «body(module, ctx)»
          </body>
        </html>
    '''

    def body(Module module, SchemaContext ctx) '''
        «header(module)»

        «typeDefinitionsSummary(module)»
        «identitiesSummary(module)»
        «groupingsSummary(module)»
        «augmentationsSummary(module, ctx)»
        «objectsSummary(module)»
        «notificationsSummary(module)»
        «rpcsSummary(module)»
        «extensionsSummary(module)»
        «featuresSummary(module)»

        «typeDefinitions(module)»

        «identities(module)»

        «groupings(module)»

        «dataStore(module)»

        «childNodes(module)»

        «notifications(module)»

        «augmentations(module, ctx)»

        «rpcs(module)»

        «extensions(module)»

        «features(module)»

    '''


    private def typeDefinitionsSummary(Module module) {
        val Set<TypeDefinition<?>> typedefs = module.typeDefinitions
        if (typedefs.empty) {
            return '';
        }
        return '''
        <div>
            <h3>Type Definitions Summary</h3>
            <table>
                <tr>
                    <th>Name</th>
                    <th>Description</th>
                </tr>
                «FOR typedef : typedefs»
                <tr>
                    <td>
                    «anchorLink(typedef.QName.localName, strong(typedef.QName.localName))»
                    </td>
                    <td>
                    «typedef.description»
                    </td>
                </tr>
                «ENDFOR»
            </table>
        </div>
        '''
    }

    def typeDefinitions(Module module) {
        val Set<TypeDefinition<?>> typedefs = module.typeDefinitions
        if (typedefs.empty) {
            return '';
        }
        return '''
            <h2>Type Definitions</h2>
            <ul>
            «FOR typedef : typedefs»
                <li>
                    <h3 id="«typedef.QName.localName»">«typedef.QName.localName»</h3>
                    <ul>
                    «typedef.descAndRefLi»
                    «typedef.restrictions»
                    </ul>
                </li>
            «ENDFOR»
            </ul>
        '''
    }

    private def identities(Module module) {
        if (module.identities.empty) {
            return '';
        }
        return '''
            <h2>Identities</h2>
            <ul>
            «FOR identity : module.identities»
                <li>
                    <h3 id="«identity.QName.localName»">«identity.QName.localName»</h3>
                    <ul>
                    «identity.descAndRefLi»
                    «IF identity.baseIdentity !== null»
                        «listItem("base", identity.baseIdentity.QName.localName)»
                    «ENDIF»
                    </ul>
                </li>
            «ENDFOR»
            </ul>
        '''
    }

    private def identitiesSummary(Module module) {
        if (module.identities.empty) {
            return '';
        }
        return '''
        <h3>Identities Summary</h3>
        <table>
            <tr>
                <th>Name</th>
                <th>Description</th>
            </tr>
            «FOR identity : module.identities»
            <tr>
                <td>
                «anchorLink(identity.QName.localName, strong(identity.QName.localName))»
                </td>
                <td>
                «identity.description»
                </td>
            </tr>
            «ENDFOR»
        </table>
        '''
    }

    private def groupings(Module module) {
        if (module.groupings.empty) {
            return '';
        }
        return '''
            <h2>Groupings</h2>
            <ul>
            «FOR grouping : module.groupings»
                <li>
                    <h3 id="«grouping.QName.localName»">«grouping.QName.localName»</h3>
                    <ul>
                        «grouping.descAndRefLi»
                        «FOR childNode : grouping.childNodes»
                            «childNode.printSchemaNodeInfo»
                        «ENDFOR»
                    </ul>
                </li>
            «ENDFOR»
            </ul>
        '''
    }

    private def groupingsSummary(Module module) {
        if (module.groupings.empty) {
            return '';
        }
        return '''
        <h3>Groupings Summary</h3>
        <table>
            <tr>
                <th>Name</th>
                <th>Description</th>
            </tr>
            «FOR grouping : module.groupings»
            <tr>
                <td>
                «anchorLink(grouping.QName.localName, strong(grouping.QName.localName))»
                </td>
                <td>
                «grouping.description»
                </td>
            </tr>
            «ENDFOR»
        </table>
        '''
    }

    def dataStore(Module module) {
        if (module.childNodes.empty) {
            return '';
        }
        return '''
            <h2>Datastore Structure</h2>
            «tree(module)»
        '''
    }

    def augmentations(Module module, SchemaContext context) {
        if (module.augmentations.empty) {
            return '';
        }
        return '''
            <h2>Augmentations</h2>
            
            <ul>
            «FOR augment : module.augmentations»
                <li>
                    <h3 id="«schemaPathToString(module, augment.targetPath, context, augment)»">
                    Target [«typeAnchorLink(augment.targetPath,schemaPathToString(module, augment.targetPath, context, augment))»]</h3>
                    «augment.description»
                        Status: «strong(String.valueOf(augment.status))»
                    «IF augment.reference !== null»
                        Reference «augment.reference»
                    «ENDIF»
                    «IF augment.whenCondition !== null»
                        When «augment.whenCondition.toString»
                    «ENDIF»
                    «FOR childNode : augment.childNodes»
                        «childNode.printSchemaNodeInfo»
                    «ENDFOR»
                    
                    <h3>Example</h3>
                    «createAugmentChildNodesAsString(new ArrayList(augment.childNodes))»
                    «printNodeChildren(parseTargetPath(augment.targetPath))»
                </li>
            «ENDFOR»
            </ul>
        '''
    }
    
    private def createAugmentChildNodesAsString(List<DataSchemaNode> childNodes) {
        augmentChildNodesAsString = new StringBuilder();
        augmentChildNodesAsString.append(printNodeChildren(childNodes))
        return ''
    }
    
    private def parseTargetPath(SchemaPath path) {
        val List<DataSchemaNode> nodes = new ArrayList<DataSchemaNode>();
        for (QName pathElement : path.pathFromRoot) {
            val module = ctx.findModuleByNamespaceAndRevision(pathElement.namespace, pathElement.revision);
            if (module !== null) {
                var foundNode = module.getDataChildByName(pathElement)
                if(foundNode == null) {
                    val child = nodes.last
                    if (child instanceof DataNodeContainer) {
                        val dataContNode = child as DataNodeContainer
                        foundNode = findNodeInChildNodes(pathElement, dataContNode.childNodes)
                    }
                }
                if(foundNode != null) {
                    nodes.add(foundNode);
                }
            }
        }
        if(! nodes.empty) {
            lastNodeInTargetPath = nodes.get(nodes.size() - 1)
        }
        
        val List<DataSchemaNode> targetPathNodes = new ArrayList<DataSchemaNode>();
        targetPathNodes.add(lastNodeInTargetPath)
        
        return targetPathNodes
    }
    
    private def DataSchemaNode findNodeInChildNodes(QName findingNode, Iterable<DataSchemaNode> childNodes) {
        for(child : childNodes) {
            if (child.QName.equals(findingNode))
                return child;
        }
        // find recursively
        for(child : childNodes) {
            if(child instanceof ContainerSchemaNode) {
                val foundChild = findNodeInChildNodes(findingNode, child.childNodes)
                if (foundChild != null)
                    return foundChild;
            }
            else if(child instanceof ListSchemaNode) {
                val foundChild = findNodeInChildNodes(findingNode, child.childNodes)
                if (foundChild != null)
                    return foundChild;
            }
        }
    }
    
    private def printNodeChildren(List<DataSchemaNode> childNodes) {
        if (childNodes.empty) {
            return ''
        }
        
        return 
        '''
        <pre>
        «printAugmentedNode(childNodes.get(0))»
        </pre>
        '''
    }
    
    private def CharSequence printAugmentedNode(DataSchemaNode child) {
        
        if(child instanceof ChoiceCaseNode)
            return ''
            
        return
        '''
        «IF child instanceof ContainerSchemaNode»
            «printContainerNode(child)»
        «ENDIF»
        «IF child instanceof AnyXmlSchemaNode»
            «printAnyXmlNode(child)»
        «ENDIF»
        «IF child instanceof LeafSchemaNode»
            «printLeafNode(child)»
        «ENDIF»
        «IF child instanceof LeafListSchemaNode»
            «printLeafListNode(child)»
        «ENDIF»
        «IF child instanceof ListSchemaNode»
            «printListNode(child)»
        «ENDIF»
        «IF child instanceof ChoiceNode»
            «printChoiceNode(child)»
        «ENDIF»
        '''
    }
    
    private def printChoiceNode(ChoiceNode child) {
        val List<ChoiceCaseNode> cases = new ArrayList(child.cases);
        if(!cases.empty) {
            val ChoiceCaseNode aCase = cases.get(0)
            for(caseChildNode : aCase.childNodes)
                printAugmentedNode(caseChildNode)
        }
    }
    
    private def printListNode(ListSchemaNode listNode) {
        return
        '''
            &lt;«listNode.QName.localName»«IF !listNode.QName.namespace.equals(currentModule.namespace)» xmlns="«listNode.QName.namespace»"«ENDIF»&gt;
                «FOR child : listNode.childNodes»
                    «printAugmentedNode(child)»
                «ENDFOR»
            &lt;/«listNode.QName.localName»&gt;
        '''
    }
    
    private def printContainerNode(ContainerSchemaNode containerNode) {
        return
        '''
            &lt;«containerNode.QName.localName»«IF !containerNode.QName.namespace.equals(currentModule.namespace)» xmlns="«containerNode.QName.namespace»"«ENDIF»&gt;
                «FOR child : containerNode.childNodes»
                    «printAugmentedNode(child)»
                «ENDFOR»
            &lt;/«containerNode.QName.localName»&gt;
        '''
    }
    
    private def printLeafListNode(LeafListSchemaNode leafListNode) {
        return
        '''
            &lt;«leafListNode.QName.localName»&gt;. . .&lt;/«leafListNode.QName.localName»&gt;
            &lt;«leafListNode.QName.localName»&gt;. . .&lt;/«leafListNode.QName.localName»&gt;
            &lt;«leafListNode.QName.localName»&gt;. . .&lt;/«leafListNode.QName.localName»&gt;
        '''
    }
    
    private def printAnyXmlNode(AnyXmlSchemaNode anyXmlNode) {
        return 
        '''
            &lt;«anyXmlNode.QName.localName»&gt;. . .&lt;/«anyXmlNode.QName.localName»&gt;
        '''
    }
    
    private def printLeafNode(LeafSchemaNode leafNode) {
        return 
        '''
            &lt;«leafNode.QName.localName»&gt;. . .&lt;/«leafNode.QName.localName»&gt;
        '''
    }

    private def augmentationsSummary(Module module, SchemaContext context) {
        if (module.augmentations.empty) {
            return '';
        }
        return '''
        <h3>Augmentations Summary</h3>
        <table>
            <tr>
                <th>Target</th>
                <th>Description</th>
            </tr>
            «FOR augment : module.augmentations»
            <tr>
                <td>
                «anchorLink(schemaPathToString(module, augment.targetPath, context, augment),
                strong(schemaPathToString(module, augment.targetPath, context, augment)))»
                </td>
                <td>
                «augment.description»
                </td>
            </tr>
            «ENDFOR»
        </table>
        '''
    }

    def notifications(Module module) {
        val Set<NotificationDefinition> notificationdefs = module.notifications
        if (notificationdefs.empty) {
            return '';
        }

        return '''
            <h2>Notifications</h2>
            «FOR notificationdef : notificationdefs»

                <h3 id="«notificationdef.path.schemaPathToId»">«notificationdef.nodeName»</h3>
                    «notificationdef.descAndRef»
                    «FOR childNode : notificationdef.childNodes»
                        «childNode.printSchemaNodeInfo»
                    «ENDFOR»
            «ENDFOR»
        '''
    }

    private def notificationsSummary(Module module) {
        if (module.notifications.empty) {
            return '';
        }
        return '''
        <h3>Notifications Summary</h3>
        <table>
            <tr>
                <th>Name</th>
                <th>Description</th>
            </tr>
            «FOR notification : module.notifications»
            <tr>
                <td>
                «anchorLink(notification.path.schemaPathToId, strong(notification.QName.localName))»
                </td>
                <td>
                «notification.description»
                </td>
            </tr>
            «ENDFOR»
        </table>
        '''
    }

    def rpcs(Module module) {
        if (module.rpcs.empty) {
            return '';
        }

        return '''
            <h2>RPC Definitions</h2>
            «FOR rpc : module.rpcs»
                <h3 id="«rpc.QName.localName»">«rpc.nodeName»</h3>
                    <ul>
                        «rpc.descAndRefLi»
                        «rpc.input.printSchemaNodeInfo»
                        «rpc.output.printSchemaNodeInfo»
                    </ul>
            «ENDFOR»
            </ul>
        '''
    }

    private def rpcsSummary(Module module) {
        if (module.rpcs.empty) {
            return '';
        }
        return '''
        <h3>RPCs Summary</h3>
        <table>
            <tr>
                <th>Name</th>
                <th>Description</th>
            </tr>
            «FOR rpc : module.rpcs»
            <tr>
                <td>
                «anchorLink(rpc.QName.localName, strong(rpc.QName.localName))»
                </td>
                <td>
                «rpc.description»
                </td>
            </tr>
            «ENDFOR»
        </table>
        '''
    }

    def extensions(Module module) {
        if (module.extensionSchemaNodes.empty) {
            return '';
        }
        return '''
            <h2>Extensions</h2>
            «FOR ext : module.extensionSchemaNodes»
                <li>
                    <h3 id="«ext.QName.localName»">«ext.nodeName»</h3>
                </li>
                «extensionInfo(ext)»
            «ENDFOR»
        '''
    }

    private def extensionsSummary(Module module) {
        if (module.extensionSchemaNodes.empty) {
            return '';
        }
        return '''
        <h3>Extensions Summary</h3>
        <table>
            <tr>
                <th>Name</th>
                <th>Description</th>
            </tr>
            «FOR ext : module.extensionSchemaNodes»
            <tr>
                <td>
                «anchorLink(ext.QName.localName, strong(ext.QName.localName))»
                </td>
                <td>
                «ext.description»
                </td>
            </tr>
            «ENDFOR»
        </table>
        '''
    }

    def features(Module module) {
        if (module.features.empty) {
            return '';
        }
        return '''
            <h2>Features</h2>

            <ul>
            «FOR feature : module.features»
                <li>
                    <h3 id="«feature.QName.localName»">«feature.QName.localName»</h3>
                    <ul>
                        «feature.descAndRefLi»
                    </ul>
                </li>
            «ENDFOR»
            </ul>
        '''
    }

    private def featuresSummary(Module module) {
        if (module.features.empty) {
            return '';
        }
        return '''
        <h3>Features Summary</h3>
        <table>
            <tr>
                <th>Name</th>
                <th>Description</th>
            </tr>
            «FOR feature : module.features»
            <tr>
                <td>
                «anchorLink(feature.QName.localName, strong(feature.QName.localName))»
                </td>
                <td>
                «feature.description»
                </td>
            </tr>
            «ENDFOR»
        </table>
        '''
    }

    private def objectsSummary(Module module) {
        if (module.childNodes.empty) {
            return '';
        }
        return '''
        <h3>Child Nodes Summary</h3>
        <table>
            <tr>
                <th>Name</th>
                <th>Description</th>
            </tr>
            «FOR childNode : module.childNodes»
            <tr>
                <td>
                «anchorLink(childNode.QName.localName, strong(childNode.QName.localName))»
                </td>
                <td>
                «childNode.description»
                </td>
            </tr>
            «ENDFOR»
        </table>
        '''
    }

    def header(Module module)
    '''
        <h1>«module.name»</h1>

        <h2>Base Information</h2>
        <table>
            <tr>
                <td>«strong("prefix")»</td>
                <td>«module.prefix»</td>
            </tr>
            <tr>
                <td>«strong("namespace")»</td>
                <td>«module.namespace»</td>
            </tr>
            <tr>
                <td>«strong("revision")»</td>
                <td>«REVISION_FORMAT.format(module.revision)»</td>
            </tr>
            <tr>
                <td>«strong("description")»</td>
                <td>«module.description»</td>
            </tr>
            <tr>
                <td>«strong("yang-version")»</td>
                <td>«module.yangVersion»</td>
            </tr>
            <tr>
                «FOR imp : module.imports BEFORE '''<td>«strong("imports")»</td><td>''' AFTER '''</td>'''»
                    «imp.prefix»:«imp.moduleName»«IF imp.revision !== null» «REVISION_FORMAT.format(imp.revision)»«ENDIF»;
                «ENDFOR»
            </tr>
        </table>
    '''

    def CharSequence schemaPathToId(SchemaPath path) {
        if(path !== null) {
            return '''«FOR qName : path.path SEPARATOR "/"»«qName.localName»«ENDFOR»'''
        }
    }

    def code(String string) '''<code>«string»</code>'''

    def process(Module module) {
        throw new UnsupportedOperationException("TODO: auto-generated method stub")
    }

    def CharSequence tree(Module module) '''
        «strong(module.name)»
        «module.childNodes.treeSet(YangInstanceIdentifier.builder.build())»
    '''

    private def dispatch CharSequence tree(ChoiceNode node,YangInstanceIdentifier path) '''
        «node.nodeName» (choice)
        «casesTree(node.cases,path)»
    '''

    def casesTree(Set<ChoiceCaseNode> nodes,YangInstanceIdentifier path) '''
        <ul>
        «FOR node : nodes»
            <li>
            «node.nodeName»
            «node.childNodes.treeSet(path)»
            </li>
        «ENDFOR»
        </ul>
    '''

    private def dispatch CharSequence tree(DataSchemaNode node,YangInstanceIdentifier path) '''
        «node.nodeName»
    '''

    private def dispatch CharSequence tree(ListSchemaNode node,YangInstanceIdentifier path) '''
        «val newPath = path.append(node)»
        «localLink(newPath,node.nodeName)»
        «node.childNodes.treeSet(newPath)»
    '''

    private def dispatch CharSequence tree(ContainerSchemaNode node,YangInstanceIdentifier path) '''
        «val newPath = path.append(node)»
        «localLink(newPath,node.nodeName)»
        «node.childNodes.treeSet(newPath)»
    '''

    def CharSequence childNodes(Module module) '''
        «val childNodes = module.childNodes»
        «IF !childNodes.nullOrEmpty»
            <h2>Child nodes</h2>

            «childNodes.printChildren(3,YangInstanceIdentifier.builder().build())»
        «ENDIF»
    '''

    def CharSequence printSchemaNodeInfo(DataSchemaNode node) {
        return '''
            <ul>
            «node.printBaseInfo»
            «IF node instanceof DataNodeContainer»
                «val dataNode = node as DataNodeContainer»
                <ul>
                «FOR usesNode : dataNode.uses»
                    «usesNode.printUses»
                «ENDFOR»
                </ul>
                <ul>
                «val Set<TypeDefinition<?>> typeDefinitions = dataNode.typeDefinitions»
                «FOR typeDef : typeDefinitions»
                    «typeDef.restrictions»
                «ENDFOR»
                </ul>
                <ul>
                «FOR grouping : dataNode.groupings»
                    «grouping.printGrouping»
                «ENDFOR»
                </ul>
                <ul>
                «FOR child : dataNode.childNodes»
                    «child.printSchemaNodeInfo»
                «ENDFOR»
                </ul>
            «ENDIF»
            </ul>
        '''
    }

    def String typeAnchorLink(SchemaPath path, CharSequence text) {
        if(path !== null) {
            val lastElement = Iterables.getLast(path.pathFromRoot)
            val ns = lastElement.namespace
            if (ns == this.currentModule.namespace) {
                return '''<a href="#«path.schemaPathToId»">«text»</a>'''
            } else {
                return '''(«ns»)«text»'''
                //to enable external (import) links
                //return '''<a href="«module».html#«path.schemaPathToId»">«prefix»:«text»</a>'''
            }
        }
    }

    def CharSequence printBaseInfo(SchemaNode node) {
        if(node instanceof LeafSchemaNode) {
            return '''
                «printInfo(node, "leaf")»
                «listItem("type", typeAnchorLink(node.type?.path, node.type.QName.localName))»
                «listItem("units", node.units)»
                «listItem("default", node.^default)»
                </ul>
            '''
        } else if(node instanceof LeafListSchemaNode) {
            return '''
                «printInfo(node, "leaf-list")»
                «listItem("type", node.type?.QName.localName)»
                </ul>
            '''
        } else if(node instanceof ListSchemaNode) {
            return '''
                «printInfo(node, "list")»
                «FOR keyDef : node.keyDefinition»
                    «listItem("key definition", keyDef.localName)»
                «ENDFOR»
                </ul>
            '''
        } else if(node instanceof ChoiceNode) {
            return '''
                «printInfo(node, "choice")»
                «listItem("default case", node.defaultCase)»
                «FOR caseNode : node.cases»
                    «caseNode.printSchemaNodeInfo»
                «ENDFOR»
                </ul>
            '''
        } else if(node instanceof ChoiceCaseNode) {
            return '''
                «printInfo(node, "case")»
                </ul>
            '''
        } else if(node instanceof ContainerSchemaNode) {
            return '''
                «printInfo(node, "container")»
                </ul>
            '''
        } else if(node instanceof AnyXmlSchemaNode) {
            return '''
                «printInfo(node, "anyxml")»
                </ul>
            '''
        }
    }

    def CharSequence printInfo(SchemaNode node, String nodeType) {
        return '''
            «IF node instanceof AugmentationTarget»
                «IF node !== null»
                    <strong>
                    <li id="«node.path.schemaPathToId»">
                        «nodeType»: «node.QName.localName»
                    </li>
                    </strong>
                «ENDIF»
            «ELSE»
                «strong(listItem(nodeType, node.QName.localName))»
            «ENDIF»
            <ul>
            «listItem("description", node.description)»
            «listItem("reference", node.reference)»
            «IF node instanceof DataSchemaNode»
                «listItem("when condition", node.constraints.whenCondition?.toString)»
                «listItem("min elements", node.constraints.minElements?.toString)»
                «listItem("max elements", node.constraints.maxElements?.toString)»
            «ENDIF»
        '''
    }

    def CharSequence printUses(UsesNode usesNode) {
        return '''
            «strong(listItem("uses", typeAnchorLink(usesNode.groupingPath, usesNode.groupingPath.pathTowardsRoot.iterator.next.localName)))»
            <ul>
            <li>refines:
                <ul>
                «FOR sp : usesNode.refines.keySet»
                    «listItem("node name", usesNode.refines.get(sp).QName.localName)»
                «ENDFOR»
                </ul>
            </li>
            «FOR augment : usesNode.augmentations»
                «typeAnchorLink(augment.targetPath,schemaPathToString(currentModule, augment.targetPath, ctx, augment))»
            «ENDFOR»
            </ul>
        '''
    }

    def CharSequence printGrouping(GroupingDefinition grouping) {
        return '''
            «strong(listItem("grouping", grouping.QName.localName))»
        '''
    }

    def CharSequence printChildren(Iterable<DataSchemaNode> nodes, int level, YangInstanceIdentifier path) {
        val anyxmlNodes = nodes.filter(AnyXmlSchemaNode)
        val leafNodes = nodes.filter(LeafSchemaNode)
        val leafListNodes = nodes.filter(LeafListSchemaNode)
        val choices = nodes.filter(ChoiceNode)
        val cases = nodes.filter(ChoiceCaseNode)
        val containers = nodes.filter(ContainerSchemaNode)
        val lists = nodes.filter(ListSchemaNode)
        return '''
            «IF ((anyxmlNodes.size + leafNodes.size + leafListNodes.size + containers.size + lists.size) > 0)»
            <h3>Direct children</h3>
            <ul>
            «FOR childNode : anyxmlNodes»
                «childNode.printShortInfo(level,path)»
            «ENDFOR»
            «FOR childNode : leafNodes»
                «childNode.printShortInfo(level,path)»
            «ENDFOR»
            «FOR childNode : leafListNodes»
                «childNode.printShortInfo(level,path)»
            «ENDFOR»
            «FOR childNode : containers»
                «childNode.printShortInfo(level,path)»
            «ENDFOR»
            «FOR childNode : lists»
                «childNode.printShortInfo(level,path)»
            «ENDFOR»
            </ul>
            «ENDIF»

            «IF path.pathArguments.iterator.hasNext»
            <h3>XML example</h3>
            «nodes.xmlExample(path.pathArguments.last.nodeType,path)»
            </h3>
            «ENDIF»
            «FOR childNode : containers»
                «childNode.printInfo(level,path)»
            «ENDFOR»
            «FOR childNode : lists»
                «childNode.printInfo(level,path)»
            «ENDFOR»
            «FOR childNode : choices»
                «childNode.printInfo(level,path)»
            «ENDFOR»
            «FOR childNode : cases»
                «childNode.printInfo(level,path)»
            «ENDFOR»
        '''
    }

    def CharSequence xmlExample(Iterable<DataSchemaNode> nodes, QName name,YangInstanceIdentifier path) '''
    <pre>
        «xmlExampleTag(name,nodes.xmplExampleTags(path))»
    </pre>
    '''

    def CharSequence xmplExampleTags(Iterable<DataSchemaNode> nodes, YangInstanceIdentifier identifier) '''
        <!-- Child nodes -->
        «FOR node : nodes»
        <!-- «node.QName.localName» -->
            «node.asXmlExampleTag(identifier)»
        «ENDFOR»

    '''

    private def dispatch CharSequence asXmlExampleTag(LeafSchemaNode node, YangInstanceIdentifier identifier) '''
        «node.QName.xmlExampleTag("...")»
    '''

    private def dispatch CharSequence asXmlExampleTag(LeafListSchemaNode node, YangInstanceIdentifier identifier) '''
        &lt!-- This node could appear multiple times --&gt
        «node.QName.xmlExampleTag("...")»
    '''

    private def dispatch CharSequence asXmlExampleTag(ContainerSchemaNode node, YangInstanceIdentifier identifier) '''
        &lt!-- See «localLink(identifier.append(node),"definition")» for child nodes.  --&gt
        «node.QName.xmlExampleTag("...")»
    '''


    private def dispatch CharSequence asXmlExampleTag(ListSchemaNode node, YangInstanceIdentifier identifier) '''
        &lt!-- See «localLink(identifier.append(node),"definition")» for child nodes.  --&gt
        &lt!-- This node could appear multiple times --&gt
        «node.QName.xmlExampleTag("...")»
    '''


    private def dispatch CharSequence asXmlExampleTag(DataSchemaNode node, YangInstanceIdentifier identifier) '''
        <!-- noop -->
    '''


    def xmlExampleTag(QName name, CharSequence data) {
        return '''&lt;«name.localName» xmlns="«name.namespace»"&gt;«data»&lt;/«name.localName»&gt;'''
    }

    def header(int level,QName name) '''<h«level»>«name.localName»</h«level»>'''


    def header(int level,YangInstanceIdentifier name) '''
        <h«level» id="«FOR cmp : name.pathArguments SEPARATOR "/"»«cmp.nodeType.localName»«ENDFOR»">
            «FOR cmp : name.pathArguments SEPARATOR "/"»«cmp.nodeType.localName»«ENDFOR»
        </h«level»>
    '''



    private def dispatch CharSequence printInfo(DataSchemaNode node, int level, YangInstanceIdentifier path) '''
        «header(level+1,node.QName)»
    '''

    private def dispatch CharSequence printInfo(ContainerSchemaNode node, int level, YangInstanceIdentifier path) '''
        «val newPath = path.append(node)»
        «header(level,newPath)»
        <dl>
          <dt>XML Path</dt>
          <dd>«newPath.asXmlPath»</dd>
          <dt>Restconf path</dt>
          <dd>«code(newPath.asRestconfPath)»</dd>
        </dl>
        «node.childNodes.printChildren(level,newPath)»
    '''

    private def dispatch CharSequence printInfo(ListSchemaNode node, int level, YangInstanceIdentifier path) '''
        «val newPath = path.append(node)»
        «header(level,newPath)»
        <dl>
          <dt>XML Path</dt>
          <dd>«newPath.asXmlPath»</dd>
          <dt>Restconf path</dt>
          <dd>«code(newPath.asRestconfPath)»</dd>
        </dl>
        «node.childNodes.printChildren(level,newPath)»
    '''

    private def dispatch CharSequence printInfo(ChoiceNode node, int level, YangInstanceIdentifier path) '''
        «val Set<DataSchemaNode> choiceCases = new HashSet(node.cases)»
        «choiceCases.printChildren(level,path)»
    '''

    private def dispatch CharSequence printInfo(ChoiceCaseNode node, int level, YangInstanceIdentifier path) '''
        «node.childNodes.printChildren(level,path)»
    '''



    def CharSequence printShortInfo(ContainerSchemaNode node, int level, YangInstanceIdentifier path) {
        val newPath = path.append(node);
        return '''
            <li>«strong(localLink(newPath,node.QName.localName))» (container)
            <ul>
                <li>configuration data: «strong(String.valueOf(node.configuration))»</li>
            </ul>
            </li>
        '''
    }

    def CharSequence printShortInfo(ListSchemaNode node, int level, YangInstanceIdentifier path) {
        val newPath = path.append(node);
        return '''
            <li>«strong(localLink(newPath,node.QName.localName))» (list)
            <ul>
                <li>configuration data: «strong(String.valueOf(node.configuration))»</li>
            </ul>
            </li>
        '''
    }

    def CharSequence printShortInfo(AnyXmlSchemaNode node, int level, YangInstanceIdentifier path) {
        return '''
            <li>«strong((node.QName.localName))» (anyxml)
            <ul>
                <li>configuration data: «strong(String.valueOf(node.configuration))»</li>
                <li>mandatory: «strong(String.valueOf(node.constraints.mandatory))»</li>
            </ul>
            </li>
        '''
    }

    def CharSequence printShortInfo(LeafSchemaNode node, int level, YangInstanceIdentifier path) {
        return '''
            <li>«strong((node.QName.localName))» (leaf)
            <ul>
                <li>configuration data: «strong(String.valueOf(node.configuration))»</li>
                <li>mandatory: «strong(String.valueOf(node.constraints.mandatory))»</li>
            </ul>
            </li>
        '''
    }

    def CharSequence printShortInfo(LeafListSchemaNode node, int level, YangInstanceIdentifier path) {
        return '''
            <li>«strong((node.QName.localName))» (leaf-list)
            <ul>
                <li>configuration data: «strong(String.valueOf(node.configuration))»</li>
            </ul>
            </li>
        '''
    }

    def CharSequence anchorLink(CharSequence anchor, CharSequence text) {
        return '''
            <a href="#«anchor»">«text»</a>
        '''
    }

    def CharSequence localLink(YangInstanceIdentifier identifier, CharSequence text) '''
        <a href="#«FOR cmp : identifier.pathArguments SEPARATOR "/"»«cmp.nodeType.localName»«ENDFOR»">«text»</a>
    '''


    private def dispatch YangInstanceIdentifier append(YangInstanceIdentifier identifier, ContainerSchemaNode node) {
        return identifier.node(node.QName);
    }

    private def dispatch YangInstanceIdentifier append(YangInstanceIdentifier identifier, ListSchemaNode node) {
        val keyValues = new LinkedHashMap<QName,Object>();
        if(node.keyDefinition !== null) {
            for(definition : node.keyDefinition) {
                keyValues.put(definition,new Object);
            }
        }

        return identifier.node(new NodeIdentifierWithPredicates(node.QName, keyValues));
    }


    def asXmlPath(YangInstanceIdentifier identifier) {
        return "";
    }

    def asRestconfPath(YangInstanceIdentifier identifier) {
        val it = new StringBuilder();
        append(currentModule.name)
        append(':')
        var previous = false;
        for(arg : identifier.pathArguments) {
            if(previous) append('/')
            append(arg.nodeType.localName);
            previous = true;
            if(arg instanceof NodeIdentifierWithPredicates) {
                for(qname : arg.getKeyValues.keySet) {
                    append("/{");
                    append(qname.localName)
                    append('}')
                }
            }
        }

        return it.toString;
    }

    private def String schemaPathToString(Module module, SchemaPath schemaPath, SchemaContext ctx, DataNodeContainer dataNode) {
            val List<QName> path = schemaPath.path
        val StringBuilder pathString = new StringBuilder()
        if (schemaPath.absolute) {
            pathString.append('/')
        }

        val QName qname = path.get(0)
        var Object parent = ctx.findModuleByNamespaceAndRevision(qname.namespace, qname.revision)

        for (name : path) {
            if (parent instanceof DataNodeContainer) {
                var SchemaNode node = parent.getDataChildByName(name)
                if (node == null && (parent instanceof Module)) {
                    val notifications = (parent as Module).notifications;
                    for (notification : notifications) {
                        if (notification.QName.equals(name)) {
                            node = notification
                        }
                    }
                }
                if (node == null && (parent instanceof Module)) {
                    val rpcs = (parent as Module).rpcs;
                    for (rpc : rpcs) {
                        if (rpc.QName.equals(name)) {
                            node = rpc
                        }
                    }
                }

                val pathElementModule = ctx.findModuleByNamespaceAndRevision(name.namespace, name.revision)
                val String moduleName = pathElementModule.name
                pathString.append(moduleName)
                pathString.append(':')
                pathString.append(name.localName)
                pathString.append('/')
                if(node instanceof ChoiceNode && dataNode !== null) {
                    val DataSchemaNode caseNode = dataNode.childNodes.findFirst[DataSchemaNode e | e instanceof ChoiceCaseNode];
                    if(caseNode !== null) {
                        pathString.append("(case)");
                        pathString.append(caseNode.QName.localName);
                    }
                }
                parent = node
            }
        }
        return pathString.toString;
    }


    def CharSequence childNodesInfoTree(Map<SchemaPath, DataSchemaNode> childNodes) '''
        «IF childNodes !== null && !childNodes.empty»
            «FOR child : childNodes.values»
                «childInfo(child, childNodes)»
            «ENDFOR»
        «ENDIF»
    '''

    def CharSequence childInfo(DataSchemaNode node, Map<SchemaPath, DataSchemaNode> childNodes) '''
        «val String path = nodeSchemaPathToPath(node, childNodes)»
        «IF path != null»
            «code(path)»
                «IF node !== null»
                <ul>
                «node.descAndRefLi»
                </ul>
            «ENDIF»
        «ENDIF»
    '''

    private def CharSequence treeSet(Collection<DataSchemaNode> childNodes, YangInstanceIdentifier path) '''
        «IF childNodes !== null && !childNodes.empty»
            <ul>
            «FOR child : childNodes»
                <li>
                    «child.tree(path)»
                </li>
            «ENDFOR»
            </ul>
        «ENDIF»
    '''

    def listKeys(ListSchemaNode node) '''
        [«FOR key : node.keyDefinition SEPARATOR " "»«key.localName»«ENDFOR»]
    '''

    private def CharSequence extensionInfo(ExtensionDefinition ext) '''
        <ul>
            «ext.descAndRefLi»
            «listItem("Argument", ext.argument)»
        </ul>
    '''

    private def dispatch CharSequence tree(Void obj, YangInstanceIdentifier path) '''
    '''



    /* #################### RESTRICTIONS #################### */
    private def restrictions(TypeDefinition<?> type) '''
        «type.baseType.toBaseStmt»
        «type.toLength»
        «type.toRange»
    '''

    private def dispatch toLength(TypeDefinition<?> type) {
    }

    private def dispatch toLength(BinaryTypeDefinition type) '''
        «type.lengthConstraints.toLengthStmt»
    '''

    private def dispatch toLength(StringTypeDefinition type) '''
        «type.lengthConstraints.toLengthStmt»
    '''

    private def dispatch toLength(ExtendedType type) '''
        «type.lengthConstraints.toLengthStmt»
    '''

    private def dispatch toRange(TypeDefinition<?> type) {
    }

    private def dispatch toRange(DecimalTypeDefinition type) '''
        «type.rangeConstraints.toRangeStmt»
    '''

    private def dispatch toRange(IntegerTypeDefinition type) '''
        «type.rangeConstraints.toRangeStmt»
    '''

    private def dispatch toRange(UnsignedIntegerTypeDefinition type) '''
        «type.rangeConstraints.toRangeStmt»
    '''

    private def dispatch toRange(ExtendedType type) '''
        «type.rangeConstraints.toRangeStmt»
    '''

    def toLengthStmt(Collection<LengthConstraint> lengths) '''
        «IF lengths != null && !lengths.empty»
            «listItem("Length restrictions:")»
            <ul>
            «FOR length : lengths»
                <li>
                «IF length.min == length.max»
                    «length.min»
                «ELSE»
                    &lt;«length.min», «length.max»&gt;
                «ENDIF»
                </li>
            «ENDFOR»
            </ul>
        «ENDIF»
    '''

    def toRangeStmt(Collection<RangeConstraint> ranges) '''
        «IF ranges != null && !ranges.empty»
            «listItem("Range restrictions:")»
            <ul>
            «FOR range : ranges»
                <li>
                «IF range.min == range.max»
                    «range.min»
                «ELSE»
                    &lt;«range.min», «range.max»&gt;
                «ENDIF»
                </li>
            «ENDFOR»
            </ul>
        «ENDIF»
    '''

    def toBaseStmt(TypeDefinition<?> baseType) '''
        «IF baseType != null»
        «listItem("Base type", typeAnchorLink(baseType?.path, baseType.QName.localName))»
        «ENDIF»
    '''



    /* #################### UTILITY #################### */
    private def String strong(CharSequence str) '''<strong>«str»</strong>'''
    private def italic(CharSequence str) '''<i>«str»</i>'''

    def CharSequence descAndRefLi(SchemaNode node) '''
        «listItem("Description", node.description)»
        «listItem("Reference", node.reference)»
    '''

    def CharSequence descAndRef(SchemaNode node) '''
        «node.description»
        «IF node.reference !== null»
            Reference «node.reference»
        «ENDIF»
    '''

    private def listItem(String value) '''
        «IF value !== null && !value.empty»
            <li>
                «value»
            </li>
        «ENDIF»
    '''

    private def listItem(String name, String value) '''
        «IF value !== null && !value.empty»
            <li>
                «name»: «value»
            </li>
        «ENDIF»
    '''

    private def String nodeSchemaPathToPath(DataSchemaNode node, Map<SchemaPath, DataSchemaNode> childNodes) {
        if (node instanceof ChoiceNode || node instanceof ChoiceCaseNode) {
            return null
        }

        val path = node.path.path
        val absolute = node.path.absolute;
        var StringBuilder result = new StringBuilder
        if (absolute) {
            result.append("/")
        }
        if (path !== null && !path.empty) {
            val List<QName> actual = new ArrayList()
            var i = 0;
            for (pathElement : path) {
                actual.add(pathElement)
                val DataSchemaNode nodeByPath = childNodes.get(SchemaPath.create(actual, absolute))
                if (!(nodeByPath instanceof ChoiceNode) && !(nodeByPath instanceof ChoiceCaseNode)) {
                    result.append(pathElement.localName)
                    if (i != path.size - 1) {
                        result.append("/")
                    }
                }
                i = i + 1
            }
        }
        return result.toString
    }

    private def void collectChildNodes(Collection<DataSchemaNode> source, Map<SchemaPath, DataSchemaNode> destination) {
        for (node : source) {
            destination.put(node.path, node)
            if (node instanceof DataNodeContainer) {
                collectChildNodes((node as DataNodeContainer).childNodes, destination)
            }
            if (node instanceof ChoiceNode) {
                val List<DataSchemaNode> choiceCases = new ArrayList()
                for (caseNode : node.cases) {
                    choiceCases.add(caseNode)
                }
                collectChildNodes(choiceCases, destination)
            }
        }
    }

    private def dispatch addedByInfo(SchemaNode node) '''
    '''

    private def dispatch addedByInfo(DataSchemaNode node) '''
        «IF node.augmenting»(A)«ENDIF»«IF node.addedByUses»(U)«ENDIF»
    '''

    private def dispatch isAddedBy(SchemaNode node) {
        return false;
    }

    private def dispatch isAddedBy(DataSchemaNode node) {
        if (node.augmenting || node.addedByUses) {
            return true
        } else {
            return false;
        }
    }

    private def dispatch nodeName(SchemaNode node) '''
        «IF node.isAddedBy»
            «italic(node.QName.localName)»«node.addedByInfo»
        «ELSE»
            «node.QName.localName»«node.addedByInfo»
        «ENDIF»
    '''

    private def dispatch nodeName(ContainerSchemaNode node) '''
        «IF node.isAddedBy»
            «strong(italic(node.QName.localName))»«node.addedByInfo»
        «ELSE»
            «strong(node.QName.localName)»«node.addedByInfo»
        «ENDIF»
    '''

    private def dispatch nodeName(ListSchemaNode node) '''
        «IF node.isAddedBy»
            «strong(italic(node.QName.localName))» «IF node.keyDefinition !== null && !node.keyDefinition.empty»«node.listKeys»«ENDIF»«node.addedByInfo»
        «ELSE»
            «strong(node.QName.localName)» «IF node.keyDefinition !== null && !node.keyDefinition.empty»«node.listKeys»«ENDIF»
        «ENDIF»
    '''

}
