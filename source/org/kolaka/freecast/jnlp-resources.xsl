<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:output method="xml" indent="yes"/>
  
  <xsl:param name="project.version"/>

	<xsl:template match="/">
		<xsl:variable name="jdic.version">
			<xsl:value-of select="modules/module[@name='jdic']/@rev"/>
		</xsl:variable>
	  <resources>
			<jar href="lib/freecast.jar">
				<xsl:attribute name="version"><xsl:value-of select="$project.version"/></xsl:attribute>
			</jar>
			<jar href="lib/freecast-etc.jar">
				<xsl:attribute name="version"><xsl:value-of select="$project.version"/></xsl:attribute>
			</jar>
			
		  	<xsl:for-each select="modules/module/artifact">
		  		<xsl:if test="not(contains(@name,'jdic'))">
		  			<jar>
		  				<xsl:attribute name="href">
		  					<xsl:text>lib/</xsl:text>
		  					<xsl:value-of select="@name"/>
		  					<xsl:text>.</xsl:text>
		  					<xsl:value-of select="@type"/>
		  				</xsl:attribute>
		  				<xsl:attribute name="version">
								<xsl:value-of select="../@rev"/>    				
		  				</xsl:attribute>
		  			</jar>
		  		</xsl:if>
		  	</xsl:for-each>
	  	<jar href="lib/jdic.jar">
	  		<xsl:attribute name="version">
	  			<xsl:value-of select="$jdic.version"/>
	  		</xsl:attribute>
	  	</jar>
	  </resources>
	  <resources os="Windows">
	    <jar href="lib/windows/jdic_stub.jar">
	  		<xsl:attribute name="version"><xsl:value-of select="$jdic.version"/></xsl:attribute>
			</jar>		    
	    <nativelib href="lib/windows/jdic_native_x86.jar">
	  		<xsl:attribute name="version"><xsl:value-of select="$jdic.version"/></xsl:attribute>
	    </nativelib>
	  </resources>
	  <resources os="SunOS" arch="sparc">
	    <jar href="lib/sunos/jdic_stub.jar">
	  		<xsl:attribute name="version"><xsl:value-of select="$jdic.version"/></xsl:attribute>
	    </jar>
	    <nativelib href="lib/sunos/jdic_native_sparc.jar">
	  		<xsl:attribute name="version"><xsl:value-of select="$jdic.version"/></xsl:attribute>
	    </nativelib>
	  </resources>
	  <resources os="SunOS" arch="x86">
	    <jar href="lib/sunos/jdic_stub.jar" />
	    <nativelib href="lib/sunos/jdic_native_x86.jar" />
	  </resources>
	  <resources os="Linux">
	    <jar href="lib/linux/jdic_stub.jar" />
	    <nativelib href="lib/linux/jdic_native_x86.jar" />
	  </resources>
	</xsl:template>

</xsl:stylesheet>
