<?php
function download_descriptor($descriptor) {
  $mirror_directory="/tmp/freecast-start";
  $wget_cmd="wget --timeout=5s --tries=1 --mirror --directory-prefix=$mirror_directory --append-output=/tmp/wget.log http://$descriptor";
  exec($wget_cmd,$output,$return);
  if ($return != 0) {
	return FALSE;
  }
  return @file_get_contents("$mirror_directory/$descriptor");
}
?>