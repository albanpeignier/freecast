<?php

include('../include/start.inc');

function createjnlp($descriptor_content,$cache_file) {
		
	$dirname = dirname($_SERVER['PHP_SELF']);
	if ( $dirname{strlen($dirname)-1} != "/" ) {
	  $dirname .= '/';
	 }
		
	$codebase="http://" . $_SERVER["HTTP_HOST"] . $dirname;
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

function chooseLang($availableLangs) {
  $pref=array();
  foreach(split(',', $_SERVER["HTTP_ACCEPT_LANGUAGE"]) as $lang) {
	if (preg_match('/^([a-z]+).*?(?:;q=([0-9.]+))?/i', $lang.';q=1.0', $split)) {
	  $pref[sprintf("%f%d", $split[2], rand(0,9999))]=strtolower($split[1]);       
	}
  }
  krsort($pref);
  return array_shift(array_merge(array_intersect($pref, $availableLangs), $availableLangs));
}

$descriptor_content = FALSE;

$id = md5($descriptor);
$jnlp_file='../cache/freecast-' . $id . '.jnlp';

if (isset($_GET["descriptor"])) {
  $descriptor=$_GET["descriptor"];
  $descriptor_content = download_descriptor($descriptor);
 }

$lang='en';
if (isset($_GET["lang"])) {
  $lang=$_GET["lang"];
 } else {
  $lang=chooseLang(array('en','fr'));
 }

$parameters = array(
					'lang' => $lang,
					'url' => $descriptor
					);

$arguments = array();

if ($descriptor_content) {
  createjnlp($descriptor_content, $jnlp_file);
  $template='start.xml';
  
  $parameters['jnlp_file'] = $jnlp_file;
  $parameters['descriptor'] = 'arg:buffer';
  $arguments['buffer']=$descriptor_content;
 } else {
  $template='start-nodescriptor.xml';		
 }	

$xsltproc = xslt_create();
$html = xslt_process(
					 $xsltproc,
					 'file://' . realpath($template),
					 'file://' . realpath('start.xsl'),
					 null,
					 $arguments,
					 $parameters
					 );

xslt_free($xsltproc);
print $html;
?>
