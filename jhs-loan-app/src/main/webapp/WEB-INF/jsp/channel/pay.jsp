<%--
  Created by IntelliJ IDEA.
  User: jiebaoqiang
  Date: 2018/1/23
  Time: 16:32
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt_rt" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport" />
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>支付方式</title>
    <link rel="stylesheet" href="../css/pay.css">
    <script>
        var oHtml = document.documentElement;
        getSize();
        function getSize() {
            // 获取屏幕的宽度
            var ascreen = oHtml.clientWidth;
            if (ascreen <= 319) {
                oHtml.style.fontSize = '20px';
            } else if (ascreen >= 750) {
                oHtml.style.fontSize = '40px';
            } else {
                oHtml.style.fontSize = ascreen / 18.75 + "px";
            }
        }
        // 当窗口发生改变的时候调用
        window.onresize = function () {
            getSize();
        }
    </script>
    <style>
        #alsoAmount {
            font-size: .85rem;
            font-style: normal;
            margin: 0;
            padding: 0;
            color: #1e82d2;
            border: 0;
            outline:none;
            height: 20px;
        }
        .tips{
            position: fixed;
            top:30%;
            left:50%;
            transform: translateY(-50%);
            transform: translateX(-50%);
            -webkit-transform: translateY(-50%);
            -webkit-transform: translateX(-50%);
            padding: .5rem;
            background-color: rgba(0, 0, 0, .5);
            font-size: .3rem;
            color:#fff;
            border-radius: .5rem;
        }
    </style>
</head>
<body>
<form action="payCenter/agentDeduct.action" id="zfbForm" method="post" onsubmit="return false">
    <input type="hidden" name="payChannel" id="payChannel"/>
    <input type="hidden" name="bankId" value="${repaymentDetails.data.bankId}"/>
    <input type="hidden" name="bankNum" value="${repaymentDetails.data.cardNo}"/>
    <input type="hidden" name="borrNum" value="${repaymentDetails.data.borr_id}"/>
    <input type="hidden" name="type" value="2"/>
    <input type="hidden" name="triggerStyle" value="3"/>
    <input type="hidden" name="createUser" value=""/>
    <div class="head">

        <div class="inner">
            <p>付款金额 &nbsp;&nbsp;<span class="color">￥<em><input type="text" id="alsoAmount" name="optAmount"
                                                                value="${repaymentDetails.data.alsoAmount}"/></em></span>
            </p>
        </div>
        <div class="inner">
            <p>实际付款金额 &nbsp;&nbsp;<span class="gray">￥<em id="repayAmount">
                ${repaymentDetails.data.act_repay_amount}
            </em></span></p>
        </div>
    </div>
    <div class="main">
        <p class="tip1">请选择支付方式</p>
        <div class="clearfix">
            <ul>
                <li>
                    <div class="inner">
                        <c:if test="${!empty repaymentDetails.data.cardNo}">
                        <p class="fl">
                            <img src="../images/${repaymentDetails.data.bankCode}.png"
                                           alt="">${repaymentDetails.data.bankName}（尾号${fn:substring(repaymentDetails.data.cardNo,fn:length(repaymentDetails.data.cardNo)-4,fn:length(repaymentDetails.data.cardNo))}）
                        </p>
                        <p class="fr bankCard" data-id="unionpay"><img src="../images/noActive.png" alt=""></p>
                        </c:if>
                        <c:if test="${empty repaymentDetails.data.cardNo}">
                        <p class="fl"> 未查询到主卡，请使用其他支付方式或绑定主卡</p>
                        </c:if>
                    </div>
                </li>
                <li>
                    <div class="inner">
                        <p class="fl"><img src="../images/icon_ali.png" alt="">支付宝支付</p>
                        <p class="fr bankCard" data-id="pay-zfb"><img src="../images/noActive.png" alt=""></p>
                    </div>
                </li>
                <%--   <li>
                       <div class="inner">
                           <p class="fl"><img src="images/icon_wx.png" alt="">微信支付</p><p class="fr bankCard" data-id="1"><img src="images/noActive.png" alt=""></p>
                       </div>
                   </li>--%>
            </ul>
        </div>
    </div>
</form>
<div class="foot">
    <div class="tip2">说明：每笔手续费${repaymentDetails.data.counterFee}元，由第三方平台收取</div>
    <button class="payBtn" value="支付" onclick="return submitForm()">确认付款</button>
</div>
<div class="modal" style="display: none">
    <div class="modalBox">
        <img id="close" src="../images/ic_close.png" style="width: 14%;height: 20%;position: absolute;right: 0;top: -2.5rem;" alt="">
        <div class="title">
            <img src="../images/tip.png" alt="">
        </div>
        <div class="content">
            支付订单已提交！
        </div>
        <div class="bottom">
            <button class="success fl">完成支付</button>
            <button class="goTo color fl">继续支付</button>
        </div>
    </div>
</div>
<div class="loading" style="display: none">
    <div class="modalBox">
        <div class="content">
            <img src="../images/loading.gif">
        </div>
    </div>
</div>
<div class="tips" style="display: none;" >
    <div class="content" style="font-size: 20px">
    </div>
</div>
<script src="../js/jquery-1.8.0.min.js"></script>
<%--<script src="http://jh.yizhibank.com/js/callalipay.js"></script>--%>
<script>
    var flag = true;
    var openDetails = function () {
        if (navigator.userAgent.match(/(iPhone|iPod|iPad);?/i)) {
            //苹果
            window.webkit.messageHandlers.openDetails.postMessage(null);
        }else if (navigator.userAgent.match(/android/i)) {
            //安卓
            window.yhs.openDetails();
        }
    };

    var unionpay = function () {
       var bankId = ${empty repaymentDetails.data.bankId ? 0 :repaymentDetails.data.bankId};
        if (navigator.userAgent.match(/(iPhone|iPod|iPad);?/i)) {
            //苹果
            window.webkit.messageHandlers.unionpay.postMessage({
                alsoAmount:$("#alsoAmount").val(),
                act_repay_amount:$("#repayAmount").text().trim(),
                bankId:bankId ,
                borr_id:${repaymentDetails.data.borr_id}
            });
        }else if (navigator.userAgent.match(/android/i)) {
            //安卓
            window.yhs.unionpay($("#alsoAmount").val(),$("#repayAmount").text().trim(),bankId ,${repaymentDetails.data.borr_id});
        }
    };

    function zfbSubmit() {

        $('.loading').show();
        if (flag) {
            flag = false;
            $.ajax({
                type: "POST",
                dataType: "json",
                url: "../payCenter/agentDeduct.action",
                data: $("#zfbForm").serialize(),
                timeout : 30000,
                success: function (res) {
                    $('.loading').hide();
                    if (res.code == 200) {
                        if (res.data == '' || res.data == null) {
                            $(".tips .content").html("系统繁忙，请稍候");
                            $(".tips").fadeIn().delay(3000).fadeOut();
                        } else {
                            callAlipay(res.data);
                            $('.modal').show();
                        }
                    } else {
                        if (res.code != 200) {
                            $(".tips .content").html(res.info);
                            $(".tips").fadeIn().delay(3000).fadeOut();
                        }
                    }
                    flag = true;
                },
                error : function(xhr,textStatus) {
                    if (textStatus == 'timeout') {
                        $('.loading').hide();
                        $(".tips .content").html("系统繁忙，请稍候");
                        $(".tips").fadeIn().delay(3000).fadeOut();
                    }
                    flag = true;
                }
            });
        }
    }

    function submitForm() {

        var alsoAmount = $("#alsoAmount").val();
        var pattern = /^([1-9]\d*|0)(\.\d{1,2}|\.)?$/;
        if (!pattern.test(alsoAmount)) {
            $("#alsoAmount").val("");
            $("#repayAmount ").html((${repaymentDetails.data.counterFee}).toFixed(2));
            $(".tips .content").html("输入金额格式错误");
            $(".tips").fadeIn().delay(3000).fadeOut();
            return;
        }
        //验证金额是否小于最低应还
        var minimum = ${repaymentDetails.data.minimum};
        if (alsoAmount != null && alsoAmount != '' && alsoAmount < minimum) {
            $("#alsoAmount").val("");
            $("#repayAmount ").html((${repaymentDetails.data.counterFee}).toFixed(2));
            $(".tips .content").html("金额小于最低应还" + minimum + "元");
            $(".tips").fadeIn().delay(3000).fadeOut();
            return;
        }
        //验证金额是否大于最大应还
        var maximum = ${repaymentDetails.data.alsoAmount};
        if (alsoAmount > maximum) {
            $("#alsoAmount").val("");
            $("#repayAmount ").html((${repaymentDetails.data.counterFee}).toFixed(2));
            $(".tips .content").html("金额大于最大应还" + maximum + "元");
            $(".tips").fadeIn().delay(3000).fadeOut();
            return;
        }
        //验证渠道是否选择
        var payChannel = $("#payChannel").val();
        if (payChannel == '' || payChannel == null){
            $(".tips .content").html("请选择支付渠道");
            $(".tips").fadeIn().delay(3000).fadeOut();
            return;
        }
        if ("pay-zfb" == payChannel) {
            zfbSubmit();
        }else if ("unionpay" == payChannel){
            unionpay();
        }
    }

    $(function () {
        // 点击发送
        $('.bankCard').on('click', function () {
            $('.bankCard img').attr('src', '../images/noActive.png');
            $(this).find('img').attr('src', '../images/active.png');
            $("#payChannel").val($(this).data().id);
        });

        $('.success').on('click', function () {
            openDetails();
        });
        $('#close').on('click', function () {
            $('.modal').hide();
        });
        $('.goTo').on('click',function () {
           zfbSubmit();
        });
    });
    $(function () {
        $("#alsoAmount").bind("input propertychange change", function (event) {
            var alsoAmount = $("#alsoAmount").val();
            var fee = ${repaymentDetails.data.counterFee};
            $("#repayAmount ").html((alsoAmount * 1 + fee * 1).toFixed(2));
            var pattern = /^([1-9]\d*|0)(\.\d{1,2}|\.)?$/;
            if (!pattern.test(alsoAmount)) {
                $("#alsoAmount").val("");
                $("#repayAmount ").html((${repaymentDetails.data.counterFee}).toFixed(2));
            }
        });
    });

    function callAlipay(gourl) {
        if(gourl) {
            var urlscheme = 'alipays';
            var ug = this.userAgent();
            if(ug.ios) {
                urlscheme = 'alipay';
            }
            var p = 'platformapi';
            var sm = '11';
            var s = '100000' + sm;
            var gopage = urlscheme + '://' + p + '/startApp?appId=' + s + '&url=' + encodeURIComponent(gourl);
            document.location.href = gopage;
        }
    }

    function userAgent() {
        var output = {};
        if(navigator.userAgent.match(/(iPhone|iPod|iPad);?/i)) {
            output['ios'] = true;
        } else if(navigator.userAgent.match(/android/i)) {
            output['android'] = true;
        }
        return output;
    }
</script>
</body>
</html>