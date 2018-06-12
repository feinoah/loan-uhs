<%--
  Created by IntelliJ IDEA.
  User: jiebaoqiang
  Date: 2018/1/23
  Time: 16:32
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE html>
<html>
<head>
    <base href="<%=basePath%>">
    <title>手机出售及回购协议</title>
    <meta name="viewport" content="initial-scale=1, maximum-scale=1, minimum-scale=1, user-scalable=no">
    <meta name="mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta name="apple-mobile-web-app-title" content="vue">
    <script src="https://uhuishou.ronghezulin.com/loan-app/js/jquery-1.8.0.min.js" type="text/javascript"></script>
</head>
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
    .phoneWrap{
        padding:0.75rem 0.75rem 1rem 0.5rem;
        background:#fff;
        color:#333;
        line-height:1rem;
        font-size:0.6rem;
        font-family: "PingFang SC", "Helvetica Neue", Helvetica, Arial, sans-serif;
    }
    .phoneWrap h4{
        text-align:center;
        font-weight:700;
        font-size:0.7rem;
    }
    .shortUd{
        padding:0 0.5rem ;
        display: inline-block;
        line-height:0.75rem;
        border-bottom: 1px solid #666;
    }
    .longUd{
        padding:0 0.5rem ;
        display: inline-block;
        line-height:0.75rem;
        border-bottom: 1px solid #666;
    }
    .fewUd{
        padding:0 0.5rem ;
        display: inline-block;
        line-height:0.75rem;
        border-bottom: 1px solid #666;
    }
    .wrapBox{
        margin:0.5rem auto 0.5rem;
    }
    .tableBox{
        width:100%;
        margin:0.2rem 0;
        border-collapse:collapse;
    }
    .tableBox td,.tableBox th{
        border:1px solid #ccc;
        height:0.8rem;
        text-align:center;
    }
    .lineName{
        border:1px solid #333;

    }

    .lineName li{
        padding-left:0.5rem;
        list-style: none;
        border-bottom:1px solid #333;
    }
    .lineName li:last-child{
        border:0;
    }
</style>
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
<body>
<div class="phoneWrap">
    <h4>手机出售及回购协议</h4>
    <p style="text-indent:2em;margin-top:0.5rem;">本售后回租合同（下称“本合同”）由以下双方通过“悠多多”APP 平台以电子文本形式形成，于<span class="shortUd"></span>年<span class="shortUd"></span>月<span class="shortUd"></span>日签署，各方均承认本电子合同的法律效力。</p>
    <div class="wrapBox">
        <p>甲方（买入方、出租方）:<span>融合融资租赁（上海）有限公司</span></p>
        <p>法定代表人：<span>胡国祥</span></p>
        <p>地址：<span>中国（上海）自由贸易试验区富特北路 277 号 4 层 436 室</span></p>
    </div>
    <div class="wrapBox">
        <p>乙方（出售方、租用方）：<span class="longUd">${agreement.data.name}</span></p>
        <p>身份证号：<span class="longUd">${agreement.data.idCard}</span></p>
        <p>乙方手机号码：<span class="longUd">${agreement.data.phone}</span></p>
    </div>
    <div class="wrapBox">
        <p style="text-indent:2em;">根据《中华人民共和国合同法》、《电子签名法》，以售后回租交易方式，乙方将其自有的移动设备（以下称“回租物件”）出售给甲方并租回使用，甲、乙双方签订售后回租合同。甲、乙双方经协商一致，就回租物件购买事宜签订本合同：</p>
    </div>
    <h5>第一条  回租物件</h5>
    <p>1.1 回租物件（手机）详细情况</p>
    <p>具体见平台识别的安卓手机型号、操作系统和序列号，或苹果手机型号、操作系统和上传的苹果手机序列号图片。</p>
    <p>1.2 回租物件的任何税费事项由乙方办理与承担，并由乙方承担由此产生的一切法律责任与后果。 </p>
    <div class="wrapBox">
        <h5>第二条 合同价款</h5>
        <p>2.1 甲、乙双方确认，经双方协商，甲方按回租物件的评估价RMB<span class="shortUd">${agreement.data.assessAmount}</span> 元 (人民币：<span class="fewUd assessAmount"></span>元整)向乙方支付购买价款。</p>
        <p>2.2 甲、乙双方确认，经双方协商，乙方按回租物件的评估价的20%作为手机售后回租评估费RMB<span class="shortUd">${agreement.data.serviceAmount}</span>元 (人民币：<span class="fewUd serviceAmount"></span>元整)支付给甲方。乙方同意甲方在支付回租物件价款时直接扣收，不需再支付。</p>
        <p>2.3 付款方式</p>
        <p>因甲方支付回租物件价款的同时需要扣收服务费，在本合同生效后,甲方以电汇方式当日内向乙方实际支付 RMB<span class="shortUd">${agreement.data.payAmount}</span>元 (人民币：<span class="fewUd payAmount"></span>元整)。至此，甲方完成支付合同价款的义务。</p>
        <p>2.4 乙方指定收款账号如下：</p>
        <div class="lineName">
            <ul>
                <li>开户名称：<span>${agreement.data.name}</span></li>
                <li>开户银行：<span>${agreement.data.bankName}</span></li>
                <li>收款账号：<span>${agreement.data.bankNum}</span></li>
            </ul>
        </div>
    </div>
    <div class="wrapBox">
        <h5>第三条 回租物件所有权的转移</h5>
        <p>3.1 甲、乙双方确认，本合同第一条约定的回租物件的所有权，于甲方实际支付完毕回租物件价款后转移给甲方。并且，该所有权的转移视为乙方在回租物件现有状态下向甲方交货。</p>
        <p>3.2 上述所有权转移时，鉴于乙方已实际占有回租物件，乙方的占有行为视为甲方已将回租物件交付乙方，同时租用期视为开始。</p>
        <p>3.3 乙方回购上述回租物件时，有关所有权的转移时点及转移方式同 3.1 款；有关回租物件的交付方式同 3.2 款。</p>
    </div>
    <div class="wrapBox">
        <h5>第四条 回租物件的质量担保</h5>
        <p>4.1 乙方保证回租物件没有质量瑕疵。</p>
        <p>4.2 乙方保证甲方将上述回租物件交付时，如果回租物件存在质量瑕疵，甲方不承担责任，乙方不得对回租物件的质量向甲方提出异议或要求甲方承担任何法律责任。</p>
        <p>4.3 乙方回购上述回租物件，有关回租物件的质量担保同 4.1、4.2 款。</p>
    </div>
    <div class="wrapBox">
        <h5>第五条 回租物件的权利担保 </h5>
        <p>5.1 乙方保证在甲方支付回租物件价款前，对回租物件享有完整的所有权。</p>
        <p>5.2 乙方保证回租物件所有权转移前或同时，回租物件不存在权利上的瑕疵或限制，乙方未曾、将来也不会在回租物件上设定任何担保物权。</p>
        <p>5.3 鉴于回租的性质，对于回租物件可能存在的权利瑕疵或限制,乙方不得要求甲方承担任何责任。</p>
        <p>5.4 在乙方使用回租物件期间，若因乙方原因导致第三人人身或者财产损害，由乙方承担侵权损害赔偿责任。</p>
        <p>5.5 乙方回购上述回租物件，有关回租物件的权利担保同 5.2、5.3、5.4 款。</p>
    </div>
    <div class="wrapBox">
        <h5>第六条 回租物件升级等技术支持 </h5>
        <p>6.1 回租期间，回租物件需要的技术或服务支持等，甲方不承担责任。</p>
        <p>6.2 乙方若有上述需要，应根据其与售后服务商签订的技术或服务等合同向其主张相应权利。</p>
        <p>6.3 回租期间，由乙方自行负责回租物件的维修，保养，所需费用均由乙方承担。</p>
    </div>
    <div class="wrapBox">
        <h5>第七条 起租日和租用期限</h5>
        <p>7.1 甲方完成向乙方支付价款后，即视为租用物件的所有权从乙方转移至甲方。</p>
        <p>7.2 租用期限 7 天，从甲方支付价款当日起算；租用期届满后可以续租或回购。</p>
    </div>
    <div class="wrapBox">
        <h5>第八条 租金</h5>
        <p>8.1 乙方承租租用物件应向甲方交付租金，租金的支付方式、币种和次数等均按本合同约定。</p>
        <p>8.2 租金币种为人民币。</p>
        <p>8.3 租金及支付：租用期满租金共<span class="fewUd">${agreement.data.planRental}</span>元（大写<span class="fewUd planRental"></span>元  ），在支付租金的同时乙方必须回购租用物件（回购价款为<span class="fewUd">${agreement.data.ransomAmount}</span>元,（大写<span class="fewUd ransomAmount"></span>元））；乙方在租赁期满之日（即<span class="fewUd"></span>年<span class="fewUd"></span>月<span class="fewUd"></span>日）将租金及回购价款共计<span class="fewUd">${agreement.data.planRepay}</span>元支付甲方。 </p>
        <p>注：1、如果乙方支付上述款项发生逾期，乙方需承担违约责任，逾期第一天按租用物件评估价的11%向甲方支付违约金，从逾期第二天开始按租用物件评估价的1%/天向甲方支付违约金。2、原则上不得提前回购，如提前回购需甲方同意，租金仍按总计金额支付。3、如果付款日遇到法定节假日或公休日，付款日期不进行顺延。</p>
    </div>
    <div class="wrapBox">
        <h5>第九条 租用物件的所有权和使用权</h5>
        <p>所有权：甲方对本合同所记载的租用物件拥有完整的所有权，因此：</p>
        <p>9.1 乙方应当定期或随时向甲方提供租用物件使用、损坏、维修等状况说明，使甲方了解其真实状况；甲方有权随时用通讯或现场检查的办法，检查租用物件的状态；甲方若行使上述权利，乙方应给予积极配合和协助。</p>
        <p>9.2 在保证乙方享有本合同项下所有权利并且不影响乙方正常使用的条件下，甲方在必要时可以向任何第三方转让其对租用物件的所有权，或以租用物件抵押、担保，本合同的效力不受影响。甲方行使上述权利，应事先通知乙方，乙方有优先回购权。</p>
        <p>使用权：在租用期内乙方拥有本租用物件的使用权，因此：</p>
        <p>9.3 乙方须自负费用，按照国家或同行业有关规定和惯行的标准，对租用物件谨慎使用并进行良好的保养，定期维修和检查，使其处于良好，正常的使用状态；对租用物件维修，保养的权益归于甲方。</p>
        <p>9.4 乙方正常需要更换租用物件的零部件时，必须使用租用物件原生产厂家生产的同规格、同型号的零部件并书面通知甲方。若乙方采用代用件，必须事先征得甲方的书面同意。</p>
        <p>9.5 乙方的一切行为均不得损坏租用物件，均不得阻碍或改变租用物件原来的用途和功能。</p>
        <p>9.6 因租用物件本身设置、保管、使用等原因，致使第三者的人身及/或财产遭受损失时，乙方应承担全部赔偿和/或其它责任。</p>
        <p>9.7 乙方不得擅自出售、转让、转租租用物件；未经甲方同意，不得在租用物件上设置任何抵押权或担保权益，或进行其他任何侵害甲方所有权的行为。</p>
        <p>9.8 因租用物件本身及其设置、保管、使用及租金的支付等所发生的一切费用，均由乙方负担。</p>
        <p>9.9 在本租用合同有效期内，除非乙方违约，甲方不得干预乙方对租用物件的正常使用或擅自取回租用物件。</p>
    </div>
    <div class="wrapBox">
        <h5>第十条 租用物件的回购</h5>
        <p>10.1 乙方对租用物件有到期回购的义务。</p>
        <p>10.2 乙方按本合同规定时间，按照约定向甲方支付手机回购价款</p>
        <p>10.3 乙方实际支付回租物件的回购价款，并结清所有租金、违约金给甲方后，租用物件的所有权同时转移给乙方。</p>
    </div>
    <div class="wrapBox">
        <h5>第十一条 违约事项和补救措施</h5>
        <p>11.1 如果在租用期间内任何时间：</p>
        <p>11.1.1 乙方未能在到期日支付根据本合同应付的任何款项；</p>
        <p>11.1.2 乙方没有按照本合同的约定履行任何其他义务；</p>
        <p>11.2 如乙方发生上述 11.1 条约定的情形时，甲方有权采取下列措施,以获得根据本合同合理预期的经济利益：</p>
        <p>11.2.1 要求乙方支付违约金，逾期第一天按租用物件评估价的11%支付违约金，从逾期第二天开始按租用物件评估价的1%/天支付违约金。</p>
        <p>11.2.2 解除本合同，收回或处置租用物件，并要求乙方赔偿甲方所遭受的一切损失。当甲方做出对租用物件的处置方法时，乙方应无条件自负费用按甲方要求退回租用物件（完好状态下）给甲方，否则乙方应承担甲方重新占有、取回租用物件的合理费用。</p>
        <p>11.2.3 要求乙方偿付甲方就乙方违约行为而行使任何权利所发生的一切律师费用及其他合理支出。 </p>
        <p>11.2.4 采取法律规定的其他补救措施。</p>
        <p>11.3 甲方在采取前款规定的处置方法时，并不免除本合同规定乙方所应承担的其它义务。</p>
    </div>
    <div class="wrapBox">
        <h5>第十二条 付款授权及催款</h5>
        <p>12.1 乙方授权甲方在款项到期支付日可以通过第三方支付机构从乙方绑定的银行账户内划转应支付金额，并协助甲方获得第三方支付机构划扣所需的文件及授权。</p>
        <p>12.2 如乙方未能在到期日支付根据本合同应付的任何款项或者有其他违约行为，乙方知晓并同意甲方对乙方进行与本合同有关的违约提醒及催款、租用物件处置工作，包括但不限于电话通知、短信通知、微信通知、手机应用推送通知、发律师函、上门催款回收处置、对乙方提起诉讼等。乙方在此确认并同意，甲方可以将此违约提醒及催款、租用物件处置工作委托给本协议外的其他方进行。乙方对前述委托的提醒、催款、租用物件处置事项已明确知晓并应积极配合。因进行以上催款、租用物件处置等工作所产生的相关费用由乙方承担。</p>
    </div>
    <div class="wrapBox">
        <h5>第十三条 情况变更的处理</h5>
        <p>在租用期间，乙方姓名、地址、联系电话等发生变化，不影响本合同的执行，但乙方应立即将上述变更情况以书面形式通知甲方。</p>
    </div>
    <div class="wrapBox">
        <h5>第十四条 争议的解决及适用法律</h5>
        <p>14.1 双方因本合同引起的或与本合同有关的争议，均同意提请中国广州仲裁委员会,按照申请仲裁时该会现行有效的网络仲裁规则进行网络仲裁并书面审理案件。仲裁裁决是终局的，对双方均有约束力。</p>
        <p>送达条款：甲方确认以 <span class="longUd">Youhuishou@ronghezulin.com </span>、乙方确认以<span class="longUd">${agreement.data.email} </span>为联络邮箱，为双方之间及涉诉纠纷相关材料送达地址；甲方并以<span class="longUd">13810935107 </span>为联络手机号码、乙方并以<span class="longUd">${agreement.data.phone} </span>为联络手机号码，为短信通知号码。按本合同约定由任何一方发给其他方的任何通知，应以电子邮件或者短信等形式发出，送至约定邮箱或者手机号码。收件人指定系统接受材料或通知之日，即视为送达和收到之日。如一方需变更联络邮箱或者手机号码的，应当书面通知并得到对方确认。</p>
        <p>仲裁费、保全费、公告费、律师费、保全担保费、保全保险费、调查费、公证费等因维权仲裁所产生的相关一切费用及损失由败诉方承担。</p>
        <p>14.2 本协议适用中华人民共和国法律。</p>
    </div>
    <div class="wrapBox">
        <h5>第十五条 合同的终止</h5>
        <p>本合同只有在以下情况下才能终止：</p>
        <p>15.1 甲乙双方已全部履行完毕本合同规定的各项义务。</p>
        <p>15.2 经甲、乙双方同意协议解除本合同。</p>
        <p>15.3 其他法律规定合同终止的情况。</p>
    </div>
    <div class="wrapBox">
        <h5>第十六条 合同的转让</h5>
        <p>16.1 甲方有权将其在本合同项下的权益全部或部分转让给有关第三者，但甲方有义务将转让的事实及时通知乙方，以便乙方对受让人履行其在本合同项下的义务。</p>
        <p>16.2 乙方如欲转让其在本合同项下的权利和义务，必须事先征得甲方的书面同意。</p>
    </div>
    <div class="wrapBox">
        <h5>第十七条 合同的变更和解除</h5>
        <p>除本合同另有约定，本合同一经签订，甲、乙任一方均不得任意变更和解除；如变更或解除，需经甲、乙双方共同协商并以书面形式达成协议。如果任何一方有违约行为，经另一方催告改正，违约方在守约方催告期限内仍未改正的，则守约方有权解除合同。</p>
    </div>
    <div class="wrapBox">
        <h5>第十八条 合同的生效条件</h5>
        <p>合同自双方通过 悠多多 APP 平台电子签名或盖章之日起生效。</p>
    </div>
    <div class="wrapBox">
        <h5>第十九条 其他</h5>
        <p>19.19.1 本合同构成双方权利义务的全部，构成甲、乙双方之间售卖与租用有关的全部协议。</p>
        <p>19.2 乙方在此确认，乙方将在租用期间或其实际占用期间内承担有关租用物件灭失及毁损的风险。在租用期间或乙方实际占用期间内，如租用物件遭受灭失及毁损，乙方应及时采取有效措施以防止损失的扩大，同时必须立即通知甲方。甲方可选择如下约定的一种或几种方式进行处理，并由乙方负担全部费用：</p>
        <p>19.2.1 将租用物件复原或修理至完全正常使用状态；</p>
        <p>19.2.2 更换与租用物件同等型号、性能的部件、配件或物件；</p>
        <p>19.3 在前款情形下，本合同继续执行，乙方支付租金、回购租用物件及其他义务不变;</p>
        <p>19.4 租用物件灭失或毁损到无法修理的程度时，乙方应在甲方的支付通知要求的时间内将全部未付租金、其他应付未付回购款项及违约金、其他任何应付款项支付甲方。</p>
        <p>19.5 根据前款，乙方将上述所有应付款项支付给甲方后，甲方将租用物件（以其当时状态）的所有权及对第三者的权利（如有时）转移给乙方。至此，本合同终止。</p>
    </div>
    <div class="wrapBox">
        <p>甲 方 ： (公章)</p>
        <p>法定代表人/授权代表（签字）：<span></span></p>
    </div>
    <div class="wrapBox">
        <p>乙 方（签字）：<span></span></p>
    </div>
</div>
<script>

    /** 数字金额大写转换(可以处理整数,小数,负数) */
    function smalltoBIG(n) {
        var fraction = ['角', '分'];
        var digit = ['零', '壹', '贰', '叁', '肆', '伍', '陆', '柒', '捌', '玖'];
        var unit = [ ['元', '万', '亿'], ['', '拾', '佰', '仟']  ];
        var head = n < 0? '欠': '';
        n = Math.abs(n);

        var s = '';

        for (var i = 0; i < fraction.length; i++)
        {
            s += (digit[Math.floor(n * 10 * Math.pow(10, i)) % 10] + fraction[i]).replace(/零./, '');
        }
        s = s || '整';
        n = Math.floor(n);

        for (var i = 0; i < unit[0].length && n > 0; i++)
        {
            var p = '';
            for (var j = 0; j < unit[1].length && n > 0; j++)
            {
                p = digit[n % 10] + unit[1][j] + p;
                n = Math.floor(n / 10);
            }
            s = p.replace(/(零.)*零$/, '').replace(/^$/, '零')  + unit[0][i] + s;
        }
        return head + s.replace(/(零.)*零元/, '元').replace(/(零.)+/g, '零').replace(/^整$/, '零元整').replace("元整", "");
    }

    $(function() {
        var assessAmount = ${agreement.data.assessAmount};
        var payAmount = ${agreement.data.payAmount};
        var ransomAmount = ${agreement.data.ransomAmount};
        var serviceAmount = ${agreement.data.serviceAmount};
        var planRental = ${agreement.data.planRental};
        $(".assessAmount").html(smalltoBIG(assessAmount));
        $(".payAmount").html(smalltoBIG(payAmount));
        $(".ransomAmount").html(smalltoBIG(ransomAmount));
        $(".serviceAmount").html(smalltoBIG(serviceAmount));
        $(".planRental").html(smalltoBIG(planRental));
    });
</script>
</body>
</html>