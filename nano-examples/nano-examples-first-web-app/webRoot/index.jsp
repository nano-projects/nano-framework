<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<script type="text/javascript" src="js/jquery.min.js"></script>
<script type="text/javascript">
$.ajax({
	url : '/first-webapp/first/hello/123' , 
	type : "PUT" ,
	contentType: "application/x-www-form-urlencoded; charset=utf-8" , 
	data : {
		name: 'postName'
	},
	success : function(data) {
		console.log(data);
		$('#context')[0].innerHTML = JSON.stringify(data);
	}
});

</script>
<body>
	<div id="context"></div>
</body>
</html>
