<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

 <xsl:output method="xml" indent="yes" />

 <xsl:param name="href"/>
 <xsl:param name="codebase"/>

	<xsl:variable name="name" select="/stream/name"/>
	<xsl:variable name="version">
		<xsl:choose>
			<xsl:when test="boolean(/stream/jnlp/version)">
				<xsl:value-of select="/stream/jnlp/version"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>stable</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:variable name="libfreecast_baseurl">
		<xsl:text>http://jws.freecast.org/</xsl:text>
		<xsl:value-of select="$version"/>
		<xsl:text>/</xsl:text>
	</xsl:variable>

 <xsl:template match="/">
	<jnlp spec="1.0+">
		<xsl:attribute name="codebase"><xsl:value-of select="$codebase"/></xsl:attribute>
		<xsl:attribute name="href"><xsl:value-of select="$href"/></xsl:attribute>
					  
	  <information>
	    <title><xsl:text>FreeCast - </xsl:text><xsl:value-of select="$name"/></title> 
	    <vendor>Kolaka Team</vendor> 
	    <homepage> 
	    	<xsl:attribute name="href"><xsl:value-of select="/stream/homepage"/></xsl:attribute>
	    </homepage>
	    
	    <description><xsl:value-of select="$name"/></description>
	    <icon href="http://download.freecast.org/jws/default/freecast-icon-64.gif"/>
	    <icon kind="splash" href="http://download.freecast.org/jws/default/freecast-splash.jpg"/>
	  </information> 
	
	  <security> 
	    <all-permissions/>
	  </security> 
	  
	  <resources> 
	    <j2se version="1.4+"/>
	    <jar main="true">
		    	<xsl:attribute name="href">
		    		<xsl:value-of select="$libfreecast_baseurl"/>
		    		<xsl:text>lib/freecast-bootstrap.jar</xsl:text>
		    	</xsl:attribute>
			</jar>
			
	    <extension name="libfreecast">
	    	<xsl:attribute name="href">
	    		<xsl:value-of select="$libfreecast_baseurl"/>
	    		<xsl:text>libfreecast.jnlp</xsl:text>
	    	</xsl:attribute>
	    </extension>
	  </resources>
	
	  <application-desc main-class="org.kolaka.freecast.node.swing.Bootstrap">
			<xsl:if test="/stream/jnlp/config">
				<argument>
					<xsl:text>-config</xsl:text>
				</argument>
				<argument>
					<xsl:value-of select="/stream/jnlp/config"/>
				</argument>
			</xsl:if>
			<xsl:if test="/stream/tracker/host">
				<argument>
					<xsl:text>-Dnode.peerprovider.trackeraddress.host=</xsl:text><xsl:value-of select="/stream/tracker/host"/>
				</argument>
			</xsl:if>
			<xsl:if test="/stream/tracker/port">
				<argument>
					<xsl:text>-Dnode.peerprovider.trackeraddress.port=</xsl:text><xsl:value-of select="/stream/tracker/port"/>
				</argument>
			</xsl:if>
	  </application-desc>
	</jnlp>
 </xsl:template>
 
</xsl:stylesheet>
