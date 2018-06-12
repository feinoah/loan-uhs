package com.jhh.jhs.loan.service.contract;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONObject;
import com.jhh.jhs.loan.api.contract.ElectronicContractService;
import com.jhh.jhs.loan.common.enums.ContractDataCodeEnum;
import com.jhh.jhs.loan.common.util.*;
import com.jhh.jhs.loan.entity.app.BorrowList;
import com.jhh.jhs.loan.entity.app.NoteResult;
import com.jhh.jhs.loan.entity.app.Product;
import com.jhh.jhs.loan.entity.common.Constants;
import com.jhh.jhs.loan.entity.contract.Contract;
import com.jhh.jhs.loan.entity.contract.IdEntity;
import com.jhh.jhs.loan.mapper.app.BorrowListMapper;
import com.jhh.jhs.loan.mapper.app.CodeValueMapper;
import com.jhh.jhs.loan.mapper.contract.ContractMapper;
import com.jhh.jhs.loan.mapper.manager.RepaymentPlanMapper;
import com.jhh.jhs.loan.mapper.product.ProductMapper;
import com.jinhuhang.settlement.service.SettlementAPI;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import tk.mybatis.mapper.entity.Example;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by wanzezhong on 2017/11/23.
 *
 * @author carl.wan
 */
@Slf4j
@Service
public class ElectronicContractServiceImpl implements ElectronicContractService {

    @Autowired
    private BorrowListMapper borrowListMapper;


    @Autowired
    private CodeValueMapper codeValueMapper;

    @Autowired
    private ContractMapper contractMapper;

    @Autowired
    private RepaymentPlanMapper repaymentPlanMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private SettlementAPI settlementAPI;
    @Value("${contract.url}")
    private String contractUrl;

    @Value("${contract.url.generate}")
    private String generateUrl;

    @Value("${contract.url.preview}")
    private String previewUrl;

    @Value("${contract.url.download}")
    private String downloadUrl;

    @Value("${contract.token}")
    private String token;

//    @Value("${contract.productId}")
//    private String productId;
    @Value("${dfsUrl}")
    private String dfsUrl;
    @Value("${baseUrl}")
    private String baseUrl;


    @Override
    public String createElectronicContract(Integer borrId) {
        Integer contractStatus = Constants.ContractStatus.EXCEPTION;
        String result = "";
        try {
            //1..获取产品相关信息product
            Product product=queryProductByBorrId(borrId);
            if(null==product){
                return result;
            }
            //2.获取应执行的封装数据data
            Map data=queryContractDataByProdId(product,borrId);
            if(null==data){
                return result;
            }
            Map param = new HashMap();
            param.put("token", token);
            param.put("productId", product.getContractPrdouctId());
            param.put("contractNo", data.get("#borrNum#"));
            param.put("cardNo", data.get("#CardID#"));
            param.put("name", data.get("#name#"));
            param.put("mobile", data.get("#phone#"));
            param.put("data", JSONObject.toJSONString(data));
            result = HttpUtils.sendPost(contractUrl + generateUrl, HttpUtils.toParam(param));
            if (Detect.notEmpty(result)) {
                JSONObject jsonResult = JSONObject.parseObject(result);
                JSONObject jsonData = jsonResult.getJSONObject("data");
                if (jsonData.getString("code").equals("10000")) {
                    contractStatus = Constants.ContractStatus.CREATEING;
                }
            }
            //创建合同
            creatContract(borrId, contractStatus, result);
        } catch (Exception e) {
            creatContract(borrId, contractStatus, e.getMessage());
            e.printStackTrace();
        }
        return result;
    }


    @Override
    public String queryElectronicContract(Integer borrId) {
        String result = "";
        //1..获取产品相关信息product
        Product product=queryProductByBorrId(borrId);
        if(null==product){
            return result;
        }
        //2.获取应执行的封装数据data
        Map data=queryContractDataByProdId(product,borrId);
        if(null==data){
            return result;
        }
        Map param = new HashMap();
        param.put("token", token);
        param.put("productId", product.getContractPrdouctId());
        param.put("serialNo", borrId);
        param.put("data", JSONObject.toJSONString(data));

        try {
            result = HttpUtils.sendPost(contractUrl + previewUrl, HttpUtils.toParam(param));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public String downElectronicContract(String borrNum) {
        Assertion.notEmpty(borrNum, "合同号不能为空");
        NoteResult noteResult = new NoteResult();
        noteResult.setCode(CodeReturn.FAIL_CODE);
        noteResult.setInfo("合同下载文件正在生成中，请两分钟后再试...");
        noteResult.setData("合同下载文件正在生成中，请两分钟后再试...");
        //查找本地合同是否存在
        Contract contract = new Contract();
        contract.setBorrNum(borrNum);
        contract = contractMapper.selectOne(contract);
        if (contract != null) {
            Product product=queryProductByBorrId(contract.getBorrId());
            if(null==product){
                noteResult.setData("合同下载文件失败，查询产品信息");
                return  JSONObject.toJSONString(noteResult);
            }
            if (contract.getStatus().equals(Constants.ContractStatus.SUCESS)) {
                noteResult.setCode(CodeReturn.SUCCESS_CODE);
                noteResult.setData(contract.getContractUrl());
                noteResult.setInfo("");
                return JSONObject.toJSONString(noteResult);
            } else {
                Map param = new HashMap();
                param.put("productId", product.getContractPrdouctId());
                param.put("contractNo", borrNum);
                try {
                    String result = HttpUtils.sendPost(contractUrl + downloadUrl, HttpUtils.toParam(param));
                    //正常生成合同查看
                    if (Detect.notEmpty(result)) {
                        JSONObject jsonResult = JSONObject.parseObject(result);
                        JSONObject jsonData = jsonResult.getJSONObject("data");
                        if (jsonData.getString("code").equals("10000")) {
                            noteResult.setCode(CodeReturn.SUCCESS_CODE);
                            noteResult.setData(jsonData.getString("data"));
                            noteResult.setInfo("成功");
                            String imageUrl = null;
                            InputStream inputStream = null;
                            try {
                                inputStream = PdfToImage.pdfToImage(jsonData.getString("data"), "png", null, true);
                                imageUrl = DFSUtil.uploadContent(dfsUrl, inputStream, "png");
                            } catch (IOException e) {
                                log.info("合同PDF转为图片失败:{}", e.getMessage());
                                e.printStackTrace();
                            } catch (Exception e) {
                                log.info("合同PDF转为图片失败:{}", e.getMessage());
                                e.printStackTrace();
                            } finally {
                                if (inputStream != null) {
                                    inputStream.close();
                                }
                            }
                            saveContract(contract.getBorrId(), Constants.ContractStatus.SUCESS, "", jsonData.getString("data"), imageUrl);
                            return JSONObject.toJSONString(noteResult);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        //未生成正常合同
        BorrowList bl = borrowListMapper.selectByBorrNum(borrNum);
        if (bl != null) {
            createElectronicContract(bl.getId());
        }

        return JSONObject.toJSONString(noteResult);
    }

    @Override
    public String callBack(String code, String url, String borrNum) throws UnsupportedEncodingException {
        //查找本地合同是否存在
        Example example = new Example(Contract.class);
        example.createCriteria().andEqualTo("borrNum", borrNum);
        List<Contract> contract = contractMapper.selectByExample(example);
        String imageUrl = null;
        if (contract != null && contract.size() > 0 && Detect.isPositive(contract.get(0).getBorrId())) {
            int status = Constants.ContractStatus.FAIL;
            if (code.equals("10000")) {
                status = Constants.ContractStatus.SUCESS;
                InputStream inputStream = null;
                try {
                    inputStream = PdfToImage.pdfToImage(url, "png", null, true);
                    imageUrl = DFSUtil.uploadContent(dfsUrl, inputStream, "png");
                } catch (IOException e) {
                    log.info("合同PDF转为图片失败:{}", e.getMessage());
                    e.printStackTrace();
                } catch (Exception e) {
                    log.info("合同PDF转为图片失败:{}", e.getMessage());
                    e.printStackTrace();
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            log.info("关闭流失败{}", e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }
            saveContract(contract.get(0).getBorrId(), status, "", url, imageUrl);
        } else {
            log.error("电子合同未找到对应订单号" + borrNum);
        }
        return null;
    }

    @Override
    public void disposeExceptionContract() {
        Example example = new Example(Contract.class);
        Set<Integer> inSet = new HashSet();
        inSet.add(Constants.ContractStatus.SUCESS);
        example.createCriteria().andNotIn("status", inSet);
        //查询失败异常合同
        List<Contract> contracts = contractMapper.selectByExample(example);
        if (Detect.notEmpty(contracts)) {
            //调用下载接口
            for (Contract constant : contracts) {
                downElectronicContract(constant.getBorrNum());
            }
        }
    }

    private Map getContractDate(Integer borrId) {
        Assertion.isPositive(borrId, "合同Id不能为空");

        IdEntity idEntity = borrowListMapper.queryIdentityById(borrId);
        BorrowList borrowList = borrowListMapper.getBorrowListByBorrId(borrId);
        Integer prodId = idEntity.getProdId();
        //1.借款金额    目前从合同表里取合同金额
        String money = String.format("%.2f", Double.valueOf(idEntity.getBorrAmount()));

        //3.借款期数
        String termNum = String.valueOf(idEntity.getTermId());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        long now = DateUtil.dateToStamp(idEntity.getPayDate());
        String borrowDate = sdf.format(new Date(now));
        //16.签订年
        String signYear = borrowDate.substring(0, 4);
        //17.签订月
        String signMonth = borrowDate.substring(5, 7);
        //18.签订日
        String signDay = borrowDate.substring(8, borrowDate.length() - 1);
        Map<String, String> map = new HashMap();
        map.put("#bankCardNo#", idEntity.getCardNum());
        //银行卡信息
        map.put("#bank#", idEntity.getBankName());
        map.put("#bank_num#", idEntity.getBankNum());
        map.put("#bankPhone#", idEntity.getBankPhone());
        map.put("#baseBorrowDate#", DateUtil.getDateString(DateUtil.getDate(idEntity.getPayDate())));
        map.put("#baseDay#", termNum);
        map.put("#baseMoney#", money);
        map.put("#borrBankNum#", idEntity.getBankNum());
        map.put("#CardID#", idEntity.getCardNum());
        //客户名
        map.put("#name#", idEntity.getName());
        map.put("#borrNum#", idEntity.getBorrNum());
        map.put("#phone#", idEntity.getPhone());
        map.put("#borrowDate#", DateUtil.getDateString(DateUtil.getDate(idEntity.getPayDate())));
        map.put("#planDate#", "999999999999999999");
        map.put("#day1#", signDay);
        map.put("#month1#", signMonth);
        map.put("#year1#", signYear);
        map.put("#email#", idEntity.getEmail());
        //手机评估价
        map.put("#evaluationPrice#", idEntity.getBorrAmount());
        map.put("#evaCapital#", NumberUtil.ToFormat(Double.valueOf(idEntity.getBorrAmount())));
        //押金
        map.put("#deposit#", idEntity.getDepositAmount());
        map.put("#depCapital#", NumberUtil.ToFormat(Double.valueOf(idEntity.getDepositAmount())));
        //实际打款金额
        map.put("#remainLoan#", idEntity.getPayAmount());
        map.put("#remCapital#", NumberUtil.ToFormat(Double.valueOf(idEntity.getPayAmount())));
        //回购金额
        map.put("#repurchase#", idEntity.getRansomAmount());
        map.put("#repCapital#", NumberUtil.ToFormat(Double.valueOf(idEntity.getRansomAmount())));
        List terms = repaymentPlanMapper.getRepaymentTermPlan(borrId.toString());
        if (terms != null && terms.size() > 0) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            for (int i = 0; i < terms.size(); i++) {
                Map term = (Map) terms.get(i);
                String date = term.get("repayDate").toString().split("\\s+")[0];
                if (i == 0) {
                    map.put("#firstTime#", date);
                    map.put("#rent#", String.format("%.2f", term.get("rentalAmount")));
                } else if (i == 1) {
                    map.put("#secondTime#", date);
                } else if (i == 2) {
                    map.put("#thirdTime#", date);
                } else if (i == 3) {
                    map.put("#finalTime#", date);
                    Float payTotalValue = new BigDecimal(borrowList.getRansomAmount()).add(((BigDecimal) term.get("rentalAmount"))).floatValue();
                    Float shouldTotalValue = new BigDecimal(payTotalValue).subtract(new BigDecimal(borrowList.getDepositAmount())).floatValue();
                    String payTotal = String.format("%.2f", payTotalValue);
                    String shouldTotal = String.format("%.2f", shouldTotalValue);
                    map.put("#totalPay#", payTotal);
                    map.put("#payable#", shouldTotal);
                }
            }
        }
        return map;
    }

    private Map getContractDateByOneTerm(Integer borrId) {
        Assertion.isPositive(borrId, "合同Id不能为空");
        IdEntity idEntity = borrowListMapper.queryIdentityById(borrId);
        BorrowList borrowList = borrowListMapper.getBorrowListByBorrId(borrId);
        Map<String, String> map = new HashMap();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
        /**************页面1********************/
        map.put("#borrNum#", borrowList.getBorrNum());
        long now = DateUtil.dateToStamp(idEntity.getPayDate());
        String borrowDate = sdf.format(new Date(now));
        //1-1.签订年
        String signYear = borrowDate.substring(0, 4);
        //1-2.签订月
        String signMonth = borrowDate.substring(5, 7);
        //1-3.签订日
        String signDay = borrowDate.substring(8, borrowDate.length() - 1);
        //a.签订日期
        map.put("#day1#", signDay);
        map.put("#month1#", signMonth);
        map.put("#year1#", signYear);
        //b.客户信息
        map.put("#name#", idEntity.getName());
        map.put("#CardID#", idEntity.getCardNum());
        map.put("#phone#", idEntity.getPhone());
        //c.手机评估价
        map.put("#evaluationPrice#", idEntity.getBorrAmount());
        map.put("#evaCapital#", NumberUtil.ToFormat(Double.valueOf(idEntity.getBorrAmount())));
        //d.服务费
        map.put("#serviceFee#", idEntity.getServiceAmount());
        map.put("#serCapital#", NumberUtil.ToFormat(Double.valueOf(idEntity.getServiceAmount())));
        //e.实际打款金额
        map.put("#remainLoan#", idEntity.getPayAmount());
        map.put("#remCapital#", NumberUtil.ToFormat(Double.valueOf(idEntity.getPayAmount())));
        //f.银行卡信息
        map.put("#name#", idEntity.getName());
        map.put("#bank#", idEntity.getBankName());
        map.put("#bank_num#", idEntity.getBankNum());
        /**************页面2********************/
        //a.回购金额
        map.put("#repurchase#", idEntity.getRansomAmount());
        map.put("#repCapital#", NumberUtil.ToFormat(Double.valueOf(idEntity.getRansomAmount())));
        List terms = repaymentPlanMapper.getRepaymentTermPlan(borrId.toString());
        if (terms != null && terms.size() > 0) {
            Map term=(Map)terms.get(0);
            String date = term.get("repayDate").toString().split("\\s+")[0];
        //b.租金
            map.put("#rent#", String.format("%.2f", term.get("rentalAmount")));
            //2-1.签订年
            String year2 = date.substring(0, 4);
            //2-2.签订月
            String month2 = date.substring(5, 7);
            //2-3.签订日
            String day2 = date.substring(8, date.length());
            map.put("#day2#", day2);
            map.put("#month2#", month2);
            map.put("#year2#", year2);
            Float payTotalValue = new BigDecimal(borrowList.getRansomAmount()).add(((BigDecimal) term.get("rentalAmount"))).floatValue();
            Float shouldTotalValue = new BigDecimal(payTotalValue).subtract(new BigDecimal(borrowList.getDepositAmount())).floatValue();
            String payTotal = String.format("%.2f", payTotalValue);
            String shouldTotal = String.format("%.2f", shouldTotalValue);
            map.put("#sum#", payTotal);
            //map.put("#payable#", shouldTotal);
        }

        /**************页面3********************/
        /**************页面4********************/
        map.put("#email#", idEntity.getEmail());
        map.put("#phone#", idEntity.getPhone());
        /**************页面5********************/
        return map;
    }


    /**
     * 选择产品金额和天数,获得产品所有费用
     *
     * @param idEntity 订单实体
     * @return
     */
    public NoteResult getProductCharge(IdEntity idEntity, String money) {
        //构建返回结果对象NoteResult
        NoteResult result = new NoteResult(CodeReturn.FAIL_CODE, "fail");
        try {
            /*Integer product_id;
            if(Detect.isPositive(productId)){
                //如果有值直接赋值
                product_id = productId;
            }else {
                //根据金额和天数查询产品ID
                product_id = productTermMapper.findProductId(money, day);
            }*/
            //根据产品ID查询产品期数表，获得产品金额，天数，利息(利率*金额*天数)
            //ProductTerm term = productTermMapper.selectById(productId);

            //产品信息
            Product product=productMapper.selectByPrimaryKey(idEntity.getProdId());
            if(null==product){
              return result;
            }
            //借款金额
            BigDecimal amount = new BigDecimal(money);
            //应还总额
            double total = amount.doubleValue();
            //利率 库里保存的是年利率
            BigDecimal rate = new BigDecimal("");
            BigDecimal monthRate = rate.divide(new BigDecimal("12")).setScale(2, BigDecimal.ROUND_DOWN);
            //计算总利息  等额本息每月还款计算公式：[贷款本金×月利率×(1+月利率)^还款月数]÷[(1+月利率)^还款月数-1]
            BigDecimal b1 = amount.multiply(monthRate).multiply(new BigDecimal(Math.pow(new BigDecimal("1").add(monthRate).doubleValue(), idEntity.getTermId())));
            BigDecimal b2 = new BigDecimal(Math.pow(new BigDecimal("1").add(monthRate).doubleValue(), idEntity.getTermId()) - 1);
            double mul = b1.divide(b2, 2, BigDecimal.ROUND_DOWN).doubleValue();
            //总金额加上利息
            total += mul;
            String interest = String.format("%.2f", mul);
            JSONObject data = new JSONObject();
            data.put("product_id", String.valueOf(product.getContractPrdouctId()));
            data.put("interest", interest);
            //信息管理费
            data.put("information_manage_fee", amount.multiply(new BigDecimal(idEntity.getInformationManageFee())).divide(new BigDecimal("100")));
            //账号管理费
            data.put("account_manage_fee", amount.multiply(new BigDecimal(idEntity.getAccountManageFee())).divide(new BigDecimal("100")));
            data.put("total", String.format("%.2f", total));
            if (!data.isEmpty()) {
                result.setCode(CodeReturn.SUCCESS_CODE);
                result.setInfo("success");
                result.setData(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new NoteResult(CodeReturn.FAIL_CODE, "fail");
        }
        return result;
    }

    /**
     * 创建电子合同
     *
     * @param borrId
     * @param status
     */
    private void creatContract(Integer borrId, Integer status, String msg) {
        if (Detect.isPositive(borrId)) {
            BorrowList bl = borrowListMapper.selectByPrimaryKey(borrId);
            if (bl != null) {
                Contract contract = new Contract();
                contract.setBorrNum(bl.getBorrNum());
                contract.setBorrId(borrId);
                contract.setCreateDate(Calendar.getInstance().getTime());
                contract.setStatus(status);
                contract.setResultJson(msg);
                contractMapper.insertContract(contract);
            }
        }
    }

    /**
     * 保存电子合同
     *
     * @param borrId
     * @param status
     */
    private void saveContract(Integer borrId, Integer status, String msg, String url, String imageUrl) throws UnsupportedEncodingException {
        if (Detect.isPositive(borrId) && Detect.isPositive(status)) {
            Contract contract = new Contract();
            contract.setBorrId(borrId);
            contract = contractMapper.selectOne(contract);

            if (contract != null) {
                contract.setStatus(status);
                if (Detect.notEmpty(msg)) {
                    contract.setResultJson(msg);
                }
                contract.setContractUrl(url);
                if (Detect.notEmpty(imageUrl)) {
                    contract.setImageUrl(baseUrl + URLEncoder.encode(imageUrl, "UTF-8"));
                }
                contractMapper.updateByPrimaryKeySelective(contract);
            }
        }
    }

    /**
     * 获取封装的数据
     * @param product
     * @return
     */
    private Map queryContractDataByProdId(Product product,Integer borrId){
         Map map=null;
        if(ContractDataCodeEnum.GET_DATA_FOUR_TERM_IOS.getCode().equals(product.getId().toString())
                ||ContractDataCodeEnum.GET_DATA_FOUR_TERM_ANDROID.getCode().equals(product.getId().toString())
                ){
            map= getContractDate(borrId);
        }
        else if(ContractDataCodeEnum.GET_DATA_ONE_TERM_IOS.getCode().equals(product.getId().toString())
                ||ContractDataCodeEnum.GET_DATA_ONE_TERM_ANDROID.getCode().equals(product.getId().toString())
                ){
            map= getContractDateByOneTerm(borrId);
        }
        return map;
    }

    /**
     * 获取产品id
     */
    private Product queryProductByBorrId(Integer borrId){
          BorrowList borrowList=borrowListMapper.selectByPrimaryKey(borrId);
          if(null==borrowList||!Detect.isPositive(borrowList.getProdId())){
              return null;
          }
          Product product=productMapper.selectByPrimaryKey(borrowList.getProdId());
          return product;
    }
    public static void main(String[] arge) throws IOException {
        String imageUrl = null;
        InputStream inputStream = null;
        try {
            inputStream = PdfToImage.pdfToImage("http://10.0.2.164/group1/M01/0C/EE/CgACplqg592AYSTpAArBCsAzHq8240.pdf", "png", null, true);
            imageUrl = DFSUtil.uploadContent("http://192.168.1.87:12015/loan-dfs/fdfs", inputStream, "png");
            System.out.println(imageUrl);
            int index;
            byte[] bytes = new byte[1024];
            FileOutputStream downloadFile = new FileOutputStream("C:\\Users\\wanzezhong\\Desktop\\aa.png");
            while ((index = inputStream.read(bytes)) != -1) {
                downloadFile.write(bytes, 0, index);
                downloadFile.flush();
            }
            downloadFile.close();
        } catch (IOException e) {
            log.info("合同PDF转为图片失败:{}", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            log.info("合同PDF转为图片失败:{}", e.getMessage());
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
//        System.out.println("http://cas.ronghezulin.com/loan-manage/proxy/image.action?path=" + URLEncoder.encode("http://10.0.2.166/group1/M01/0C/EE/CgACplqftzSAGSQxAAo2jR4QGPY707.png", "UTF-8"));
    }

    public String getContractUrl() {
        return contractUrl;
    }

    public void setContractUrl(String contractUrl) {
        this.contractUrl = contractUrl;
    }

    public String getGenerateUrl() {
        return generateUrl;
    }

    public void setGenerateUrl(String generateUrl) {
        this.generateUrl = generateUrl;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}
