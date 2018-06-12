var initRepaymentPlanTable = function(){
    tableUtils.initMuliTableToolBar(
        "repaymentPlanTable",
        "repayment/plan.action?userNo="+usernum,
        null,
        [
            {dataField : "borrNum",caption : "合同编号",alignment : "center",allowFiltering:true,filterOperations:["="],allowSorting:false},
            {dataField : "bedueDays",caption : "逾期天数",alignment : "center",allowFiltering:true,filterOperations:["=",">"]},
            {dataField : "borrId",caption : "borrId",alignment : "center",visible: false},
            {dataField : "customerId",caption : "customerId",alignment : "center",visible: false,allowSorting:false},
            {dataField : "userName",caption : "姓名",alignment : "center",allowFiltering:true,width:110,filterOperations:["="],allowSorting:false},
            {dataField : "idCard",caption : "身份证号码",alignment : "center",allowFiltering:true,width:190,filterOperations:["="],allowSorting:false},
            {dataField : "customerMobile",caption : "手机号码",alignment : "center",allowFiltering:true,width:140,filterOperations:["="],allowSorting:false},
            {dataField : "productId",caption : "产品名称",alignment : "center",allowFiltering:true,allowSorting:false,width:150,
                lookup:{
                    dataSource:pruducts,displayExpr: 'format',valueExpr: 'value'
                }
            },
            {dataField : "rental",caption : "应还租金",alignment : "center",allowFiltering:false,allowSorting:false},
            {dataField : "ransomAmount",caption : "回购手机",alignment : "center",allowFiltering:false,allowSorting:false},
            {dataField : "penalty",caption : "应还违约金",alignment : "center",allowFiltering:false,allowSorting:false},
            {dataField : "depositAmount",caption : "押金",alignment : "center",allowFiltering:false,allowSorting:false},
            {dataField : "surplusTotalAmount",caption : "剩余还款",alignment : "center",allowFiltering:false,filterOperations:["="],allowSorting:true},
            {
                dataField : "repayDate",
                caption : "到期日",
                alignment: "center",
                dataType: "date",
                filterOperations:["=","between"],
                calculateCellValue: function (data) {
                    if(data) {
                        return data.repayDate;
                    }else {
                        return '';
                    }
                }
            },
            {dataField : "borrStatus",caption : "合同状态",alignment : "center",allowFiltering:true,allowSorting:false,width:100,filterOperations:["="],
                lookup:{
                    dataSource:[
                        { value: 'BS005', format: '逾期未还' },
                        { value: 'BS004', format: '待还款' },
                    ],
                    valueExpr: 'value',
                    displayExpr: 'format'
                }
            }
        ],
        "还款计划"+new Date(),
        function(e){
            var dataGrid = e.component;
            var toolbarOptions = e.toolbarOptions.items;
            toolbarOptions.push({
                location: "before",
                widget: "dxButton",
                options: {
                    hint: "查看",
                    text: "查看",
                    visible : !disableButton("ym-db",0),
                    icon: "find",
                    onClick: function () {
                        var selectData = dataGrid.getSelectedRowsData();
                        if (selectData.length == 0) {
                            alert("请选择需要查看的还款计划");
                            return;
                        }
                        if (selectData.length > 1) {
                            alert("一次只能操作一条数据");
                            return;
                        }
                        var customerId = selectData[0].customerId;
                        var contractKey = selectData[0].borrId;
                        var contractId = selectData[0].borrNum;
                        console.info(customerId + "==" + contractKey + "==" + contractId);
                        layer_alert(customerId, contractKey, contractId);
                    }
                }
            },
                {
                    location: "before",
                    widget: "dxButton",
                    visible : !disableButton("ym-db",1),
                    options: {
                        hint: "刷新",
                        text: "刷新",
                        icon: "refresh",
                        onClick: function () {
                            tableUtils.refresh("repaymentPlanTable");
                        }
                    }
                })
        }
    );
};

var repaymentPlan = function () {
    $('.modal-backdrop').hide();
    checkPageEnabled("ym-db");
    initRepaymentPlanTable();
};

Date.prototype.format = function(format)
{
    var o = {
        "M+" : this.getMonth()+1, //month
        "d+" : this.getDate(),    //day
        "h+" : this.getHours(),   //hour
        "m+" : this.getMinutes(), //minute
        "s+" : this.getSeconds(), //second
        "q+" : Math.floor((this.getMonth()+3)/3),  //quarter
        "S" : this.getMilliseconds() //millisecond
    }
    if(/(y+)/.test(format)) format=format.replace(RegExp.$1,
        (this.getFullYear()+"").substr(4 - RegExp.$1.length));
    for(var k in o)if(new RegExp("("+ k +")").test(format))
        format = format.replace(RegExp.$1,
            RegExp.$1.length==1 ? o[k] :
                ("00"+ o[k]).substr((""+ o[k]).length));
    return format;
}

