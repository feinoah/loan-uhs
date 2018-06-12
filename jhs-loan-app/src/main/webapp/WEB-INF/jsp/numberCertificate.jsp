<%@ page import="com.jhh.jhs.loan.entity.app.NoteResult" %>
<%@ page import="com.alibaba.fastjson.JSONObject" %>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://"
			+ request.getServerName() + ":" + request.getServerPort()
			+ path + "/";
	NoteResult result = (NoteResult) request.getAttribute("data");
	JSONObject data = (JSONObject) result.getData();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<head>
	<meta charset="utf-8">
	<title>数字证书服务协议（e签宝）</title>
	<meta name="viewport" content="initial-scale=1, maximum-scale=1, minimum-scale=1, user-scalable=no">
	<meta name="mobile-web-app-capable" content="yes">
	<meta name="apple-mobile-web-app-capable" content="yes">
	<meta name="apple-mobile-web-app-status-bar-style" content="black">
	<meta name="apple-mobile-web-app-title" content="vue">
	<!-- <link rel="stylesheet" type="text/css" href="style.css"/> -->
	<script>

        var oHtml = document.documentElement;
        getSize();
        function getSize(){
            // 获取屏幕的宽度
            var ascreen=oHtml.clientWidth;
            if (ascreen<=319) {
                oHtml.style.fontSize = '20px';
            } else if(ascreen>=750){
                oHtml.style.fontSize = '40px';
            }else{
                oHtml.style.fontSize=ascreen/18.75+"px";
            };
        }
        // 当窗口发生改变的时候调用
        window.onresize = function(){
            getSize();
        }
	</script>
	<script type="application/javascript" src="<%=path%>/register/jquery-1.8.0.min.js"></script>
	<base href="<%=basePath%>"></base>
	<style type="text/css">
		@charset "utf-8";
		/* 禁用iPhone中Safari的字号自动调整 */
		html {
			-webkit-text-size-adjust: 100%;
			-ms-text-size-adjust: 100%;
		}
		/* 去除iPhone中默认的input样式 */
		input[type="submit"],
		input[type="reset"],
		input[type="button"] {
			-webkit-appearance: none;
			resize: none;
		}
		/* 取消链接高亮  */
		body,div,ul,li,ol,h1,h2,h3,h4,h5,h6,input,textarea,select,p,dl,dt,dd,a,img,button,form,table,th,tr,td,tbody,article,aside,details,figcaption,figure,footer,header,hgroup,menu,nav,section {
			-webkit-tap-highlight-color: rgba(0, 0, 0, 0);
		}
		body {
			font: 12px "PingFang SC", "Helvetica Neue", Helvetica, Arial, sans-serif;
			color: #555;
			background-color: #F7F7F7;
		}
		*{
			margin:0;
			padding:0;
		}
		em,
		i {
			font-style: normal;
		}
		strong {
			font-weight: normal;
		}
		.clearfix:after {
			content: "";
			display: block;
			visibility: hidden;
			height: 0;
			clear: both;
		}
		.clearfix {
			zoom: 1;
		}
		a {
			text-decoration: none;
			color: #969696;
			font-family: "PingFang SC", "Helvetica Neue", Helvetica, Arial, sans-serif;
		}
		img {
			border: none;
		}
		html {
			font-size: 40px;
			min-width: 319px;
			max-width: 750px;
			margin: 0 auto;
		}
		.numberWrap{
			padding:0.75rem 0.75rem 1rem 0.5rem;
			background:#fff;
			color:#333;
			line-height:1rem;
			font-size:0.6rem;
			font-family: "PingFang SC", "Helvetica Neue", Helvetica, Arial, sans-serif;
		}
		.numberWrap h3{
			text-align:center;
			margin:0.5rem 0;
		}
		.numberWrap p{
			font-size:0.6rem;
			line-height:1rem;
		}

	</style>
</head>
<body>
<div class="numberWrap">
	<h3>声明</h3>
	<p>在使用悠多多平台服务之前，您应当认真阅读并遵守《数字证书服务协议》（以下简称“本协议”），请您务必审慎阅读、充分理解各条款内容，特别是免除或者限制责任的条款、争议解决和法律适用条款，您应重点阅读。如您对协议有任何疑问的，应向杭州天谷信息科技有限公司咨询。当您按照注册页面提示填写信息、阅读并同意本协议且完成全部注册程序后，或您按照激活页面提示填写信息、阅读并同意本协议且完成全部激活程序后，或您以其他悠多多平台允许的方式实际使用悠多多平台服务时，即表示您已充分阅读、理解并接受本协议的全部内容，并与杭州天谷信息科技有限公司及悠多多平台达成协议。您承诺接受并遵守本协议的约定，届时您不应以未阅读本协议的内容或者未获得融合融资租赁（上海）有限公司对您问询的解答等理由，主张本协议无效，或要求撤销本协议。</p>

	<h3>数字证书服务协议</h3>
	<p>甲方： <span><%=data == null ? "" : data.get("name") %></span></p>
	<p>证件类型：身份证</p>
	<p>证件号： <span><%=data == null ? "" : data.get("number") %></span></p>
	<p>乙方：杭州天谷信息科技有限公司</p>
	<p>地址：浙江省杭州市西湖区西斗门路3号天堂软件园E幢9层</p>
	
	<dl>
   <dt>鉴于：</dt>
   <dd>1、乙方是为互联网企业或用户推出的基于实名身份，为企业和个人提供由信息产业部认可的电子认证中心颁发的数字证书、电子签章、时间戳、电子数据保全等服务的电子签名公司，专注于为企业、政府机构和个人提供全面的电子签名服务与电子数据保全解决方案。</dd>
   <dd>2、甲方通过融合融资租赁（上海）有限公司（以下简称“悠多多”）使用数字证书获得电子签名信任服务，成为乙方电子签名体系不可分割的部分。</dd>
   <dd style="text-indent:1rem;">为进一步明确双方的权利、义务，根据《中华人民共和国电子签名法》、《电子认证服务管理办法》的规定，签订如下协议，以兹共同遵守。</dd>
   <dt>第一条	名词解释</dt>
   <dd>订户： 指委托乙方从数字认证中心获得证书的个人、组织机构。</dd>
   <dd>数字证书：是指一段信息，它至少包含了一个名字，标识特定的CA或标识特定的订户，包含了订户的公钥、证书有效期、证书序列号，及CA数字签名。</dd>
   <dd>证书私钥：数字证书包含证书本身和一个密钥对，密钥对的一部分是公钥，另一部分称为私钥。公钥公之于众，谁都可以使用。私钥只有自己知道，一般信息都是由公钥进行加密，相对应的私钥进行解密。</dd>
   <dt>第二条 本协议中的“证书”指：个人证书或企业证书。</dt>
   <dt>第三条 甲方应按照乙方规定的证书申请流程向悠多多平台提供有关资料，并保证所填写的注册信息和所提供的资料的真实性、准确性和完整性，否则悠多多平台或乙方有权拒绝甲方的申请请求。</dt>
   <dt>第四条 乙方接受甲方委托通过第三方认证中心提供的数字证书服务符合《中华人民共和国电子签名法》的相关规定。</dt>
   <dt>第五条 甲方的证书信息在证书有效期限内变更的，应当及时书面告知悠多多平台和乙方，并终止使用该证书。</dt>
   <dt>第六条 若甲方为企业用户，甲方企业分立、合并、解散、注销、宣告破产或倒闭，或被撤销营业执照等主体资格终止的，应于上述情况发生时的5个工作日内通过书面形式告知悠多多平台和乙方申请撤销数字证书，并终止使用该证书，否则，因未尽该通知义务给悠多多平台和乙方造成损失的，由甲方全部赔偿。</dt>
   <dt>第七条 甲方同意悠多多平台及乙方向有关部门和个人核实甲方的信息。悠多多平台及乙方应合法地收集、处理、传递及应用甲方的资料，按照国家有关规定及本协议的约定予以保密。</dt>
   <dt>第八条 甲方对证书享有独立的使用权。甲方使用证书产生的权利，由甲方享有；甲方使用证书产生的义务、责任，由甲方承担。</dt>
   <dt>第九条 本证书只能在数字证书有效期限内、在悠多多平台上使用。</dt>
   <dt>第十条 证书有效期限届满，甲方需要继续使用的，应当及时办理证书更新手续。</dt>
   <dt>第十一条 甲方应当妥善保管证书私钥。因甲方原因致使证书私钥泄露、损毁或者丢失的，损失由甲方承担。</dt>
   <dt>第十二条 证书私钥在证书有效期内损毁、丢失、泄露的，甲方应当及时申请办理撤销手续。撤销自手续办妥时起生效。撤销生效前发生的损失由甲方承担。</dt>
   <dt>第十三条 甲方知悉证书私钥已经丢失或者可能已经丢失时，应当及时告知悠多多平台及乙方，协助完成撤销该证书的工作，并终止使用该证书。</dt>
   <dt>第十四条 甲方有下列情形之一，乙方有权向第三方认证机构申请撤销证书并不承担任何责任。由此给乙方造成损失的，甲方应当向乙方承担赔偿责任：</dt>
  	<dd>1、甲方向悠多多平台提供的资料或者信息不真实、不完整或者不准确的。</dd>
  	<dd>2、甲方证书的信息有变更，未终止使用该证书并通知悠多多平台及乙方的。</dd>
  	<dd>3、甲方知悉证书私钥已经丢失或者可能已经丢失时，未终止使用该证书并通知悠多多平台及乙方的。</dd>
  	<dd>4、甲方超过证书的有效期限及应用范围使用证书的。</dd>
  	<dd>5、甲方公司分立、合并、解散、注销、宣告破产或倒闭，被撤销营业执照等主体资格终止的。</dd>
  	<dd>6、甲方使用证书用于违法、犯罪活动的。</dd>
   <dt>第十五条 由于第三方电子认证中心的原因导致证书私钥被破译、窃取，致使甲方遭受损失的。第三方电子认证中心应向甲方承担赔偿责任。</dt>
   <dt>第十六条 因设备故障、电力故障及通讯故障或者电脑病毒、自然灾害等因素造成甲方损失的，乙方不承担任何责任。</dt>
   <dt>第十七条 乙方将根据国家有关法律的规定，依从严谨、安全的保密原则，妥善保管甲方提交的资料。除下列情形外，乙方不会向第三方泄露甲方的资料：</dt>
		<dd>1、经过甲方同意提供的。</dd>
		<dd>2、根据执法单位的要求或为公共目的向相关单位提供的。</dd>
		<dd>3、根据有关法律、法规、证券交易所规则等要求向政府、证券交易所或其他监管机构、乙方的法律、会计、商业及其他顾问、雇员提供的。</dd>
		<dd>4、其他乙方依法应当提供的。</dd>
	 <dt>第十八条 有下列情形之一的，本协议终止：</dt>
		<dd>1、甲方证书期限届满。</dd>
		<dd>2、甲方证书被撤销。</dd>
		<dd>3、甲方向乙方申请终止本协议，乙方同意的。</dd>
		<dd>4、双方协商终止本协议的。</dd>
		<dd>5、依据法律、法规等规定，本协议应当终止的。</dd>
	 <dt>第十九条 本协议的有效期限为证书的有效期限。证书期限届满，甲方更新证书的，本协议有效期限顺延至证书更新期限届满日。</dt>
	 <dt>第二十条 甲乙双方约定，甲方通过在悠多多APP上通过包括但不限于点击、勾选、手写签名等方式之一或方式的结合确认本协议即视为甲方与乙方达成协议并同意接受本协议的全部约定内容，协议即生效。</dt>
	 <dt>第二十一条 因本协议产生的争议，不论争议金额大小，均提交杭州仲裁委员会适用杭州仲裁委员会仲裁规则项下的简易程序进行仲裁。仲裁裁决为终局的，对各方均有拘束力。</dt>
		
	</dl>
</div>
</body>
</html>
