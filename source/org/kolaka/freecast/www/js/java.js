var javawsInstalled = 0;
var isIE = "false";
if (navigator.mimeTypes && navigator.mimeTypes.length) {
	var x = navigator.mimeTypes['application/x-java-jnlp-file'];
	if (x) javawsInstalled = 1;
} else { 
	isIE = "true";
}

function changecss(theclass,element,value) {
	var cssRules;
	if (document.all) {
		cssRules = 'rules';
	}
	else if (document.getElementById) {
		cssRules = 'cssRules';
	}
	for (var S = 0; S < document.styleSheets.length; S++){
		for (var R = 0; R < document.styleSheets[S][cssRules].length; R++) {
			if (document.styleSheets[S][cssRules][R].selectorText == theclass) {
				document.styleSheets[S][cssRules][R].style[element] = value;
			}
		}
	}
}

if (javawsInstalled) {
	changecss('.java','display','inline');
	changecss('.nojava','display','none');
}


