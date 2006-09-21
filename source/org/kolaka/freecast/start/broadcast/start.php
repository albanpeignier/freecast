<?php

include('../include/start.inc');

function createjnlp($descriptor_content, $codebase, $cache_file) {
	$href=$cache_file;

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
		
	if (!$cache_handle = fopen($cache_file, 'w')) {
	  exit("cannot open jnlp cache file ($cache_file)");
	 }
	if (fwrite($cache_handle, $jnlp) === FALSE) {
	  exit("cannot write to jnlp cache file ($cache_file)");
	 }
	  
	fclose($cache_handle);

	return $cache_file;
}

$dirname = dirname($_SERVER['PHP_SELF']);
if ( $dirname{strlen($dirname)-1} != "/" ) {
  $dirname .= '/';
 }
	
$codebase="http://" . $_SERVER["HTTP_HOST"] . $dirname;

$descriptor_content = FALSE;

if (isset($_GET["descriptor"])) {
  $descriptor=$_GET["descriptor"];
  $descriptor_content = download_descriptor($descriptor);

	$id = md5($descriptor);
	$jnlp_file='../cache/freecast-' . $id . '.jnlp';

	createjnlp($descriptor_content, $codebase, $jnlp_file);
	$cache_file = $jnlp_file;
} else {
	$cache_file = 'freecast-manager.jnlp';
}

header('Location: /' . $dirname . $cache_file);
?>
