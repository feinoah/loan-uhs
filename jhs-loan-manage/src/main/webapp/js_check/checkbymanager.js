var checkbymanager = function () {
    loadUserId_cbm();
    loadtable2_zb();
    window_find_zb();
    window_ok_zb();
    setToolbar_zb(true);
    window_no_zb();
    window_transfer_zb();
};

var loadUserId_cbm = function () {
    checkPageEnabled("ym-zb");
};

var setToolbar_zb = function (init) {

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
        visible: !disableButton("ym-zb", 0),
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
        visible: !disableButton("ym-zb", 1),
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
            box_ok_value_zb(selectobj_param[0], payChannel);
        }
    });
    $("#tool_no").dxButton({
        hint: "拒绝",
        text: "拒绝",
        icon: "close",
        disabled: true,
        visible: !disableButton("ym-zb", 2),
        onClick: function () {

            var dataGrid = $('#userTable').dxDataGrid('instance');
            var selectobj_param = dataGrid.getSelectedRowsData();
            if (selectobj_param.length > 1) {
                showMessage("只能选择一条数据！");
            } else {
                $("#window").dxPopup({
                    visible: true,
                });
                box_no_value_zb(selectobj_param[0]);
            }
        }
    });

    $("#tool_transfer").dxButton({
        hint: "转件",
        text: "转件",
        icon: "revert",
        disabled: true,
        visible: !disableButton("ym-zb", 3),
        onClick: function () {

            var dataGrid = $('#userTable').dxDataGrid('instance');
            var selectobj_param = dataGrid.getSelectedRowsData();
            var brroid_list = "";
            for (var i = 0; i < selectobj_param.length; i++) {
                brroid_list += selectobj_param[i].id + ",";
            }
            box_transfer_value_zb(brroid_list);

        }
    });

    $("#tool_cancel").dxButton({
        hint: "取消借款",
        text: "取消借款",
        icon: "revert",//canExport
        visible: !disableButton("ym-zb", 4),
        disabled: true,
        onClick: function () {

            var dataGrid = $('#userTable').dxDataGrid('instance');
            var selectobj_param = dataGrid.getSelectedRowsData();
            var brroid_list = "";
            for(var i = 0;i<selectobj_param.length;i++) {
                brroid_list += selectobj_param[i].id +",";
            }
            risk_cancel(brroid_list);
        }
    });
    $("#tool_reload").dxButton({
        hint: "刷新",
        text: "刷新",
        icon: "refresh",
        visible: !disableButton("ym-zb", 5),
        onClick: function () {
            var dataGrid = $('#userTable').dxDataGrid('instance');
            dataGrid.refresh();
        }
    });
    $("#tool_export").dxButton({
        hint: "导出",
        text: "导出",
        icon: "export",
        visible: !disableButton("ym-zb", 6),
        onClick: function (loadOptions) {
            var dataGrid = $('#userTable').dxDataGrid('instance');
            var filter = dataGrid.getCombinedFilter();
            filter = JSON.stringify(filter) == undefined ? '' : JSON.stringify(filter);
            var url = "risk/auditsforUser/export.action?filter=" + encodeURI(filter);
            exportData(url, null);
        }
    });
};

var loadtable2_zb = function () {
    DevExpress.config({
        forceIsoDateParsing: true

    });
    var orders = new DevExpress.data.CustomStore({
        load: function (loadOptions) {
            var deferred = $.Deferred(),
                args = {};
            args.filter = loadOptions.filter ? JSON.stringify(loadOptions.filter) : "";
            args.sort = loadOptions.sort ? JSON.stringify(loadOptions.sort) : "";
            args.requireTotalCount = loadOptions.requireTotalCount;
            if (loadOptions.sort) {
                args.orderby = loadOptions.sort[0].selector;
                if (loadOptions.sort[0].desc)
                    args.orderby += " desc";
            }

            args.skip = loadOptions.skip || 0;
            args.take = loadOptions.take || 10;
            $.ajax({
                url: "risk/auditsforUser.action",
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

    $("#userTable").dxDataGrid({
        dataSource: {
            store: orders
        },
        dateSerializationFormat: "yyyy-MM-dd HH:mm:ss",
        "export": {
            enabled: true,
            fileName: "Employees",
            allowExportSelectedData: true
        },
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
            mode: "multiple",
            selectAllMode: "page"
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
            pageSize: 10
        },
        pager: {
            showPageSizeSelector: true,
            allowedPageSizes: [10, 15, 30, 45, 60, 90, 120, 150, 200],
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

            $("#tool_find").dxButton({disabled: (data.selectedRowsData.length != 1) || disableButton("ym-zb", 0)});
            $("#tool_ok").dxButton({disabled: data.selectedRowsData.length != 1 || flag || disableButton("ym-zb", 1)});
            $("#tool_no").dxButton({disabled: data.selectedRowsData.length != 1 || flag || disableButton("ym-zb", 2)});
            $("#tool_transfer").dxButton({disabled: !data.selectedRowsData.length || flag || disableButton("ym-zb", 3)});
            $("#tool_cancel").dxButton({disabled: !data.selectedRowsData.length || flag || disableButton("ym-zb", 4)});
            $("#tool_ok_helibao").dxButton({disabled: data.selectedRowsData.length != 1 || flag || disableButton("ym-zb", 7)});
        },
        columns: [
            {
                dataField: "borrNum",
                caption: "合同编号",
                alignment: "center",
                allowFiltering: true,
                filterOperations: ["="],
                allowSorting: false
            }, {
                dataField: "name",
                caption: "姓名",
                alignment: "center",
                allowFiltering: true,
                filterOperations: ["="],
                allowSorting: false
            }, {
                dataField: "cardNum",
                caption: "身份证号",
                alignment: "center",
                allowFiltering: true,
                filterOperations: ["="],
                allowSorting: false
            }, {
                dataField: "phone",
                caption: "手机号码",
                alignment: "center",
                allowFiltering: true,
                filterOperations: ["="],
                allowSorting: false
            },
            {
                dataField: "productName",
                caption: "产品名称",
                alignment: "center",
                allowFiltering: true,
                filterOperations: ["="],
                allowSorting: false,
                lookup: {
                    dataSource: pruducts,
                    displayExpr: 'format'
                }, width: 125
            }, {
                dataField: "borrAmount",
                caption: "贷款金额",
                alignment: "center",
                dataType: "number",
                allowFiltering: true,
                filterOperations: ["="],
                allowSorting: false
            }, {
                dataField: "bankName",
                caption: "银行名称",
                alignment: "center",
                allowFiltering: true,
                filterOperations: ["="],
                allowSorting: false
            }, {
                dataField: "bankCard",
                caption: "银行卡号",
                alignment: "center",
                allowFiltering: true,
                filterOperations: ["="],
                allowSorting: false
            }, {
                dataField: "borrStatusValue",
                caption: "合同状态",
                alignment: "center",
                lookup: {
                    dataSource: [
                        {value: 'BS003', format: '已签约'},
                        {value: 'BS004', format: '待还款'},
                        {value: 'BS007', format: '已取消'},
                        {value: 'BS009', format: '电审未通过'},
                        {value: 'BS011', format: '放款中'},
                        {value: 'BS012', format: '放款失败'}
                    ], displayExpr: 'format'
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
                        {value: 'BS014', format: '提前结清'}

                    ], displayExpr: 'format'
                }
            }
            , {
                dataField: "baikeluStatus",
                caption: "自动电呼",
                alignment: "center",
                lookup: {
                    //通过、拒绝、未接通、未完成、非本人
                    dataSource: [
                        /*{value: '8888', format: '未拨打'},
                        {value: '1', format: '拨打中'},
                        {value: '2', format: '通过'},
                        {value: '3', format: '拒绝'},
                        {value: '4', format: '未接通'},
                        {value: '5', format: '非本人'},*/
                        //{value: '8888', format: '未拨打'},

                        {value: '1', format: '拨打中'},
                        {value: '2', format: '未完成'},
                        {value: '3', format: '通过'},
                        {value: '4', format: '拒绝'},
                        {value: '5', format: '未接通'},
                        {value: '6', format: '非本人'},
                    ], displayExpr: 'format', valueExpr: 'value'
                }
            }, {
                dataField: "reason",
                caption: "人工审核理由",
                alignment: "center",
                allowFiltering: false,
                allowSorting: false
            },
            {
                dataField: "isManualValue",
                caption: "是否人工审核",
                alignment: "center",
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
                allowFiltering: false,
                allowSorting: false
            },
            {
                dataField: "emplloyeeName",
                caption: "审核人",
                alignment: "center",
                filterOperations: ["=", "between"]
            },
            {
                dataField: "makeborrDate",
                caption: "签约时间",
                alignment: "center",
                dataType: "date",
                filterOperations: ["=", "between"]
            }]
    });
};

// 查看详情
var window_find_zb = function () {
    $("#window_find").dxPopup({
        showTitle: true,
        title: '详情',
        width: "95%",
        height: "88%",
        visible: false,
        WindowScroll: true,
        resizeEnabled: true,
        onHiding: function () {
            loadtable2_zb();
        }
    });
};
var window_ok_zb = function () {
    $("#window_ok").dxPopup({
        showTitle: true,
        maxWidth: 400,
        maxHeight: 220,
        title: '放款',
        visible: false,
        WindowScroll: true,
        resizeEnabled: true,
        onHiding: function () {
            setToolbar_zb();
            loadtable2_zb();
        }
    });
};

var window_no_zb = function () {
    $("#window").dxPopup({
        showTitle: true,
        maxWidth: 500,
        maxHeight: 300,
        title: '拒绝',
        visible: false,
        WindowScroll: true,
        resizeEnabled: true,
        onHiding: function () {
            setToolbar_zb();
            loadtable2_zb();
        }
    });
};

var window_transfer_zb = function () {
    $("#window_transfer").dxPopup({
        showTitle: true,
        maxWidth: 500,
        maxHeight: 300,
        title: '转件',
        visible: false,
        WindowScroll: true,
        resizeEnabled: true,
        onHiding: function () {
            setToolbar_zb();
            loadtable2_zb();
        }
    });
};

var box_ok_value_zb = function (selectobj, channel) {
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
                    payChannel: channel
                };
                $("#window_ok").dxPopup({
                    visible: false,
                });
                $.ajax({
                    type: "POST",
                    url: "review/pay.action",
                    data: conmitdata,
                    success: function (msg) {
                        if (msg.code == 200) {
                            $("#window_ok").dxPopup({
                                visible: false,
                            });
                            setToolbar_zb();
                            loadtable2_zb();
                        }
                        showMessage(msg.msg);
                        $("#submit_ok").dxButton("instance").option("disabled", false);
                    }
                });
            }
        });
};
var box_no_value_zb = function (selectobj) {
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
            auditBorrList(selectobj.id, reason, "review/contract/reject.action");
        }
    });
}

var box_transfer_value_zb = function (borrIds) {
    $("#window_transfer").dxPopup({
        visible: true,
    });
    var transfer = "";
    $.ajax({
        type: "GET",
        url: "risk/reviewers.action",
        data: {
            status: "y",
        },
        success: function (msg) {
            $('#transfer').dxSelectBox({
                dataSource: msg,
                placeholder: "必填",
                valueExpr: 'employNum',
                displayExpr: 'emplloyeeName',
                showClearButton: true,
                onValueChanged: function (e) {
                    transfer = e.value;
                }
            });
        }
    });

    // 增加保存按钮
    $("#submit_transfer").dxButton({
        text: "确定",
        hint: "确认发送",
        icon: "todo",
        disabled: false,
        onClick: function () {
            riskTransfer(borrIds, transfer);
        }
    });
}

function risk_cancel(selectobj) {

    $.ajax({
        url: "/loan-manage/cancel/borrList.action",
        data: {borrowIds:selectobj},
        type: 'POST',
        timeout: 50000,
        dataType:"json",
        success: function (result) {
            if (result.code === 200) {
                alert(result.msg);
                var dataGrid = $('#userTable').dxDataGrid('instance');
                dataGrid.refresh();
            } else {
                showMessage(result.msg);
            }
        },
        error: function () {
            // deferred.reject("Data Loading Error");
        }
    });

    // $.post(
    //     "borrList/id.action",
    //     {
    //         id: borrowId
    //     },
    //     function (msg) {
    //         if (msg.code == 200) {
    //             alert(msg.msg);
    //             var dataGrid = $('#userTable').dxDataGrid('instance');
    //             dataGrid.refresh();
    //         } else {
    //             showMessage(msg.msg);
    //         }
    //     }, 'json'
    // );
}