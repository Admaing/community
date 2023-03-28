$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");
	//发送AJAX请求之前，将CSRF令牌设置到消息的请求头中
	// 后续把其他文件的csrf 控制加上，目前把 security的csrf禁用掉
	// var token = $("meta[name='_csrf']").attr("content");
	// var header = $("meta[name='_csrf_header']").attr("content");
	// $(document).ajaxSend(function (e,xhr,options){
	// 	//发请求的时候，头回携带这个数据
	// 	xhr.setRequestHeader(header,token);
	// });
	//获取标题和内容
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();

	//发送异步请求(POST)
	$.post(
		CONTEXT_PATH + "/discuss/add",
		{"title":title,"content":content},
		function (data){
			console.log(data)
			data = $.parseJSON(data);
		//	回复的消息,把消息显示到提示框
			$("#hintBody").text(data.msg);
			//显示提示框
			$("#hintModal").modal("show");
			//2秒后自动隐藏
			setTimeout(function(){
				$("#hintModal").modal("hide");
				// 刷新页面
				if (data.code==0){
					window.location.reload();
				}
			}, 2000);
		}
	);
}