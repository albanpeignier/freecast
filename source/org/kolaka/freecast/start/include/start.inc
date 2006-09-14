<?php
function download_descriptor($descriptor) {
  $wget_cmd="wget --timeout=5s --tries=1 -O - -q http://$descriptor";
  exec($wget_cmd,$output,$return);
  if ($return != 0) {
	return FALSE;
  }
  return join("\n", $output);
}
?>
