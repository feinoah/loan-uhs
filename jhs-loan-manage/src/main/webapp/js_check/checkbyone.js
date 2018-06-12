var checkbyone = function () {
    setToolbar_za(true);
    loadtable2_za();

    window_find_za();
    window_ok_za();
    window_no_za();
    window_no_black();
}

var setToolbar_za = function (init) {
    checkPageEnabled("ym-za");

    $(".modal").on('hide.bs.modal', function () {
        tableUtils.clearSelection("userTable");
    });
    $(".modal").on('hidden.bs.modal', function () {
        tableUtils.clearSelection("userTable");
    });

    $("#tool_find").dxButton({
        hint: "查看详情",
        text: "详情",
        icon: "find",
        disabled: true,
        onClick: function () {
            var dataGrid = $('#userTable').dxDataGrid('instance');
            var selectobj = dataGrid.getSelectedRowsData();
            var himid = selectobj[0].perId;
            var brroid = selectobj[0].id;
            loadwindow_userinfo(himid, brroid);
        }
    });

    if(init){
        $("#tool_selectBox").dxSelectBox({
            placeholder: '放款渠道',
            dataSource: payChannelsSource2,
            searchEnabled: true,
            value:"",
            valueExpr: 'value',
            displayExpr: 'format',
            onItemClick: function (e) {
                var payChannel = e.itemData.value;
                $("#payChannel").val(payChannel);
            }
        });
    }

    $("#tool_ok").dxButton({
        hint: "放款",
        text: "放款",
        icon: "todo",
        disabled: true,
        onClick: function () {
            var payChannel = $("#payChannel").val();
            var dataGrid = $('#userTable').dxDataGrid('instance');
            var selectobj_param = dataGrid.getSelectedRowsData();
            var name;
            for(var i=0; i<payChannelsSource.length;i++){
                if(payChannelsSource[i].value==payChannel){
                    name = payChannelsSource[i].format;
                    break;
                }
            }
            $("#window_ok").dxPopup({
                title: '放款('+name+')',
                visible: true,
            });
            box_ok_value_za(selectobj_param[0],payChannel);
        }
    });
    $("#tool_no").dxButton({
        hint: "拒绝",
        text: "拒绝",
        icon: "close",
        disabled: true,
        onClick: function () {

            var dataGrid = $('#userTable').dxDataGrid('instance');
            var selectobj_param = dataGrid.getSelectedRowsData();
            if (selectobj_param.length > 1) {
                showMessage("只能选择一条数据！");
            } else {
                $("#window").dxPopup({
                    visible: true,
                });
                box_no_value_za(selectobj_param[0]);
            }
        }
    });

    $("#tool_black").dxButton({
        hint: "拉黑",
        text: "拒绝并拉黑",
        icon: "close",
        disabled: true,
        onClick: function () {
            var dataGrid = $('#userTable').dxDataGrid('instance');
            var selectobj_param = dataGrid.getSelectedRowsData();
            if (selectobj_param.length > 1) {
                showMessage("只能选择一条数据！");
            } else {
                $("#window").dxPopup({
                    visible: true,
                });
                box_black_value_za(selectobj_param[0]);
            }
        }
    });
};

var loadtable2_za = function () {
    DevExpress.config({
        forceIsoDateParsing: true,

    });
    var orders = new DevExpress.data.CustomStore({
        load: function (loadOptions) {
            var deferred = $.Deferred(),
                args = {};
            args.filter = loadOptions.filter ? JSON.stringify(loadOptions.filter) : "";   // Getting filter settings
            args.sort = loadOptions.sort ? JSON.stringify(loadOptions.sort) : "";  // Getting sort settings
            args.requireTotalCount = loadOptions.requireTotalCount; // You can check this parameter on the server side
            if (loadOptions.sort) {
                args.orderby = loadOptions.sort[0].selector;
                if (loadOptions.sort[0].desc)
                    args.orderby += " desc";
            }

            args.skip = loadOptions.skip || 0;
            args.take = loadOptions.take || 15;
            args.employNum = usernum;
            $.ajax({
                url: 'risk/auditsforUser.action',
                data: args,
                type: 'GET',
                success: function (result) {
                    result = JSON.parse(result);
                    deferred.resolve(result.list, {totalCount: result.total});
                },
                error: function () {
                    deferred.reject("Data Loading Error");
                },
                timeout: 50000
            });

            return deferred.promise();
        }
    });

    $("#userTable")
        .dxDataGrid(
            {
                dataSource: {
                    store: orders
                },
                dateSerializationFormat: "yyyy-MM-dd HH:mm:ss",
                remoteOperations: {
                    sorting: true,
                    paging: true,
                    filtering: true
                },
                filterRow: {
                    visible: true,
                    applyFilter: "auto"
                },
                rowAlternationEnabled: true,
                showRowLines: true,
                selection: {
                    mode: "multiple"
                },
                allowColumnReordering: true,
                allowColumnResizing: true,
                columnAutoWidth: true,
                columnChooser: {
                    title: "列选择器",
                    enabled: true,
                    emptyPanelText: '把你想隐藏的列拖拽到这里...'
                },
                columnFixing: {
                    enabled: true
                },
                paging: {
                    pageSize: 15,
                },
                pager: {
                    showPageSizeSelector: true,
                    allowedPageSizes: [10, 15, 30, 45, 60],
                    showInfo: true,
                    infoText: '第{0}页 . 共{1}页'
                },
                onSelectionChanged: function (data) {
                    var flag = false;
                    for (var i = 0; i < data.selectedRowsData.length; i++) {
                        if (data.selectedRowsData[i].borrStatus != "BS003"
                            && data.selectedRowsData[i].borrStatus != "BS012") {
                            flag = true;
                            break;
                        }
                    }

                    $("#tool_find")
                        .dxButton(
                            {
                                disabled: (data.selectedRowsData.length != 1)
                                || disableButton("ym-za",0),
                            });
                    $("#tool_ok")
                        .dxButton(
                            {
                                disabled: data.selectedRowsData.length != 1
                                || flag
                                || disableButton("ym-za",1),
                            });
                    $("#tool_no")
                        .dxButton(
                            {
                                disabled: data.selectedRowsData.length != 1
                                || flag
                                || disableButton("ym-za",2),
                            });
                    $("#tool_black")
                        .dxButton(
                            {
                                disabled: data.selectedRowsData.length != 1
                                || flag
                                || disableButton("ym-za",3),
                            });
                },
                columns: [
                    {dataField: "borrNum",
                    caption: "合同编号",
                    alignment: "center",
                    allowFiltering:true,
                    filterOperations:["="],
                    allowSorting:false
                }, {
                    dataField: "name",
                    caption: "姓名",
                    alignment: "center",
                    allowFiltering:true,
                    filterOperations:["="],
                    allowSorting:false
                }, {
                    dataField: "cardNum",
                    caption: "身份证号",
                    alignment: "center",
                    allowFiltering:true,
                    filterOperations:["="],
                    allowSorting:false
                }, {
                    dataField: "phone",
                    caption: "手机号码",
                    alignment: "center",
                    allowFiltering:true,
                    filterOperations:["="],
                    allowSorting:false
                },
                {
                    dataField: "productName",
                    caption: "产品名称",
                    alignment: "center",
                    allowFiltering:true,
                    filterOperations:["="],
                    allowSorting:false,
                    lookup:{
                        dataSource:pruducts,
                        displayExpr: 'format'
                    },width:125
                }, {
                    dataField: "borrAmount",
                    caption: "贷款金额",
                    alignment: "center",
                    dataType: "number",
                    allowFiltering:true,
                    filterOperations:["="],
                    allowSorting:false
                }, {
                    dataField: "bankName",
                    caption: "银行名称",
                    alignment: "center",
                    allowFiltering:true,
                    filterOperations:["="],
                    allowSorting:false
                }, {
                    dataField: "bankCard",
                    caption: "银行卡号",
                    alignment: "center",
                    allowFiltering:true,
                    filterOperations:["="],
                    allowSorting:false
                }, {
                    dataField: "borrStatusValue",
                    caption: "合同状态",
                    alignment: "center",
                    lookup: {
                        dataSource: [
                            {value: 'BS003', format: '已签约'},
                            {value: 'BS012', format: '放款失败'}
                        ],
                        displayExpr : 'format'
                    }
                },
                {
                    dataField: "borrUpStatusValue",
                    caption: "上单状态",
                    alignment: "center",
                    lookup: {
                        dataSource: [
                            {value: 'BS006', format: '正常结清'},
                            {value: 'BS007', format: '已取消'},
                            {value: 'BS008', format: '审核未通过'},
                            {value: 'BS009', format: '电审未通过'},
                            {value: 'BS010', format: '逾期结清'},
                            {value: 'BS012', format: '放款失败'},
                            {value: 'BS013', format: '提前结清'}
                        ],
                        displayExpr : 'format'
                    }
                }
                , {
                        dataField : "baikeluStatus",
                        caption : "自动电呼",
                        alignment : "center",
                        calculateCellValue: function (data) {
                            if(data.baikeluStatus == null){
                                return "未拨打";
                            }else{
                                if(data.baikeluStatus == 1){
                                    return "拨打中";
                                }else if (data.baikeluStatus == 2){
                                    return "未完成";
                                }else if (data.baikeluStatus == 3){
                                    return "通过";
                                }else if (data.baikeluStatus == 4){
                                    return "拒绝";
                                }else if (data.baikeluStatus == 5){
                                    return "未接通";
                                }else if (data.baikeluStatus == 6){
                                    return "非本人";
                                }
                            }
                        },
                        lookup: { dataSource: [
                            {value: '1', format: '拨打中'},
                            {value: '2', format: '未完成'},
                            {value: '3', format: '通过'},
                            {value: '4', format: '拒绝'},
                            {value: '5', format: '未接通'},
                            {value: '6', format: '非本人'},
                        ],  displayExpr: 'format' }
                    },{
                    dataField: "reason",
                    caption: "审核理由",
                    alignment: "center",
                    allowFiltering:false,
                    allowSorting:false
                },
                {
                    dataField: "isManual",
                    caption: "是否人工审核",
                    alignment: "center",
                    calculateCellValue: function (data) {
                        if (data.isManual != 4) {
                            return "是";
                        } else {
                            return "否";
                        }
                    },
                    lookup: {
                        dataSource: [
                            {value: '1', format: '是'},
                            {value: '2', format: '否'},
                        ], displayExpr: 'format'
                    }
                }, {
                    dataField: "description",
                    caption: "认证说明",
                    alignment: "center",
                    allowFiltering:false,
                    allowSorting:false
                },
                {
                    dataField: "makeborrDate",
                    caption: "签约时间",
                    alignment: "center",
                    dataType: "date",
                    filterOperations:["=","between"]
                }]
            });
}

// 查看详情
var window_find_za = function () {
    $("#window_find").dxPopup({
        showTitle: true,
        title: '详情',
        width: "95%",
        height: "88%",
        visible: false,
        WindowScroll: true,
        resizeEnabled: true,
        onHiding: function () {
            loadtable2_za();
        },
    });
}
var window_ok_za = function () {
    $("#window_ok").dxPopup({
        showTitle: true,
        maxWidth: 400,
        maxHeight: 220,
        title: '放款',
        visible: false,
        WindowScroll: true,
        resizeEnabled: true,
        onHiding: function () {
            setToolbar_za();
            loadtable2_za();
        },
    });
}

var window_no_za = function () {
    $("#window").dxPopup({
        showTitle: true,
        maxWidth: 500,
        maxHeight: 300,
        title: '拒绝',
        visible: false,
        WindowScroll: true,
        resizeEnabled: true,
        onHiding: function () {
            setToolbar_za();
            loadtable2_za();
        },
    });
}

var window_no_black = function () {
    $("#window").dxPopup({
        showTitle: true,
        maxWidth: 500,
        maxHeight: 300,
        title: '拒绝并拉黑',
        visible: false,
        WindowScroll: true,
        resizeEnabled: true,
        onHiding: function () {
            setToolbar_za();
            loadtable2_za();
        },
    });
}

var box_ok_value_za = function (selectobj, channel) {
    $("#pro_check_name").html(selectobj.productName);
    $("#per_check_name").html(selectobj.name);
    // 增加保存按钮
    $("#submit_ok").dxButton(
        {
            text: "确定",
            hint: "确认发送",
            icon: "todo",
            disabled: false,
            onClick: function () {
                $("#submit_ok").dxButton("instance").option("disabled", true);
                var conmitdata = {
                    borrId: selectobj.id,
                    userNum: usernum,
                    payChannel:channel
                };
                $.ajax({
                    type: "POST",
                    url: "review/pay.action",
                    data: conmitdata,
                    success: function (msg) {
                        if(msg.code == 200){
                            $("#window_ok").dxPopup({
                                visible : false,
                            });
                            setToolbar_za();
                            loadtable2_za();
                        }
                        showMessage(msg.msg);
                        $("#submit_ok").dxButton("instance").option("disabled", false);
                    }
                });
            }
        });
};

var box_no_value_za = function (selectobj) {
    var reason = "";

    $("#reason").dxTextArea({
        placeholder: "必填",
        height: 100,
        value: reason,
        showClearButton: false,
        onValueChanged: function (data) {
            reason = data.value;
        }
    });

    // 增加保存按钮
    $("#submit_no").dxButton({
        text: "确定",
        hint: "确认发送",
        icon: "todo",
        disabled: false,
        onClick: function () {
            auditBorrList(selectobj.id, reason, "review/contract/reject.action" );
        }
    });
}

var box_black_value_za = function (selectobj) {
    var reason = "";

    $("#reason").dxTextArea({
        placeholder: "必填",
        height: 100,
        value: reason,
        showClearButton: false,
        onValueChanged: function (data) {
            reason = data.value;
        }
    });

    // 增加保存按钮
    $("#submit_no").dxButton({
        text: "确定",
        hint: "确认发送",
        icon: "todo",
        disabled: false,
        onClick: function () {
            auditBorrList(selectobj.id, reason, "review/contract/black.action" );
        }
    });
}

var loadPayChannels = function(){
    $.post("loanManagement/queryPayChannels.action",function (data) {
        if(data.code==1){
            $(data.object).each(function(){
                $("#selectPayChannel").append("<option value='"+this.codeCode+"'>"+this.meaning+"</option>");
            })
        }
    })
}