<?php
	$descriptor_content = FALSE;

	if (isset($_GET["descriptor"])) {
		$initial_descriptor=$_GET["descriptor"];
		$descriptor=$_GET["descriptor"];
		$descriptor_content = @file_get_contents('http://' . $descriptor);
		$id = md5($descriptor);
	}

	if (! $descriptor_content) {
		$descriptor='download.freecast.org/jws/stable/descriptor.xml';
		$descriptor_content = @file_get_contents('http://' . $descriptor);
		$id = "default";
	}
	
	$dirname = dirname($_SERVER['PHP_SELF']);
	if ( $dirname{strlen($dirname)-1} != "/" ) {
		$dirname .= '/';
	}
	
	$codebase="http://" . $_SERVER["HTTP_HOST"] . $dirname;
	$href="jnlp/descriptor=" . $descriptor;
	// rawurlencode($descriptor);

	$xsltproc = xslt_create();

	$parameters = array(
	   'href' => $href,
	   'codebase' => $codebase
	);

	$arguments = array(
	   '/buffer' => $descriptor_content
	);

	$jnlp = xslt_process(
	   $xsltproc,
	   'arg:/buffer',
	   'file://' . realpath('jnlp.xsl'),
	   null,
	   $arguments,
	   $parameters
	);

	xslt_free($xsltproc);
	
	$cache_file='jnlp-cache/freecast-' . md5($descriptor) . '.jnlp';
	
	if (!$cache_handle = fopen($cache_file, 'a')) {
  	exit("cannot open jnlp cache file ($cache_file)");
  }
	if (fwrite($cache_handle, $jnlp) === FALSE) {
    exit("cannot write to jnlp cache file ($cache_file)");
  }
  
  fclose($cache_handle);
	
	header('Location: ' . $cache_file);
	/*
	
	header('Content-type: application/x-java-jnlp-file');
  header('Content-Disposition: filename="freecast.jnlp"');
    
	print $jnlp;
	print "<!-- " . $descriptor . "-->";
	print "<!-- " . $initial_descriptor . "-->";
	
	*/
?>
