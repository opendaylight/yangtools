package org.opendaylight.yangtools.yang.unified.doc.generator

import org.opendaylight.yangtools.yang.model.api.SchemaContext
import java.io.File
import java.util.Set
import org.opendaylight.yangtools.yang.model.api.Module
import java.io.IOException
import java.util.HashSet
import java.io.BufferedWriter
import java.io.OutputStreamWriter;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode
import org.opendaylight.yangtools.yang.model.api.TypeDefinition
import org.opendaylight.yangtools.yang.model.api.SchemaNode
import org.opendaylight.yangtools.yang.model.util.ExtendedType
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition
import java.text.SimpleDateFormat
import java.util.Collection
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer
import org.slf4j.LoggerFactory
import org.slf4j.Logger
import java.util.List
import org.opendaylight.yangtools.yang.common.QName
import org.opendaylight.yangtools.yang.model.api.RpcDefinition
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition
import java.util.ArrayList
import java.util.Map
import org.opendaylight.yangtools.yang.model.api.SchemaPath

import org.sonatype.plexus.build.incremental.BuildContext;
import org.sonatype.plexus.build.incremental.DefaultBuildContext;

import org.opendaylight.yangtools.yang.model.api.ChoiceNode
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeIdentifierWithPredicates
import java.util.LinkedHashMap
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeIdentifier
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNodeimport java.util.HashMap
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode

class GeneratorImpl {

    File path
    static val REVISION_FORMAT = new SimpleDateFormat("yyyy-MM-dd")
    static val Logger LOG = LoggerFactory.getLogger(GeneratorImpl)
    static val BuildContext CTX = new DefaultBuildContext();
    var Module currentModule;


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
                    «strong("typedef " + typedef.QName.localName)»
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
                    «strong("identity " + identity.QName.localName)»
                    <ul>
                    «identity.descAndRefLi»
                    «IF identity.baseIdentity != null»
                        «listItem("base", identity.baseIdentity.QName.localName)»
                    «ENDIF»
                    </ul>
                </li>
            «ENDFOR»
            </ul>
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
                    «strong("grouping " + grouping.QName.localName)»
                    <ul>
                        «grouping.descAndRefLi»
                    </ul>
                </li>
            «ENDFOR»
            </ul>
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
                    <h3>Target [«schemaPathAsRestconfPath(module, augment.targetPath, context)»]</h3>
                    «augment.description»
                    «IF augment.reference !== null»
                        Reference «augment.reference»
                    «ENDIF»
                    «IF augment.whenCondition !== null»
                        When «augment.whenCondition.toString»
                    «ENDIF»
                    «augment.childNodes.printChildren(3,InstanceIdentifier.builder().toInstance())»
                </li>
            «ENDFOR»
            </ul>
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

                <h3>«notificationdef.nodeName»</h3>
                    «notificationdef.descAndRef»
                    «notificationdef.childNodes.printChildren(3,InstanceIdentifier.builder().toInstance())»
            «ENDFOR»
        '''
    }

    def rpcs(Module module) {
        if (module.rpcs.empty) {
            return '';
        }

        return '''
            <h2>RPC Definitions</h2>
            «FOR rpc : module.rpcs»
                <h3>«rpc.nodeName»</h3>
                    «rpc.rpcInfo(InstanceIdentifier.builder().node(rpc.QName).toInstance())»
            «ENDFOR»
            </ul>
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
                    <h3>«ext.nodeName»</h3>
                </li>
                «extensionInfo(ext)»
            «ENDFOR»
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
                    «strong("feature " + feature.QName.localName)»
                    <ul>
                        «feature.descAndRefLi»
                    </ul>
                </li>
            «ENDFOR»
            </ul>
        '''
    }

    def header(Module module) '''
        <h1>«module.name»</h1>

        <h2>Base Information</h2>
        <dl>
            <dt>Prefix</dt>
            <dd>«pre(module.prefix)»</dd>
            <dt>Namespace</dt>
            <dd>«pre(module.namespace.toString)»</dd>
            <dt>Revision</dt>
            <dd>«pre(REVISION_FORMAT.format(module.revision))»</dd>

            «FOR imp : module.imports BEFORE "<dt>Imports</dt>" »
                <dd>«code(imp.prefix)» = «code(imp.moduleName)»</dd>
            «ENDFOR»
        </dl>
    '''

    def code(String string) '''<code>«string»</code>'''

    def process(Module module) {
        throw new UnsupportedOperationException("TODO: auto-generated method stub")
    }

    def CharSequence tree(Module module) '''
        «strong("module " + module.name)»
        «module.childNodes.treeSet(InstanceIdentifier.builder.toInstance())»
    '''

    private def dispatch CharSequence tree(ChoiceNode node,InstanceIdentifier path) '''
        «node.nodeName» (choice)
        «casesTree(node.cases,path)»
    '''

    def casesTree(Set<ChoiceCaseNode> nodes,InstanceIdentifier path) '''
        <ul>
        «FOR node : nodes»
            <li>
            «node.nodeName»
            «node.childNodes.treeSet(path)»
            </li>
        «ENDFOR»
        </ul>
    '''

    private def dispatch CharSequence tree(DataSchemaNode node,InstanceIdentifier path) '''
        «node.nodeName»
    '''

    private def dispatch CharSequence tree(ListSchemaNode node,InstanceIdentifier path) '''
        «val newPath = path.append(node)»
        «localLink(newPath,node.nodeName)»
        «node.childNodes.treeSet(newPath)»
    '''

    private def dispatch CharSequence tree(ContainerSchemaNode node,InstanceIdentifier path) '''
        «val newPath = path.append(node)»
        «localLink(newPath,node.nodeName)»
        «node.childNodes.treeSet(newPath)»
    '''

    def CharSequence childNodes(Module module) '''
        «val childNodes = module.childNodes»
        «IF childNodes !== null && !childNodes.empty»
            <h2>Child nodes</h2>

            «childNodes.printChildren(3,InstanceIdentifier.builder().toInstance())»
        «ENDIF»
    '''

    def CharSequence printChildren(Set<DataSchemaNode> nodes, int level, InstanceIdentifier path) {
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

        «IF !path.path.empty»
        <h3>XML example</h3>
        «nodes.xmlExample(path.path.last.nodeType,path)»
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

    def CharSequence xmlExample(Set<DataSchemaNode> nodes, QName name,InstanceIdentifier path) '''
    <pre>
        «xmlExampleTag(name,nodes.xmplExampleTags(path))»
    </pre>
    '''

    def CharSequence xmplExampleTags(Set<DataSchemaNode> nodes, InstanceIdentifier identifier) '''
        <!-- Child nodes -->
        «FOR node : nodes»
        <!-- «node.QName.localName» -->
            «node.asXmlExampleTag(identifier)»
        «ENDFOR»

    '''

    private def dispatch CharSequence asXmlExampleTag(LeafSchemaNode node, InstanceIdentifier identifier) '''
        «node.QName.xmlExampleTag("...")»
    '''

    private def dispatch CharSequence asXmlExampleTag(LeafListSchemaNode node, InstanceIdentifier identifier) '''
        &lt!-- This node could appear multiple times --&gt
        «node.QName.xmlExampleTag("...")»
    '''

    private def dispatch CharSequence asXmlExampleTag(ContainerSchemaNode node, InstanceIdentifier identifier) '''
        &lt!-- See «localLink(identifier.append(node),"definition")» for child nodes.  --&gt
        «node.QName.xmlExampleTag("...")»
    '''


    private def dispatch CharSequence asXmlExampleTag(ListSchemaNode node, InstanceIdentifier identifier) '''
        &lt!-- See «localLink(identifier.append(node),"definition")» for child nodes.  --&gt
        &lt!-- This node could appear multiple times --&gt
        «node.QName.xmlExampleTag("...")»
    '''


    private def dispatch CharSequence asXmlExampleTag(DataSchemaNode node, InstanceIdentifier identifier) '''
        <!-- noop -->
    '''


    def xmlExampleTag(QName name, CharSequence data) {
        return '''&lt;«name.localName» xmlns="«name.namespace»"&gt;«data»&lt;/«name.localName»&gt;'''
    }

    def header(int level,QName name) '''<h«level»>«name.localName»</h«level»>'''


    def header(int level,InstanceIdentifier name) '''
        <h«level» id="«FOR cmp : name.path SEPARATOR "/"»«cmp.nodeType.localName»«ENDFOR»">
            «FOR cmp : name.path SEPARATOR "/"»«cmp.nodeType.localName»«ENDFOR»
        </h«level»>
    '''



    private def dispatch CharSequence printInfo(DataSchemaNode node, int level, InstanceIdentifier path) '''
        «header(level+1,node.QName)»
    '''

    private def dispatch CharSequence printInfo(ContainerSchemaNode node, int level, InstanceIdentifier path) '''
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

    private def dispatch CharSequence printInfo(ListSchemaNode node, int level, InstanceIdentifier path) '''
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

    private def dispatch CharSequence printInfo(ChoiceNode node, int level, InstanceIdentifier path) '''
        «val Set<DataSchemaNode> choiceCases = new HashSet(node.cases)»
        «choiceCases.printChildren(level,path)»
    '''

    private def dispatch CharSequence printInfo(ChoiceCaseNode node, int level, InstanceIdentifier path) '''
        «node.childNodes.printChildren(level,path)»
    '''



    def CharSequence printShortInfo(ContainerSchemaNode node, int level, InstanceIdentifier path) {
        val newPath = path.append(node);
        return '''
            <li>«strong(localLink(newPath,node.QName.localName))» (container)</li>
        '''
    }

    def CharSequence printShortInfo(ListSchemaNode node, int level, InstanceIdentifier path) {
        val newPath = path.append(node);
        return '''
            <li>«strong(localLink(newPath,node.QName.localName))» (list)</li>
        '''
    }

    def CharSequence printShortInfo(AnyXmlSchemaNode node, int level, InstanceIdentifier path) {
        return '''
            <li>«strong((node.QName.localName))» (anyxml)</li>
        '''
    }

    def CharSequence printShortInfo(LeafSchemaNode node, int level, InstanceIdentifier path) {
        return '''
            <li>«strong((node.QName.localName))» (leaf)</li>
        '''
    }

    def CharSequence printShortInfo(LeafListSchemaNode node, int level, InstanceIdentifier path) {
        return '''
            <li>«strong((node.QName.localName))» (leaf-list)</li>
        '''
    }

    def CharSequence localLink(InstanceIdentifier identifier, CharSequence text) '''
        <a href="#«FOR cmp : identifier.path SEPARATOR "/"»«cmp.nodeType.localName»«ENDFOR»">«text»</a>
    '''


    private def dispatch InstanceIdentifier append(InstanceIdentifier identifier, ContainerSchemaNode node) {
        val pathArguments = new ArrayList(identifier.path)
        pathArguments.add(new NodeIdentifier(node.QName));
        return new InstanceIdentifier(pathArguments);
    }

    private def dispatch InstanceIdentifier append(InstanceIdentifier identifier, ListSchemaNode node) {
        val pathArguments = new ArrayList(identifier.path)
        val keyValues = new LinkedHashMap<QName,Object>();
        if(node.keyDefinition != null) {
            for(definition : node.keyDefinition) {
                keyValues.put(definition,new Object);
            }
        }
        pathArguments.add(new NodeIdentifierWithPredicates(node.QName,keyValues));
        return new InstanceIdentifier(pathArguments);
    }


    def asXmlPath(InstanceIdentifier identifier) {
        return "";
    }

    def asRestconfPath(InstanceIdentifier identifier) {
        val it = new StringBuilder();
        append(currentModule.name)
        append(":")
        var previous = false;
        for(arg : identifier.path) {
            if(previous) append("/")
            append(arg.nodeType.localName);
            previous = true;
            if(arg instanceof NodeIdentifierWithPredicates) {
                val nodeIdentifier = arg as NodeIdentifierWithPredicates;
                for(qname : nodeIdentifier.keyValues.keySet) {
                    append("/{");
                    append(qname.localName)
                    append("}")
                }
            }
        }

        return it.toString;
    }

    private def String schemaPathAsRestconfPath(Module module, SchemaPath schemaPath, SchemaContext ctx) {
        val Map<String, String> imports = new HashMap();
        for (mImport : module.imports) {
            imports.put(mImport.prefix, mImport.moduleName)
        }

        val List<QName> path = schemaPath.path
        val StringBuilder pathString = new StringBuilder()
        if (schemaPath.absolute) {
            pathString.append("/")
        }

        val QName qname = path.get(0)
        var Object parent = ctx.findModuleByNamespaceAndRevision(qname.namespace, qname.revision)

        for (name : path) {
            if (parent instanceof DataNodeContainer) {
                var SchemaNode node = (parent as DataNodeContainer).getDataChildByName(name)
                if (node == null && (parent instanceof Module)) {
                    val notifications = (parent as Module).notifications;
                    for (notification : notifications) {
                        if (notification.QName.localName.equals(name.localName)) {
                            node = notification
                        }
                    }
                }

                if (!(node instanceof ChoiceNode) && !(node instanceof ChoiceCaseNode)) {
                    var String prefix = node.QName.prefix
                    var String moduleName
                    if (prefix == null || "".equals(prefix) || prefix.equals(module.prefix)) {
                        moduleName = module.name
                    } else {
                        moduleName = imports.get(prefix)
                    }
                    pathString.append(moduleName)
                    pathString.append(":")
                    pathString.append(node.QName.localName)
                    pathString.append("/")
                }
                parent = node
            } else if (parent instanceof ChoiceNode) {
                parent = (parent as ChoiceNode).getCaseNodeByName(qname.localName)
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

    private def CharSequence treeSet(Collection<DataSchemaNode> childNodes, InstanceIdentifier path) '''
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

    private def CharSequence rpcInfo(RpcDefinition rpc,InstanceIdentifier path) '''
        <ul>
            «rpc.descAndRefLi»
            <li>
                «rpc.input.tree(path)»
            </li>
            <li>
                «rpc.output.tree(path)»
            </li>
        </ul>
    '''

    private def CharSequence extensionInfo(ExtensionDefinition ext) '''
        <ul>
            «ext.descAndRefLi»
            «listItem("Argument", ext.argument)»
        </ul>
    '''

    private def dispatch CharSequence tree(Void obj, InstanceIdentifier path) '''
    '''



    /* #################### RESTRICTIONS #################### */
    private def restrictions(TypeDefinition<?> type) '''
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
            «listItem("Length restrictions")»
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
            «listItem("Range restrictions")»
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



    /* #################### UTILITY #################### */
    private def String strong(CharSequence str) '''<strong>«str»</strong>'''
    private def italic(CharSequence str) '''<i>«str»</i>'''
    private def pre(CharSequence str) '''<pre>«str»</pre>'''

    def CharSequence descAndRefLi(SchemaNode node) '''
        «listItem(node.description)»
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
                val DataSchemaNode nodeByPath = childNodes.get(new SchemaPath(actual, absolute)) 
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
                for (caseNode : (node as ChoiceNode).cases) {
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
