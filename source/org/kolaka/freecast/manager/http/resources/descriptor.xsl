<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:output method="xml" indent="yes"/>

	<xsl:param name="homepage"/>
	<xsl:param name="version"/>
	<xsl:param name="host"/>

	<xsl:template match="/">
		<stream>
			<name>FreeCast Test Network</name>
			<homepage>
				<xsl:value-of select="$homepage"/>
			</homepage>

			<jnlp>
				<version>
					<xsl:value-of select="$version"/>
				</version>
				<config>
					<xsl:value-of select="$homepage"/>
					<xsl:text>/config.xml</xsl:text>
				</config>
			</jnlp>
		</stream>
	</xsl:template>
</xsl:stylesheet>