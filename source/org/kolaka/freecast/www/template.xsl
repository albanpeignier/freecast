<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:output method="xml" 
			  doctype-public="-//W3C//DTD XHTML 1.1//EN" 
			  doctype-system="http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd"
			  indent="yes"/>
  <xsl:param name="lang"/>

  <xsl:template match="page">
	<html>
	  <xsl:attribute name="xml:lang"><xsl:value-of select="$lang"/></xsl:attribute>
	  
	  <head>
		<title><xsl:text>Freecast - peer-to-peer streaming | </xsl:text><xsl:apply-templates select="title"/></title> 

		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="keywords" content="freecast p2p peer-to-peer radio broadcast streaming music video ogg vorbis theora java"/>
		<meta name="description" content="FreeCast is a Java application which allows peer-to-peer audio and video streaming"/>

		<link href="css/screen.css" rel="stylesheet" type="text/css" media="screen" title="FreeCast"/>
		<link href="css/print.css" rel="stylesheet" type="text/css" media="print"/>

		<link rel="icon" href="favicon.ico" type="image/x-icon"/>
		<link rel="shortcut icon" href="favicon.ico" type="image/x-icon"/>    

		<script src="start.js" type="text/javascript">
		  /* IE doesn't support an atomic script tag ... use Firefox, guys .. */
		</script>
		<script language="javascript" type="text/javascript">
		  <![CDATA[
				   function show(url,width,height) {
				   window.open(url, "show","height=" + (height+10) + ",width=" + (width+10) + ",status=no,resizable=no,scrollbars=no");
				   }
		  ]]>
		</script>
		<script src="js/java.vbs" type="text/vbscript">
		  /* IE doesn't support an atomic script tag ... use Firefox, guys .. */
		</script>
		<script src="js/java.js" type="text/javascript">
		  /* IE doesn't support an atomic script tag ... use Firefox, guys .. */
		</script>
	  </head>
	  <body>
		<div id="container"> 
		  <div id="branding">
			<div id="logo">
			  <a href="."><img src="images/header/freecast.png" alt=""/></a>
			</div>
			<div id="tool"><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text></div>  
		  </div>
		  
		  <xsl:apply-templates select="document('data/navbar.xml')"/>
		  <xsl:call-template name="menubar"/>
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
  
  <xsl:template match="navbar">
	<div id="nav">
      <ul>
		<xsl:for-each select="navitem"> 
		  <li>
			<a>
			  <xsl:attribute name="href"><xsl:value-of select="link/@href"/></xsl:attribute>
			  <xsl:apply-templates select="title"/>
			</a>
		  </li>
		</xsl:for-each>
      </ul>
	</div>
  </xsl:template>

  <xsl:template name="menubar"> 
	<div id="sidebar">
	  <div id="menu">
		<h3>
		  <img alt="" src="images/nav/list.png"/>
		  <xsl:apply-templates select="/page/title"/>
		</h3>
		<ul>
		  <xsl:for-each select="/page/content/h3">
			<li>
			  <a>
				<xsl:attribute name="href"><xsl:text>#</xsl:text><xsl:value-of select="@id"/></xsl:attribute>
				<xsl:apply-templates select="."/>
			  </a>
			</li>
		  </xsl:for-each>
		</ul>
		
		<h3><img src="images/nav/newsletters.png" alt=""/> Lists</h3>
		<ul>
		  <li><a href="http://lists.tryphon.org/wws/info/freecast-listener" title="Subscribe to the Listener mailing-list">Listener Mailing List</a></li>
		  <li><a href="http://lists.tryphon.org/wws/info/freecast-user" title="Subscribe to the User mailing-list">User Mailing List</a></li>
		</ul>
		<h3><img src="images/nav/talk.png" alt=""/> Contact</h3>
		<ul>
		  <li><a href="mailto:freecast-support@lists.tryphon.org" title="Contact Support Team">Support Team</a></li>
		  <li><a href="mailto:freecast-dev@lists.tryphon.org" title="Contact Development Team">Dev Team</a></li>
		</ul>
	  </div>
	  <p class="flags">
		<a href="/en" title="English version"><img src="images/gb.png" alt="GB"/></a>
		<a href="/fr" title="Site en Francais"><img src="images/fr.png" alt="FR"/></a>
		<a href="/es" title=""><img src="images/es.png" alt="ES"/></a>
	  </p>
	</div>
  </xsl:template>
  
  <xsl:template match="content">
	<div id="content">
	  <xsl:apply-templates mode="copy"/>
	  <div class="footer">
		<p class="licenses">
		  <a href="http://creativecommons.org/licenses/by-nc-sa/2.0/" title="Creative Commons License">CC</a> | <a href="http://validator.w3.org/check/referer" title="XHTML">XHTML</a> | <a href="http://jigsaw.w3.org/css-validator/check/referer?warning=no&amp;profile=css2" title="CSS2">CSS</a>
		</p>
	  </div>
	</div>
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

