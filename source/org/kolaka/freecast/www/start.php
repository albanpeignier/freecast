<?php
	$descriptor_content = FALSE;

	if (isset($_GET["descriptor"])) {
		$descriptor=$_GET["descriptor"];
		$descriptor_content = @file_get_contents('http://' . $descriptor);
	}

	if (! $descriptor_content) {
		$descriptor='download.freecast.org/jws/stable/descriptor.xml';
		$descriptor_content = @file_get_contents('http://' . $descriptor);
	}

	$lang=$_GET["lang"];
	
	$xsltproc = xslt_create();
	
	$parameters = array(
	   'lang' => 'en',
	   'descriptor' => 'arg:buffer',
	   'url' => $descriptor
	);

	$arguments = array(
	   'buffer' => $descriptor_content
	);

	$html = xslt_process(
	   $xsltproc,
	   'file://' . realpath('start.xml'),
	   'file://' . realpath('start.xsl'),
	   null,
	   $arguments,
	   $parameters
	);

	xslt_free($xsltproc);
	print $html;
?>
