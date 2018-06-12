<%--
  Created by IntelliJ IDEA.
  User: xingmin
  Date: 2018/5/28
  Time: 10:57
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<html style="font-size: 0.75rem;">
    <head>
        <title>认证支付</title>
        <meta charset="UTF-8">
        <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport" />
        <meta http-equiv="X-UA-Compatible" content="ie=edge">
        <style>
            * {
                margin: 0;
                padding: 0;
            }

            html {
                min-width: 319px;
                max-width: 750px;
                margin: 0 auto;
            }

            body {
                background-color: #f6f6f8;
                font-size: .75rem;
            }

            h3 {
                text-align: center;
                font-weight: normal;
            }

            li {
                position: relative;
                list-style: none;
                width: 100%;
                margin-top: .25rem;
                border-top: 1px solid #dcdcdc;
            }

            .inner {
                height: 2.5rem;
                line-height: 2.5rem;
                border-bottom: 1px solid #dcdcdc;
                padding: 0 .5rem;
                background-color: #fff;
            }

            .head {
                padding: .6rem .75rem;
            }

            .showMoney {
                font-size: .9rem;
                text-align: center;
                line-height: 4rem;
            }

            .icon {
                font-size: 1.8rem;
            }

            .money {
                font-size: 2.5rem;
            }

            button {
                background-color: #1e82d2;
                text-align: center;
                color: #fff;
                border: 0;
                border-radius: .2rem;
                font-size: .75rem;
            }

            .sendBtn {
                width: 3.75rem;
                height: 1.5rem;
                line-height: 1.5rem;
                margin-top: .5rem;
            }

            .payBtn {
                display: block;
                width: 15rem;
                height: 2.2rem;
                line-height: 2.2rem;
                letter-spacing: .5rem;
                margin: 0 auto;
                margin-bottom: 1rem;
            }

            input {
                border: 0;
                height: 2.5rem;
                padding-left: .6rem;
                font-size: .75rem;
            }

            .fl {
                float: left;
            }

            .fr {
                float: right;
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

            .tip {
                width: 15rem;
                color: #eb7764;
                margin: 1rem auto;
            }

            .bank {
                color: #1e82d2;
                font-size: .7rem;
            }

            .bankCard {
                width: 40%;
                line-height: 1.3rem;
                text-align: right;
            }

            input:focus {
                outline: 0;
            }

            .wdth53 {
                width: 53%;
            }
        </style>
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
                ;
            }

            // 当窗口发生改变的时候调用
            window.onresize = function () {
                getSize();
            }
        </script>
    </head>
    <body>
        <div class="head">
            <h3>支付金额</h3>
            <div class="showMoney"><span class="icon">￥</span><span class="money">${result.amount}</span>元</div>
            <div>
                <p>订单号：${result.serial}</p>
                <p>收款商户：${result.company}</p>
            </div>
        </div>
        <div class="main">
            <div class="clearfix">
                <li>
                    <div class="inner">
                        <p class="fl">卡号</p>
                        <p class="fr bankCard"><span class="card">${result.bankCard}</span><span
                                class="bank">${result.bankName}</span></p>
                    </div>
                </li>
            </div>
            <li>
                <div class="inner clearfix"><p class="fl">姓名</p>
                    <p class="fr user">${result.name}</p></div>
                <div class="inner clearfix"><p class="fl">身份证</p>
                    <p class="fr userCard">${result.cardNum}</p></div>
            </li>
            <li>
                <div class="inner clearfix"><p class="fl">手机号</p>
                    <p class="fr">${result.phone}</p></div>
                <div class="inner clearfix"><p class="fl">短信验证码</p>
                    <input id="phoneCode" type="number" placeholder="请输入短信验证码" class="wdth53 fl">
                    <button class="sendBtn fr" value="点击发送">点击发送</button>
                </div>
            </li>

            <div class="tip">银行扣款后，悠多多APP会在20-60分钟更新借款状态，请您耐心等待。</div>
        </div>
        <div class="foot">
            <button class="payBtn" value="支付">支付</button>
        </div>
        <script src="https://uhuishou.ronghezulin.com/loan-app/js/jquery-1.8.0.min.js"></script>
        <script>
            $(function () {

                var sendMsg = function () {
                    if (navigator.userAgent.match(/(iPhone|iPod|iPad);?/i)) {
                        //苹果
                        window.webkit.messageHandlers.sendMsg.postMessage('');
                    }else if (navigator.userAgent.match(/android/i)) {
                        //安卓
                        window.yhs.sendMsg();
                    }
                };
                
                var repay = function (code) {
                    if (navigator.userAgent.match(/(iPhone|iPod|iPad);?/i)) {
                        //苹果
                        window.webkit.messageHandlers.repay.postMessage(code);
                    }else if (navigator.userAgent.match(/android/i)) {
                        //安卓
                        window.yhs.repay(code);
                    }
                }

                var reg = /^(\d{4})\d+(\d{4})$/;
                $('.card').text($('.card').text().replace(reg, "$1**********$2"));
                $('.userCard').text($('.userCard').text().replace(reg, "$1****$2"));
                var str = $('.user').text();
                var sum = str.slice(-2);
                for (var i = 0; i < str.length - 2; i++) {
                    sum = '*' + sum;
                }
                $('.user').text(sum)


                // 点击支付按钮变灰禁用
                $('.payBtn').on('click', function () {
                    $(this).css({background: '#dcdcdc',}).attr('disabled', 'disabled');
                    repay($("#phoneCode").val());
                })

                // 点击发送
                $('.sendBtn').on('click', function () {
                    sendMsg();
                    $(this).css({background: '#dcdcdc'}).attr('disabled', 'disabled').text('60s');
                    var left_time = 60;
                    var tt = window.setInterval(function(){
                        left_time = left_time - 1;
                        if (left_time <= 0) {
                            window.clearInterval(tt);
                            $('.sendBtn').css({background: '#1e82d2',}).removeAttr('disabled').text('点击发送');
                        }else {
                            $('.sendBtn').text(left_time + 's');
                        }
                    }, 1000);
                })
            })

            function repayResult(code) {
                $('.payBtn').css({background: '#1e82d2',}).removeAttr('disabled')
            }

        </script>
    </body>
</html>
