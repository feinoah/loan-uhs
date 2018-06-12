package com.jhh.jhs.loan.manage.controller.loan;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.jhh.jhs.loan.api.loan.BankService;
import com.jhh.jhs.loan.api.loan.RepaymentPlanService;
import com.jhh.jhs.loan.api.manager.ManageInfoService;
import com.jhh.jhs.loan.common.util.ExcelUtils;
import com.jhh.jhs.loan.entity.app.BankVo;
import com.jhh.jhs.loan.entity.app.NoteResult;
import com.jhh.jhs.loan.entity.app.Product;
import com.jhh.jhs.loan.entity.common.Constants;
import com.jhh.jhs.loan.entity.loan.Collectors;
import com.jhh.jhs.loan.entity.loan.ReceiptUsers;
import com.jhh.jhs.loan.entity.manager.*;
import com.jhh.jhs.loan.entity.manager_vo.LoanManagementVo;
import com.jhh.jhs.loan.manage.controller.BaseController;
import com.jhh.jhs.loan.manage.entity.*;
import com.jhh.jhs.loan.manage.service.common.ExportConcurrentService;
import com.jhh.jhs.loan.manage.service.loan.CollectorsService;
import com.jhh.jhs.loan.manage.service.loan.LoanManagementService;
import com.jhh.jhs.loan.manage.service.loan.RepaymentService;
import com.jhh.jhs.loan.manage.utils.QueryParamUtils;
import com.jhh.pay.driver.pojo.BankBaseInfo;
import org.apache.commons.lang.StringUtils;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.vo.NormalExcelConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//贷后管理
@Controller
@RequestMapping("/loanManagement")
public class LoanManagementController extends BaseController{

    @Autowired
    private ManageInfoService manageInfoService;
    @Autowired
    private CollectorsService collectorsLevelService;
    @Autowired
    private LoanManagementService loanManagementService;
    @Autowired
    private RepaymentPlanService repaymentPlanService;
    @Autowired
    private ExportConcurrentService exportConcurrentService;
    @Autowired
    private BankService bankService;
    @Autowired
    private RepaymentService repaymentService;

    /**
     * 查询减免金额
     */
    @RequestMapping(value = "/queryReduceAmount", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> queryReduceAmount(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>(16);
        String borrId = request.getParameter("contractId");
        if (StringUtils.isEmpty(borrId)) {
            result.put("code", -100);
            result.put("info", "参数有误，contractId为空");
            return result;
        }

        PageInfo<LoanManagementVo> pageInfo = loanManagementService.selectLoanManagementInfo(request, borrId);
        if (null == pageInfo) {
            result.put("code", -100);
            result.put("info", String.format("查询失败，contractId【%s】, pageInfo【%s】", borrId, pageInfo));
            return result;
        }

        List<LoanManagementVo> pageList = pageInfo.getList();
        if (pageList.isEmpty() || pageList.size() < 1) {
            result.put("code", -100);
            result.put("info", String.format("查询失败，contractId【%s】, pageInfo【%s】, pageList【%s】", borrId, pageInfo, pageList));
            return result;
        }

        Float reduceAmount = 0f;
        LoanManagementVo loanManagementVo = pageInfo.getList().get(0);
        // 合同逾期天数 > 7
        if (loanManagementVo.getBedueDays() > 7) {
            reduceAmount = Float.parseFloat(loanManagementVo.getRepayAmount()) - Float.parseFloat(loanManagementVo.getReduceAmount()) >= Float.parseFloat(loanManagementVo.getPayAmount()) ? Float.parseFloat(loanManagementVo.getSurplusTotalAmount()) : (Float.parseFloat(loanManagementVo.getSurplusTotalAmount()) + Float.parseFloat(loanManagementVo.getRepayAmount()) - Float.parseFloat(loanManagementVo.getLoanAmount()) - Float.parseFloat(loanManagementVo.getReduceAmount()));
            result.put("code", 0);
            result.put("info", String.format("查询成功，contractId【%s】, bedueDays【%s】, reduceAmount【%s】", borrId, loanManagementVo.getBedueDays(), reduceAmount));
            result.put("data", String.format("%.2f", reduceAmount));
            return result;
        }

        // 合同逾期天数 < 1 (未逾期，0)
        if (loanManagementVo.getBedueDays() < 1) {
            result.put("code", 0);
            result.put("info", String.format("查询成功，contractId【%s】, bedueDays【%s】, reduceAmount【%s】", borrId, loanManagementVo.getBedueDays(), reduceAmount));
            result.put("data", String.format("%.2f", reduceAmount));
            return result;
        }

        // 合同逾期天数 < 8 (违约金 - 已减免金额)
        List<RepaymentPlan> plans = repaymentPlanService.getOverdueRepaymentPlan(borrId);
        if (plans.isEmpty() || plans.size() < 1) {
            result.put("code", 0);
            result.put("info", String.format("查询成功，contractId【%s】, bedueDays【%s】, repaymentPlans【%s】, reduceAmount【%s】", borrId, loanManagementVo.getBedueDays(), plans, reduceAmount));
            result.put("data", String.format("%.2f", reduceAmount));
            return result;
        }

        RepaymentPlan plan = plans.get(0);
        if (plans.size() > 1) {
            plan = plans.get(plans.size() - 1);
        }
        reduceAmount = plan.getPenalty().subtract(new BigDecimal(loanManagementVo.getReduceAmount())).floatValue();
        if (Float.compare(reduceAmount, 0) < 0) {
            reduceAmount = 0f;
        }
        result.put("code", 0);
        result.put("info", String.format("查询成功，contractId【%s】, bedueDays【%s】, reduceAmount【%s】", borrId, loanManagementVo.getBedueDays(), reduceAmount));
        result.put("data", String.format("%.2f", reduceAmount));
        return result;
    }

    /**
     * 查询贷后管理
     * @param request
     * @return
     */
    @RequestMapping(value = "/loan", method = RequestMethod.GET)
    @ResponseBody
    public String queryLoans(HttpServletRequest request){
        PageInfo<LoanManagementVo> result = loanManagementService.selectLoanManagementInfo(request, null);
     return JSON.toJSONString(result);
     }

    /**
     * 查询贷后管理详情
     * @param request
     * @return
     */
    @RequestMapping(value = "/loan/detail", method = RequestMethod.GET)
    @ResponseBody
    public String queryLoans(HttpServletRequest request, @RequestParam String contractKey, @RequestParam String from){

        PageInfo<LoanManagementVo> result = loanManagementService.selectLoanManagementInfo(request, contractKey, from);
        return JSON.toJSONString(result);
    }

     /**
     * 导出贷后管理
     * @param request
     * @return
     */
    @RequestMapping(value = "/loan/export")
    @ResponseBody
    public void exportLoans(HttpServletRequest request, HttpServletResponse response){
        QueryParamUtils.buildPage(request, 50000);
        PageInfo<LoanManagementVo> result = loanManagementService.selectLoanManagementInfo(request, null);
        Map<String, Object> map = new HashMap();
        map.put(NormalExcelConstants.FILE_NAME, "贷后管理" + new SimpleDateFormat("yyyy-MM-dd hh-mm-ss").format(new Date()));
        map.put(NormalExcelConstants.CLASS, LoanManagementVo.class);
        map.put(NormalExcelConstants.DATA_LIST, result.getList());
        map.put(NormalExcelConstants.PARAMS, new ExportParams());
        ExcelUtils.jeecgSingleExcel(map, request,response);
    }

    /**
     * 转件
     * @return
     */
    @RequestMapping(value="/transferLoan",produces = "application/json", method = RequestMethod.POST)
    @ResponseBody
    public Response transferLoan(String contractIds, String userId, String opUserId){

        return loanManagementService.transferLoan(contractIds,userId,opUserId);
    }

    /**
     * 查询催收人员
     * @param request
     * @return
     */
    @RequestMapping(value="/queryReceiptUsers",produces = "application/json") @ResponseBody
    public Result queryReceiptUsers(HttpServletRequest request){
        Result result = new Result();
        int offset =  Integer.valueOf(request.getParameter("skip") == null ? "0" : request.getParameter("skip"));
        int size = Integer.valueOf(request.getParameter("take") == null ? Integer.MAX_VALUE + "" : request.getParameter("take"));
        try{
            Map<String,Object> queryMap = QueryParamUtils.getargs(request.getParameterMap());


            PageInfo<ReceiptUsers> info = loanManagementService.selectReceiptUsers(queryMap , offset , size);
            result.setCode(Result.SUCCESS);
            result.setMessage("加载成功");
            result.setObject(info);
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(Result.FAIL);
            result.setMessage("加载失败");
        }
        return result;
    }

    /**
     * 查询催收人员
     * @param request
     * @return
     */
    @RequestMapping(value="/queryReceiptUsersByUser",produces = "application/json") @ResponseBody
    public Result queryReceiptUsersByUser(HttpServletRequest request,String userNo,Integer type){
        Result result = new Result();
        int offset =  Integer.valueOf(request.getParameter("skip") == null ? "0" : request.getParameter("skip"));
        int size = Integer.valueOf(request.getParameter("take") == null ? Integer.MAX_VALUE + "" : request.getParameter("take"));
        Map<String,Object> queryParams = QueryParamUtils.getargs(request.getParameterMap());
        try{
            PageInfo<Collectors> info = loanManagementService.selectReceiptUsers(queryParams,userNo,type,offset,size);
            result.setCode(Result.SUCCESS);
            result.setMessage("加载成功");
            result.setObject(info);
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(Result.FAIL);
            result.setMessage("加载失败");
        }
        return result;
    }

    /**
     * 查询催收人员
     * @param request
     * @return
     */
    @RequestMapping(value="/queryAllReceiptUsers",produces = "application/json") @ResponseBody
    public Result queryAllReceiptUsers(HttpServletRequest request){
        Result result = new Result();
        int offset =  Integer.valueOf(request.getParameter("skip") == null ? "0" : request.getParameter("skip"));
        int size = Integer.valueOf(request.getParameter("take") == null ? Integer.MAX_VALUE + "" : request.getParameter("take"));
        try{
            PageInfo<Collectors> info = loanManagementService.selectReceiptUsers(request.getParameter("type"), offset , size);
            result.setCode(Result.SUCCESS);
            result.setMessage("加载成功");
            result.setObject(info);
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(Result.FAIL);
            result.setMessage("加载失败");
        }
        return result;
    }

    /**
     * 提交催收备注
     * @param remark
     * @return
     */
    @RequestMapping(value="/collectionRemark",produces = "application/json") @ResponseBody
    public Result collectionRemark(HttpServletRequest request,@ModelAttribute("reamrk")CollectorsRemark remark){
        Result result = new Result();
        try{
            remark.setCreateDate(new Date());
            int state = loanManagementService.addCollectionRemark(remark);
            if(state > 0){
                result.setCode(Result.SUCCESS);
                result.setMessage("催收备注提交成功");
            }else{
                result.setCode(Result.FAIL);
                result.setMessage("备注提交失败");
            }
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(Result.FAIL);
            result.setMessage("备注提交失败");
        }
        return result;
    }

    /**
     * 扣款
     * @param askCollection
     * @return
     */
    @RequestMapping(value="/askCollection",produces = "application/json") @ResponseBody
    public Result askCollection(@ModelAttribute("askCollection") AskCollection askCollection){
        Result result = new Result();
        try{
            return loanManagementService.askCollection(askCollection);
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(Result.FAIL);
            result.setMessage("提交扣款失败!");
        }
        return result;
    }

    /**
     * 查询催收信息
     * @param request
     * @return
     */
    @RequestMapping(value="/queryCollectorsInfo",produces = "application/json") @ResponseBody
    public Result queryCollectorsInfo(HttpServletRequest request){
        Result result = new Result();
        int offset =  Integer.valueOf(request.getParameter("skip") == null ? "0" : request.getParameter("skip"));
        int size = Integer.valueOf(request.getParameter("take") == null ? Integer.MAX_VALUE + "" : request.getParameter("take"));
        String userNo = StringUtils.isEmpty(request.getParameter("userNo")) ? "SYSTEM" : request.getParameter("userNo");

        try{
            Map<String,Object> queryMap = QueryParamUtils.getargs(request.getParameterMap());

            PageInfo<CollectorsListVo> info = loanManagementService.selectCollectorsInfo(queryMap,offset,size,userNo);
            result.setCode(Result.SUCCESS);
            result.setMessage("加载成功");
            result.setObject(info);
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(Result.FAIL);
            result.setMessage("加载失败");
        }
        return result;
    }

    @RequestMapping(value="/queryExportCount",produces = "application/json") @ResponseBody
    public Result queryExportCount(HttpServletRequest request){
        Result result = new Result();
        try{
            Map<String,Object> queryMap = QueryParamUtils.getargs(request.getParameterMap());
            int count = loanManagementService.queryExportCount(queryMap);
            result.setCode(Result.SUCCESS);
            result.setMessage("加载成功");
            result.setObject(count);
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(Result.FAIL);
            result.setMessage("加载失败");
        }
        return result;
    }

    @RequestMapping(value="/exportLoans")
    public void exportLoans(HttpServletRequest request, HttpServletResponse response,Integer count,String userNo){
        try{
            if( Constants.DOWNLOAD_MAX_ITEMS <= count.intValue()){
                response.setContentType("text/html;charset=utf-8");
                response.getWriter().write("<!DOCTYPE html><head><script> if(confirm('一次性下数据量多于5W条!')){window.location.href = document.referrer;}else{window.location.href = document.referrer;} </script></head>");
                return;
            }
            if(!exportConcurrentService.getExportToken()){
                response.setContentType("text/html;charset=utf-8");
                response.getWriter().write("<!DOCTYPE html><head><script> if(confirm('下载人数过多,请稍后重试!')){window.location.href = document.referrer;}else{window.location.href = document.referrer;} </script></head>");
                return;
            }
            Map<String,Object> queryMap = QueryParamUtils.getargs(request.getParameterMap());
            List<LoanManagementVo> loanManagements = loanManagementService.selectExportData(queryMap,count,userNo);
            Map<String, Object> map = new HashMap<>();
            map.put(NormalExcelConstants.FILE_NAME, "贷后管理" + new SimpleDateFormat("yyyy-MM-dd hh-mm-ss").format(new Date()));
            map.put(NormalExcelConstants.CLASS, LoanManagementVo.class);
            map.put(NormalExcelConstants.DATA_LIST, loanManagements);
            map.put(NormalExcelConstants.PARAMS, new ExportParams());

            ExcelUtils.jeecgSingleExcel(map,request,response);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 检查是否可以下载及检查最大下载条数
     * @return
     */
    @RequestMapping(value = "/checkCanDownload",produces = "application/json")  @ResponseBody
    public Result checkCanDownload(){
        Result result = new Result();
        try{
            Download download = loanManagementService.checkCanDownload();
            result.setCode(Result.SUCCESS);
            result.setMessage("检查成功");
            result.setObject(download);
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(Result.FAIL);
            result.setMessage("检查失败");
        }
        return result;
    }

    @RequestMapping(value = "/loanProducts",produces = "application/json")  @ResponseBody
    public Result loanProducts(){
        Result result = new Result();
        try{
            List<Product> products = loanManagementService.selectProducts();
            result.setCode(Result.SUCCESS);
            result.setMessage("查询成功");
            result.setObject(products);
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(Result.FAIL);
            result.setMessage("查询失败");
        }
        return result;
    }

    @RequestMapping(value = "/queryOrderType",produces = "application/json")
    @ResponseBody
    public Result getOrderTypes(){
        Result result = new Result();
        try{
            List<CodeValue> orderTypes = manageInfoService.getCodeValueListByCode("order_type");
            List<CodeValue> payCenterChannels = manageInfoService.getCodeValueListByCode("pay_center_channel");
            result.setCode(Result.SUCCESS);
            result.setMessage("查询成功");

            JSONObject orderTypeObject = new JSONObject(true);

            orderTypeObject.put("6", "减免");
            orderTypeObject.put("7", "线下还款");

            orderTypeObject.put("1", "放款(银生宝)");
            orderTypeObject.put("5", "主动还款(银生宝)");
            orderTypeObject.put("4", "还款(代收)(银生宝)");
            orderTypeObject.put("8", "批量代扣(银生宝)");
            orderTypeObject.put("11", "放款(海尔)");
            orderTypeObject.put("12", "主动还款(海尔)");
            orderTypeObject.put("13", "还款(代收)(海尔)");
            orderTypeObject.put("14", "批量代扣(海尔)");

            for (int i = 0; i < payCenterChannels.size(); i++) {
                CodeValue value = payCenterChannels.get(i);
                for (int j = 0; j < orderTypes.size(); j++) {
                    CodeValue orderType = orderTypes.get(j);
                    Integer orderTypeValue = Integer.valueOf(orderType.getCodeCode());
                    if (orderTypeValue < 15 || orderTypeValue > 18) {
                        continue;
                    }
                    if(!StringUtils.isEmpty(value.getCodeCode())) {
                        orderTypeObject.put(orderType.getCodeCode() + "/" + value.getCodeCode(), orderType.getMeaning() + "(" + value.getMeaning() + ")");
                    }
                    if(!orderTypeObject.containsKey(orderType.getCodeCode())) {
                        orderTypeObject.put(orderType.getCodeCode(), orderType.getMeaning() + "(支付中心)");
                    }
                }
            }

            result.setObject(orderTypeObject);
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(Result.FAIL);
            result.setMessage("查询失败");
        }
        return result;
    }

    @RequestMapping(value = "/queryRepayType",produces = "application/json")
    @ResponseBody
    public Result getRepayTypes(){
        Result result = new Result();
        try{
            List<CodeValue> orderTypes = manageInfoService.getCodeValueListByCode("order_type");
            List<CodeValue> payCenterChannels = manageInfoService.getCodeValueListByCode("pay_center_channel");
            result.setCode(Result.SUCCESS);
            result.setMessage("查询成功");

            JSONObject repayTypeObject = new JSONObject(true);

            repayTypeObject.put("6", "减免");
            repayTypeObject.put("7", "线下还款");

            repayTypeObject.put("5", "主动还款(银生宝)");
            repayTypeObject.put("4", "还款(代收)(银生宝)");
            repayTypeObject.put("8", "批量代扣(银生宝)");
            repayTypeObject.put("12", "主动还款(海尔)");
            repayTypeObject.put("13", "还款(代收)(海尔)");
            repayTypeObject.put("14", "批量代扣(海尔)");

            for (int i = 0; i < payCenterChannels.size(); i++) {
                CodeValue value = payCenterChannels.get(i);
                for (int j = 0; j < orderTypes.size(); j++) {
                    CodeValue orderType = orderTypes.get(j);
                    Integer orderTypeValue = Integer.valueOf(orderType.getCodeCode());
                    if (orderTypeValue < 16 || orderTypeValue > 18) {
                        continue;
                    }
                    if(!StringUtils.isEmpty(value.getCodeCode())) {
                        repayTypeObject.put(orderType.getCodeCode() + "/" + value.getCodeCode(), orderType.getMeaning() + "(" + value.getMeaning() + ")");
                    }
                    if(!repayTypeObject.containsKey(orderType.getCodeCode())) {
                        repayTypeObject.put(orderType.getCodeCode(), orderType.getMeaning() + "(支付中心)");
                    }
                }
            }

            result.setObject(repayTypeObject);
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(Result.FAIL);
            result.setMessage("查询失败");
        }
        return result;
    }

    @RequestMapping("/resetTokenQueue")
    @ResponseBody
    public String resetTokenQueue(HttpServletRequest request, HttpServletResponse response,long interval,int volume){
        try {
            String interval1 = "";
            String volume1 = "";
            if(interval > 0){
                 interval1 = exportConcurrentService.setFillInterval(interval);
            }
            if(volume > 0){
                 volume1 = exportConcurrentService.setFillVolume(volume);
            }
            return "set tokenQ`s volume:"+volume1+",interval period:"+interval1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "设置tokenQ失败!";
    }

    /**
     * 查询绑定银行卡主卡
     * @param userId 用户ID
     * @return
     */
    @RequestMapping(value="/queryMainBankInfo",produces = "application/json") @ResponseBody
    public Result queryMainBankInfo(Integer userId){
        Result result = new Result();
        try{
            BankVo bankVo = loanManagementService.selectMainBankByUserId(userId);
            if(bankVo == null){
                result.setCode(Result.FAIL);
                result.setMessage("查询银行卡主卡失败，该用户未绑定主卡");
                return result;
            }
            NoteResult bankList = bankService.getBankList();
            BankBaseInfo[]  BankBaseInfo = (BankBaseInfo[]) bankList.getData();
            JSONObject object = new JSONObject();
            object.put("bankInfo",bankVo);
            object.put("banks",BankBaseInfo);
            result.setCode(Result.SUCCESS);
            result.setMessage("查询银行卡主卡成功");
            result.setObject(object);
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(Result.FAIL);
            result.setMessage("查询银行卡主卡失败");
        }
        return result;
    }

    /**
     * 申请减免/线下还款
     * @param contractId 合同号
     * @param reduce 减免金额/还款金额
     * @return
     */
    @RequestMapping(value="/reduceLoan",produces = "application/json") @ResponseBody
    public Result reduceLoan(String contractId,String reduce,String remark,String type,String userName, String bedueDays){
        Result result = new Result();
        if(StringUtils.isEmpty(contractId)){
            result.setCode(Result.FAIL);
            result.setMessage("合同号为空");
        }else if(StringUtils.isEmpty(reduce)){
            result.setCode(Result.FAIL);
            result.setMessage("金额为空");
        }else{
            try{
                result = loanManagementService.reduceLoan(contractId,reduce,remark,type,userName,bedueDays);
                if(result.getCode() == Result.SUCCESS){
                    result.setCode(Result.SUCCESS);
                    result.setMessage("操作成功");
                }else{
                    result.setCode(Result.FAIL);
                    result.setMessage(result.getMessage());
                }
            }catch (Exception e){
                e.printStackTrace();
                result.setCode(Result.FAIL);
                result.setMessage("操作失败");
            }
        }
        return result;
    }


    /**
     * 查询批量代扣专用数据
     * @param request
     * @return
     */
    @RequestMapping(value = "/queryBatchReduce", method = RequestMethod.GET)
    @ResponseBody
    public String queryBatchReduce(HttpServletRequest request){
        QueryParamUtils.buildPage(request);
        List result = loanManagementService.queryBatchReduce(request.getParameterMap());
        return JSON.toJSONString(new PageInfo(result));
    }

    /**
     * 查询批量代扣流水数据
     * @param request
     * @return
     */
    @RequestMapping(value = "/queryBatchReduceList", method = RequestMethod.GET)
    @ResponseBody
    public String queryBatchReduceList(HttpServletRequest request) {
        QueryParamUtils.buildPage(request);
        List result = loanManagementService.getBatchReduceList(request.getParameterMap());
        return JSON.toJSONString(new PageInfo(result));
    }

    /**
     * 批量扣款
     * @param reduceData
     * @return
     */
    @RequestMapping(value = "/batchCollection", produces = "application/json")
    @ResponseBody
    public Result batchCollection(String createUser, String reduceData, String reduceMoney, String deductionsType, String payChannel){

        if(StringUtils.isEmpty(reduceData)) {
            Result result = new Result();
            result.setCode(Result.FAIL);
            result.setMessage("代扣信息为空");
            return result;
        }

        List<LoanManagementVo> askCollections = JSON.parseArray(reduceData, LoanManagementVo.class);
        return loanManagementService.batchCollection(askCollections, reduceMoney, createUser, deductionsType, payChannel);
    }


    /**
     * 查询扣款渠道
     * @return
     */
    @RequestMapping(value="/queryPayChannels",produces = "application/json") @ResponseBody
    public Result queryPayChannels(){
        Result result = new Result();
        try{
            List<CodeValue> info = loanManagementService.selectPayChannels();
            result.setCode(Result.SUCCESS);
            result.setMessage("加载成功");
            result.setObject(info);
        }catch (Exception e){
            e.printStackTrace();
            result.setCode(Result.FAIL);
            result.setMessage("加载失败");
        }
        return result;
    }

    /**
     * 导出催收备注
     * @param request
     * @param response
     * @param userNo
     * @param count
     */
    @RequestMapping(value="/exportLoansRemark")
    public void exportLoansRemark(HttpServletRequest request, HttpServletResponse response,String userNo,Integer count){
        try{
            if( Constants.DOWNLOAD_MAX_ITEMS <= count.intValue()){
                response.setContentType("text/html;charset=utf-8");
                response.getWriter().write("<!DOCTYPE html><head><script> if(confirm('一次性下数据量多于5W条!')){window.location.href = document.referrer;}else{window.location.href = document.referrer;} </script></head>");
                return;
            }
            if(!exportConcurrentService.getExportToken()){
                response.setContentType("text/html;charset=utf-8");
                response.getWriter().write("<!DOCTYPE html><head><script> if(confirm('下载人数过多,请稍后重试!')){window.location.href = document.referrer;}else{window.location.href = document.referrer;} </script></head>");
                return;
            }
            Map<String,Object> queryMap = QueryParamUtils.getParams(request.getParameterMap());
            Map<String, Object> map = new HashMap();
            if(collectorsLevelService.isOutWorker(userNo)){
                List<LoansRemarkOutVo> loansRemarkOutVoList = loanManagementService.selectExportLoansRemarkForOutWorkers(queryMap, userNo);
                map.put(NormalExcelConstants.CLASS, LoansRemarkOutVo.class);
                map.put(NormalExcelConstants.DATA_LIST, loansRemarkOutVoList);
            }else{
                List<LoansRemarkVo> loansRemark = loanManagementService.selectExportLoansRemarkVo(queryMap,userNo);
                map.put(NormalExcelConstants.CLASS, LoansRemarkVo.class);
                map.put(NormalExcelConstants.DATA_LIST, loansRemark);
            }
            map.put(NormalExcelConstants.FILE_NAME, "催收备注" + new SimpleDateFormat("yyyy-MM-dd hh-mm-ss").format(new Date()));
            map.put(NormalExcelConstants.PARAMS, new ExportParams());
            ExcelUtils.jeecgSingleExcel(map,request,response);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 根据合同编号查询还款计划表中的剩余应付
     */
    @RequestMapping(value="/surplusTotalAmountNum",produces = "application/json") @ResponseBody
    public Result surplusTotalAmountNum(String contractId) {
        Result result = new Result();
        try {
            String surplusTotalAmountNum = repaymentService.getSurplusTotalAmountNum(contractId);
            result.setCode(200);
            result.setMessage("查询成功");
            result.setObject(surplusTotalAmountNum);
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(Result.FAIL);
            result.setMessage("查询失败");
        }
        return result;
    }
}
