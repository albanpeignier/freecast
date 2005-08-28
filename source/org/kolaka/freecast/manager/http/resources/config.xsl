<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:output method="xml" indent="yes"/>

	<xsl:param name="host"/>

	<xsl:template match="/">
		<freecast>
			<node>
				<peerprovider>
					<trackeraddress>
						<host>
							<xsl:value-of select="$host"/>
						</host>
					</trackeraddress>
				</peerprovider>
			</node>
		</freecast>
	</xsl:template>
</xsl:stylesheet>