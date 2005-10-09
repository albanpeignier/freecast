<?php

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

	function download_descriptor($descriptor) {
			 $mirror_directory="/tmp/freecast-start";
			 $wget_cmd="wget --timeout=5s --tries=1 --mirror --directory-prefix=$mirror_directory --append-output=/tmp/wget.log http://$descriptor";
			 exec($wget_cmd,$output,$return);
			 if ($return != 0) {
			 	return FALSE;
		     }
 			 return @file_get_contents("$mirror_directory/$descriptor");
	}

	$descriptor_content = FALSE;

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
	   $template='start.xml';
	   $parameters['autostart'] = 'true';
	   $parameters['descriptor'] = 'arg:buffer';
	   $arguments['buffer']=$descriptor_content;
	} else {
	   $template='start-nodescriptor.xml';		
	   $parameters['autostart'] = 'false';
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
