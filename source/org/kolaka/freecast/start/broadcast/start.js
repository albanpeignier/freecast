function click_and_broadcast(descriptor) {
	url='http://@start.site@/broadcast/start.php';
	if (descriptor) url+='?descriptor='+ descriptor;
	window.location=url;
}
