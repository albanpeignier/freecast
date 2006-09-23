function start(descriptor,lang) {
	click_and_listen(descriptor,lang);
}

function click_and_listen(descriptor,lang) {
	window.open('http://@start.site@/listen/start.php?descriptor='+ descriptor + '&lang=' + lang, 'freecast','height=400,width=600');
}
