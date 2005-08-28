<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xalan="http://xml.apache.org/xslt" version="1.0">

 <xsl:output method="xhtml" 
  doctype-public="-//W3C//DTD XHTML 1.1//EN" 
  doctype-system="http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd"
  indent="yes" xalan:indent-amount="4"/>
 <xsl:param name="lang"/>
 <xsl:param name="descriptor"/>
 <xsl:param name="url"/>

 <xsl:variable name="description" select="document($descriptor)"/>
 <xsl:variable name="name" select="$description/stream/name"/>

 <xsl:template match="page">
  <html xmlns="http://www.w3.org/1999/xhtml">
   <xsl:attribute name="xml:lang"><xsl:value-of select="$lang"/></xsl:attribute>
 
   <head>
    <title><xsl:text>Freecast | </xsl:text><xsl:apply-templates select="title"/> <xsl:value-of select="$name"/></title> 

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta http-equiv="refresh">
		<xsl:attribute name="content">
			<xsl:text>3; url=jnlp/descriptor=</xsl:text><xsl:value-of select="$url"/>
		</xsl:attribute>
	</meta>

    <link href="css/start.css" rel="stylesheet" type="text/css" media="screen" title="FreeCast"/>
    <link href="css/print.css" rel="stylesheet" type="text/css" media="print"/>
   </head>

   <body>
    <div id="container"> 
     <div id="branding">
      <div id="logo">
       <a href="."><img src="images/header/freecast.png" alt=""/></a>
      </div>
      <div id="tool"><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text></div>  
     </div>
   
     <xsl:apply-templates select="content"/>
    </div>
   </body>
  </html>
 </xsl:template>

 <xsl:template match="i18n">
  <xsl:choose>
   <xsl:when test="text[@lang=$lang]">
    <xsl:apply-templates select="text[@lang=$lang]"/>
   </xsl:when>
   <xsl:otherwise>
    <xsl:apply-templates select="text[@lang='en']"/>
   </xsl:otherwise>
  </xsl:choose>
 </xsl:template>
 
 <xsl:template match="content">
  <div id="content">
   <xsl:apply-templates mode="copy"/>
  </div>
 </xsl:template>

 <xsl:template match="variable" mode="copy">
  <xsl:choose>
   <xsl:when test="@name = 'name'">
    <xsl:value-of select="$name"/>
   </xsl:when>
  </xsl:choose>
 </xsl:template>
  
 <xsl:template match="@*|node()" mode="copy">
  <xsl:copy>
   <xsl:apply-templates select="@*|node()" mode="copy"/>
  </xsl:copy>
 </xsl:template>

 <xsl:template match="i18n" mode="copy">
  <xsl:choose>
   <xsl:when test="text[@lang=$lang]">
    <xsl:copy-of select="text[@lang=$lang]/node()" />
   </xsl:when>
   <xsl:otherwise>
    <xsl:copy-of select="text[@lang='en']/node()" />
   </xsl:otherwise>
  </xsl:choose>
 </xsl:template>

</xsl:stylesheet>

