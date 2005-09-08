<?xml version='1.0'?>
<xsl:stylesheet  
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"> 

	<xsl:import href="@docbook.xsl.dir@/html/chunk.xsl"/> 

	<!--
		<xsl:param name="html.stylesheet" select="'corpstyle.css'"/> 
		<xsl:param name="admon.graphics" select="1"/>
	-->

	<xsl:template name="tr.attributes">
  		<xsl:param name="row" select="."/>
		<xsl:param name="rownum" select="0"/>

		<xsl:if test="$rownum mod 2 = 0">
  			<xsl:attribute name="class">oddrow</xsl:attribute>
		</xsl:if>
	</xsl:template>
	
</xsl:stylesheet> 

