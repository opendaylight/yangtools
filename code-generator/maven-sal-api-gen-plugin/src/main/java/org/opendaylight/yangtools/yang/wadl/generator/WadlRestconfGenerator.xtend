package org.opendaylight.yangtools.yang.wadl.generator

import java.io.BufferedWriter
import java.io.File
import java.io.OutputStreamWriter
import java.util.ArrayList
import java.util.HashSet
import java.util.List
import java.util.Set
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode
import org.opendaylight.yangtools.yang.model.api.Module
import org.opendaylight.yangtools.yang.model.api.SchemaContext
import org.sonatype.plexus.build.incremental.BuildContext
import org.sonatype.plexus.build.incremental.DefaultBuildContext

class WadlRestconfGenerator {
	
	File path
	static val BuildContext CTX = new DefaultBuildContext();
	var SchemaContext context;
	var List<DataSchemaNode> configData;
	var List<DataSchemaNode> operationalData;
	var Module module;
	var List<LeafSchemaNode> pathListParams;
	val PATH_DELIMETER = "/"

	new(File targetPath) {
		if (!targetPath.exists) targetPath.mkdirs
		path = targetPath
	}

	def generate(SchemaContext context, Set<Module> modules) {
        val result = new HashSet;
		this.context = context
		for (module : modules) {
			val dataContainers = module.childNodes.filter[it|it instanceof ContainerSchemaNode || it instanceof ListSchemaNode]
			if (!dataContainers.empty || !module.rpcs.nullOrEmpty) {
				configData = new ArrayList
				operationalData = new ArrayList
				
				for (data : dataContainers) {
					if (data.configuration) {
						configData.add(data)	
					} else {
						operationalData.add(data)
					}
				}
				
				this.module = module
				val destination = new File(path, '''«module.name».wadl''')
	            val fw = new OutputStreamWriter(CTX.newFileOutputStream(destination))
	            val bw = new BufferedWriter(fw)
	            bw.append(application);
	            bw.close();
	            fw.close();
		        result.add(destination)
			}
		}
		return result
	}
	
	private def application() '''
		<?xml version="1.0"?>
		<application xmlns="http://wadl.dev.java.net/2009/02" «module.importsAsNamespaces» xmlns:«module.prefix»="«module.namespace»">
		
			«grammars»
			
			«resources»
		</application>
	'''
	
	private def importsAsNamespaces(Module module) '''
		«FOR imprt : module.imports»
			xmlns:«imprt.prefix»="«context.findModuleByName(imprt.moduleName, imprt.revision).namespace»"
		«ENDFOR»
	'''
	
	private def grammars() '''
		<grammars>
			<include href="«module.name».yang"/>
			«FOR imprt : module.imports»
				<include href="«imprt.moduleName».yang"/>
			«ENDFOR»
		</grammars>
	'''
	
	private def resources() '''
		<resources base="http://localhost:9998/restconf">
			«resourceOperational»
			«resourceConfig»
			«resourceOperations»
		</resources>
	'''
	
	private def resourceOperational() '''
		«IF !operationalData.nullOrEmpty»
			<resource path="operational">
				«FOR schemaNode : operationalData»
					«schemaNode.firstResource(false)»
				«ENDFOR»
			</resource>
		«ENDIF»
	'''
	
	private def resourceConfig() '''
		«IF !configData.nullOrEmpty»
			<resource path="config">
				«FOR schemaNode : configData»
					«schemaNode.firstResource(true)»
				«ENDFOR»
			</resource>
		«ENDIF»
	'''
	
	private def resourceOperations() '''
		«IF !module.rpcs.nullOrEmpty»
			<resource path="operations">
				«FOR rpc : module.rpcs»
					<resource path="«module.name»:«rpc.QName.localName»">
						«methodPostRpc(rpc.input != null, rpc.output !== null)»
					</resource>
				«ENDFOR»
			</resource>
		«ENDIF»
	'''
	
	private def String firstResource(DataSchemaNode schemaNode, boolean config) '''
		<resource path="«module.name»:«schemaNode.createPath»">
			«IF !pathListParams.nullOrEmpty»
				«resourceParams»
			«ENDIF»
			«schemaNode.methodGet»
			«IF config»
				«schemaNode.mehodPut»
				«schemaNode.mehodPost»
			«ENDIF»
			
			«IF schemaNode instanceof DataNodeContainer»
				«val children = (schemaNode as DataNodeContainer).childNodes.filter[it|it instanceof ContainerSchemaNode || it instanceof ListSchemaNode]»
				«IF !children.empty»
					«FOR child : children»
						«child.resource(config)»
					«ENDFOR»
				«ENDIF»
			«ENDIF»
		</resource>
	'''
		
	private def String resource(DataSchemaNode schemaNode, boolean config) '''
		<resource path="«schemaNode.createPath»">
			«IF !pathListParams.nullOrEmpty»
				«resourceParams»
			«ENDIF»
			«schemaNode.methodGet»
			«IF config»
				«schemaNode.mehodPut»
				«schemaNode.mehodPost»
			«ENDIF»
			
			«IF schemaNode instanceof DataNodeContainer»
				«val children = (schemaNode as DataNodeContainer).childNodes.filter[it|it instanceof ContainerSchemaNode || it instanceof ListSchemaNode]»
				«IF !children.empty»
					«FOR child : children»
						«child.resource(config)»
					«ENDFOR»
				«ENDIF»
			«ENDIF»
		</resource>
	'''
	
	private def String createPath(DataSchemaNode schemaNode) {
		pathListParams = new ArrayList
		var StringBuilder path = new StringBuilder
		path.append(schemaNode.QName.localName)
		if (schemaNode instanceof ListSchemaNode) {
			val listKeys = (schemaNode as ListSchemaNode).keyDefinition
			for (listKey : listKeys) {
				pathListParams.add((schemaNode as DataNodeContainer).getDataChildByName(listKey) as LeafSchemaNode) 
				path.append(PATH_DELIMETER + "{" + listKey.localName + "}")
			}
		}
		return path.toString
	}
	
	private def resourceParams() '''
		«FOR pathParam : pathListParams»
		    «IF pathParam != null»
			«val prefix = pathParam.type.QName.prefix»
			«val type = if (prefix.nullOrEmpty) pathParam.type.QName.localName else prefix + ":" + pathParam.type.QName.localName»
			<param required="true" style="template" name="«pathParam.QName.localName»" type="«type»"/>
			«ENDIF»
		«ENDFOR»
	'''
	
	private def methodGet(DataSchemaNode schemaNode) '''
		<method name="GET">
			<response>
				<representation mediaType="application/xml" element="«schemaNode.QName.prefix»:«schemaNode.QName.localName»"/>
				<representation mediaType="text/xml" element="«schemaNode.QName.prefix»:«schemaNode.QName.localName»"/>
				<representation mediaType="application/json" element="«schemaNode.QName.prefix»:«schemaNode.QName.localName»"/>
				<representation mediaType="application/yang.data+xml" element="«schemaNode.QName.prefix»:«schemaNode.QName.localName»"/>
				<representation mediaType="application/yang.data+json" element="«schemaNode.QName.prefix»:«schemaNode.QName.localName»"/>
			</response>
		</method>
	'''
	
	private def mehodPut(DataSchemaNode schemaNode) '''
		<method name="PUT">
			<request>
				<representation mediaType="application/xml" element="«schemaNode.QName.prefix»:«schemaNode.QName.localName»"/>
				<representation mediaType="text/xml" element="«schemaNode.QName.prefix»:«schemaNode.QName.localName»"/>
				<representation mediaType="application/json" element="«schemaNode.QName.prefix»:«schemaNode.QName.localName»"/>
				<representation mediaType="application/yang.data+xml" element="«schemaNode.QName.prefix»:«schemaNode.QName.localName»"/>
				<representation mediaType="application/yang.data+json" element="«schemaNode.QName.prefix»:«schemaNode.QName.localName»"/>
			</request>
		</method>
	'''
	
	private def mehodPost(DataSchemaNode schemaNode) '''
		<method name="POST">
			<request>
				<representation mediaType="application/xml" element="«schemaNode.QName.prefix»:«schemaNode.QName.localName»"/>
				<representation mediaType="text/xml" element="«schemaNode.QName.prefix»:«schemaNode.QName.localName»"/>
				<representation mediaType="application/json" element="«schemaNode.QName.prefix»:«schemaNode.QName.localName»"/>
				<representation mediaType="application/yang.data+xml" element="«schemaNode.QName.prefix»:«schemaNode.QName.localName»"/>
				<representation mediaType="application/yang.data+json" element="«schemaNode.QName.prefix»:«schemaNode.QName.localName»"/>
			</request>
		</method>
	'''
	
	private def methodPostRpc(boolean input, boolean output) '''
		<method name="POST">
			«IF input»
			<request>
				<representation mediaType="application/xml" element="input"/>
				<representation mediaType="text/xml" element="input"/>
				<representation mediaType="application/json" element="input"/>
				<representation mediaType="application/yang.data+xml" element="input"/>
				<representation mediaType="application/yang.data+json" element="input"/>
			</request>
			«ENDIF»
			«IF output»
			<response>
				<representation mediaType="application/xml" element="output"/>
				<representation mediaType="text/xml" element="output"/>
				<representation mediaType="application/json" element="output"/>
				<representation mediaType="application/yang.data+xml" element="output"/>
				<representation mediaType="application/yang.data+json" element="output"/>
			</response>
			«ENDIF»
		</method>
	'''

}
