<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
<script type="text/javascript" src="../js/jquery.min.js"></script>
<script type="text/javascript">
$().ready(function() {
	

	var q = location.search.substr(1);
	var qs = q.split("&");
	if (qs) {
		try {
			for (var i = 0; i < qs.length; i++) {
				var param = qs[i].split("=");
				$('#' + param[0])[0].innerHTML = decodeURI(param[1]);
			}
		} catch(error) {
			console.log(error);
		}
	}
})
</script>
</head>
<body>
	<div id="value"></div>
</body>
</html>