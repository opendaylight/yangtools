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

        «groupings(module)»

        «dataStore(module)»

        «notifications(module)»

        «augmentations(module)»

        «rpcs(module)»

        «extensions(module)»

    '''


    def typeDefinitions(Module module) {
        val Set<TypeDefinition<?>> typedefs = module.typeDefinitions
        if (typedefs.empty) {
            return '';
        }
        return '''
            <h2>Type Definitions</h2>
            «list(typedefs)»

            «FOR typedef : typedefs»
            «typeDefinition(typedef)»
            «ENDFOR»
        '''
    }

    private def CharSequence typeDefinition(TypeDefinition<?> type) '''
        «header(type)»
        «body(type)»
        «restrictions(type)»
    '''

    def groupings(Module module) {
        if (module.groupings.empty) {
            return '';
        }
        return '''
            <h2>Groupings</h2>
            «list(module.groupings)»

            «FOR grouping : module.groupings»
            «headerAndBody(grouping)»
            «ENDFOR»
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

    def CharSequence headerAndBody(SchemaNode node) '''
        «header(node)»
        «body(node)»
    '''

    def header(SchemaNode type) '''
        <h3>«type.QName.localName»</h3>
    '''

    def body(SchemaNode definition) '''

        «paragraphs(definition.description)»

        «definition.reference»
    '''


    def list(Set<? extends SchemaNode> definitions) '''
        <ul>
        «FOR nodeDef : definitions» 
            <li>«nodeDef.QName.localName»</li>
        «ENDFOR»
        </ul>
    '''

    def header(Module module) '''
        <h1>«module.name»</h1>
        
        <h2>Base Information</h2>
        <dl>
            <dt>Prefix</dt>
            <dd><pre>«module.prefix»</pre></dd>
            <dt>Namespace</dt>
            <dd><pre>«module.namespace»</pre></dd>
            <dt>Revision</dt>
            <dd>«REVISION_FORMAT.format(module.revision)»</dd>
            
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
        «module.childNodes.childrenToTree»
    '''

    def dispatch CharSequence tree(DataNodeContainer node) '''
        «IF node instanceof SchemaNode»
            «(node as SchemaNode).nodeName»
        «ENDIF»
        «node.childNodes.childrenToTree»
    '''

    def dispatch CharSequence tree(DataSchemaNode node) '''
        «node.nodeName»
    '''

    def dispatch CharSequence tree(ListSchemaNode node) '''
        «node.nodeName»
        «node.childNodes.childrenToTree»
    '''

    private def CharSequence childrenToTree(Collection<DataSchemaNode> childNodes) '''
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
            «listItem("Description", augment.description)»
            «listItem("Reference", augment.reference)»
            «IF augment.whenCondition !== null»
                «listItem("When", augment.whenCondition.toString)»
            «ENDIF»
            <li>
                Path «augment.targetPath.path.pathToTree»
            </li>
            <li>
                Child nodes
                «augment.childNodes.childrenToTree»
            </li>
        </ul>
    '''

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

    def dispatch CharSequence tree(NotificationDefinition notification) '''
        <ul>
            «listItem("Description", notification.description)»
            «listItem("Reference", notification.reference)»
            <li>
                Child nodes
                «notification.childNodes.childrenToTree»
            </li>
        </ul>
    '''

    def dispatch CharSequence tree(RpcDefinition rpc) '''
        <ul>
            «listItem("Description", rpc.description)»
            «listItem("Reference", rpc.reference)»
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
            «listItem("Description", ext.description)»
            «listItem("Reference", ext.reference)»
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
            «strong("Length restrictions")»
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
            «strong("Range restrictions")»
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
    def strong(String str) '''
        <strong>«str»</strong>
    '''

    def italic(String str) '''
        <i>«str»</i>
    '''

    def pre(String string) '''<pre>«string»</pre>'''

    def paragraphs(String body) '''
        <p>«body»</p>
    '''

    def listItem(String name, String value) '''
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

    def nodeName(SchemaNode node) '''
        «IF node.isAddedBy»
            «italic(node.QName.localName)»«node.addedByInfo»
        «ELSE»
            «strong(node.QName.localName)»«node.addedByInfo»
        «ENDIF»
    '''

    def nodeName(ListSchemaNode node) '''
        «IF node.isAddedBy»
            «italic(node.QName.localName)» «IF node.keyDefinition !== null && !node.keyDefinition.empty»«node.listKeys»«ENDIF»«node.addedByInfo»
        «ELSE»
            «strong(node.QName.localName)» «IF node.keyDefinition !== null && !node.keyDefinition.empty»«node.listKeys»«ENDIF»
        «ENDIF»
    '''

}
