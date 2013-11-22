package org.opendaylight.yangtools.yang.unified.doc.generator

import org.opendaylight.yangtools.yang.model.api.SchemaContext
import java.io.File
import java.util.Set
import org.opendaylight.yangtools.yang.model.api.Module
import java.io.IOException
import java.util.HashSet
import java.io.FileWriter
import java.io.BufferedWriter
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
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema
import java.util.List
import org.opendaylight.yangtools.yang.common.QName
import org.opendaylight.yangtools.yang.model.api.RpcDefinition
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition
import java.util.ArrayList
import java.util.Map
import org.opendaylight.yangtools.yang.model.api.SchemaPath
import java.util.LinkedHashMap
import org.opendaylight.yangtools.yang.model.api.ChoiceNode
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode

class GeneratorImpl {

    File path
    static val REVISION_FORMAT = new SimpleDateFormat("yyyy-MM-dd")
    static val Logger LOG = LoggerFactory.getLogger(GeneratorImpl)


    def generate(SchemaContext context, File targetPath, Set<Module> modulesToGen) throws IOException {
        path = targetPath;
        path.mkdirs();
        val it = new HashSet;
        for (module : modulesToGen) {
            add(module.generateDocumentation());
        }
        return it;
    }

    def generateDocumentation(Module module) {
        val destination = new File(path, '''«module.name».html''')
        try {
            val fw = new FileWriter(destination)
            destination.createNewFile();
            val bw = new BufferedWriter(fw)

            bw.append(module.generate);
            bw.close();
            fw.close();
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        return destination;
    }

    def generate(Module module) '''
        <!DOCTYPE html>
        <html lang="en">
          <head>
            <title>«module.name»</title>
          </head>
          <body>
            «module.body»
          </body>
        </html>
    '''

    def body(Module module) '''
        «header(module)»

        «typeDefinitions(module)»

        «identities(module)»

        «groupings(module)»

        «childNodes(module)»

        «dataStore(module)»

        «notifications(module)»

        «augmentations(module)»

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
                    «typedef.descAndRef»
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
                    «identity.descAndRef»
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
                        «grouping.descAndRef»
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

    def augmentations(Module module) {
        if (module.augmentations.empty) {
            return '';
        }
        return '''
            <h2>Augmentations</h2>

            <ul>
            «FOR augment : module.augmentations»
                <li>
                    augment
                    «augment.tree»
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

            <ul>
            «FOR notificationdef : notificationdefs»
                <li>
                    «notificationdef.nodeName»
                    «notificationdef.tree»
                </li>
            «ENDFOR»
            </ul>
        '''
    }

    def rpcs(Module module) {
        if (module.rpcs.empty) {
            return '';
        }
        return '''
            <h2>RPC Definitions</h2>

            <ul>
            «FOR rpc : module.rpcs»
                <li>
                    «rpc.nodeName»
                    «rpc.tree»
                </li>
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

            <ul>
            «FOR ext : module.extensionSchemaNodes»
                <li>
                    «ext.nodeName»
                    «ext.tree»
                </li>
            «ENDFOR»
            </ul>
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
                        «feature.descAndRef»
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
                <dd>«pre(imp.prefix)» = «pre(imp.moduleName)»</dd>
            «ENDFOR»
        </dl>
    '''

    def process(Module module) {
        throw new UnsupportedOperationException("TODO: auto-generated method stub")
    }



    /* #################### TREE STRUCTURE #################### */
    def dispatch CharSequence tree(Module module) '''
        «strong("module " + module.name)»
        «module.childNodes.tree»
    '''

    def dispatch CharSequence tree(DataNodeContainer node) '''
        «IF node instanceof SchemaNode»
            «(node as SchemaNode).nodeName»
        «ENDIF»
        «node.childNodes.tree»
    '''

    def dispatch CharSequence tree(DataSchemaNode node) '''
        «node.nodeName»
    '''

    def dispatch CharSequence tree(ListSchemaNode node) '''
        «node.nodeName»
        «node.childNodes.tree»
    '''

    def CharSequence childNodes(Module module) '''
        «val Map<SchemaPath, DataSchemaNode> childNodes = new LinkedHashMap()»
        «collectChildNodes(module.childNodes, childNodes)»
        «IF childNodes !== null && !childNodes.empty»
            <h2>Child nodes</h2>

            «childNodes.childNodesInfoTree»
        «ENDIF»
    '''

    def CharSequence childNodesInfoTree(Map<SchemaPath, DataSchemaNode> childNodes) '''
        «IF childNodes !== null && !childNodes.empty»
            <ul>
            «FOR child : childNodes.values»
                «childInfo(child, childNodes)»
            «ENDFOR»
            </ul>
        «ENDIF»
    '''

    def CharSequence childInfo(DataSchemaNode node, Map<SchemaPath, DataSchemaNode> childNodes) '''
        «val String path = nodeSchemaPathToPath(node, childNodes)»
        «IF path != null»
            «listItem(strong(path))»
                «IF node !== null»
                <ul>
                «node.descAndRef»
                </ul>
            «ENDIF»
        «ENDIF»
    '''

    def dispatch CharSequence tree(Collection<DataSchemaNode> childNodes) '''
        «IF childNodes !== null && !childNodes.empty»
            <ul>
            «FOR child : childNodes»
                <li>
                    «child.tree»
                </li>
            «ENDFOR»
            </ul>
        «ENDIF»
    '''

    def listKeys(ListSchemaNode node) '''
        [«FOR key : node.keyDefinition SEPARATOR " "»«key.localName»«ENDFOR»]
    '''

    def dispatch CharSequence tree(AugmentationSchema augment) '''
        <ul>
            «listItem(augment.description)»
            «listItem("Reference", augment.reference)»
            «IF augment.whenCondition !== null»
                «listItem("When", augment.whenCondition.toString)»
            «ENDIF»
            <li>
                Path «augment.targetPath.path.pathToTree»
            </li>
            <li>
                Child nodes
                «augment.childNodes.tree»
            </li>
        </ul>
    '''

    def dispatch CharSequence tree(NotificationDefinition notification) '''
        <ul>
            «notification.descAndRef»
            <li>
                Child nodes
                «notification.childNodes.tree»
            </li>
        </ul>
    '''

    def dispatch CharSequence tree(RpcDefinition rpc) '''
        <ul>
            «rpc.descAndRef»
            <li>
                «rpc.input.tree»
            </li>
            <li>
                «rpc.output.tree»
            </li>
        </ul>
    '''

    def dispatch CharSequence tree(ExtensionDefinition ext) '''
        <ul>
            «ext.descAndRef»
            «listItem("Argument", ext.argument)»
        </ul>
    '''



    /* #################### RESTRICTIONS #################### */
    private def restrictions(TypeDefinition<?> type) '''
        «type.toLength»
        «type.toRange»
    '''

    def dispatch toLength(TypeDefinition<?> type) {
    }

    def dispatch toLength(BinaryTypeDefinition type) '''
        «type.lengthConstraints.toLengthStmt»
    '''

    def dispatch toLength(StringTypeDefinition type) '''
        «type.lengthConstraints.toLengthStmt»
    '''

    def dispatch toLength(ExtendedType type) '''
        «type.lengthConstraints.toLengthStmt»
    '''

    def dispatch toRange(TypeDefinition<?> type) {
    }

    def dispatch toRange(DecimalTypeDefinition type) '''
        «type.rangeConstraints.toRangeStmt»
    '''

    def dispatch toRange(IntegerTypeDefinition type) '''
        «type.rangeConstraints.toRangeStmt»
    '''

    def dispatch toRange(UnsignedIntegerTypeDefinition type) '''
        «type.rangeConstraints.toRangeStmt»
    '''

    def dispatch toRange(ExtendedType type) '''
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
    private def String strong(String str) '''<strong>«str»</strong>'''
    private def italic(String str) '''<i>«str»</i>'''
    private def pre(String str) '''<pre>«str»</pre>'''

    def CharSequence descAndRef(SchemaNode node) '''
        «listItem(node.description)»
        «listItem("Reference", node.reference)»
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
                «name»
                <ul>
                    <li>
                        «value»
                    </li>
                </ul>
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

    private def CharSequence pathToTree(List<QName> path) '''
        «IF path !== null && !path.empty»
            <ul>
            «FOR pathElement : path»
                <li>
                    «pathElement.namespace» «pathElement.localName»
                </li>
            «ENDFOR»
            </ul>
        «ENDIF»
    '''

    def dispatch addedByInfo(SchemaNode node) '''
    '''

    def dispatch addedByInfo(DataSchemaNode node) '''
        «IF node.augmenting»(A)«ENDIF»«IF node.addedByUses»(U)«ENDIF»
    '''

    def dispatch isAddedBy(SchemaNode node) {
        return false;
    }

    def dispatch isAddedBy(DataSchemaNode node) {
        if (node.augmenting || node.addedByUses) {
            return true
        } else {
            return false;
        }
    }

    def dispatch nodeName(SchemaNode node) '''
        «IF node.isAddedBy»
            «italic(node.QName.localName)»«node.addedByInfo»
        «ELSE»
            «strong(node.QName.localName)»«node.addedByInfo»
        «ENDIF»
    '''

    def dispatch nodeName(ListSchemaNode node) '''
        «IF node.isAddedBy»
            «italic(node.QName.localName)» «IF node.keyDefinition !== null && !node.keyDefinition.empty»«node.listKeys»«ENDIF»«node.addedByInfo»
        «ELSE»
            «strong(node.QName.localName)» «IF node.keyDefinition !== null && !node.keyDefinition.empty»«node.listKeys»«ENDIF»
        «ENDIF»
    '''

}
