package com.jhh.jhs.loan.manage.service.impl;

import com.jhh.jhs.loan.api.loan.CollectorsListService;
import com.jhh.jhs.loan.common.util.Assertion;
import com.jhh.jhs.loan.common.util.Detect;
import com.jhh.jhs.loan.entity.app.BorrowList;
import com.jhh.jhs.loan.entity.common.Constants;
import com.jhh.jhs.loan.entity.common.ResponseCode;
import com.jhh.jhs.loan.entity.enums.BorrowStatusEnum;
import com.jhh.jhs.loan.entity.loan.Collectors;
import com.jhh.jhs.loan.entity.loan.CollectorsList;
import com.jhh.jhs.loan.entity.manager.CollectorsCompanyVo;
import com.jhh.jhs.loan.entity.manager.CollectorsRecord;
import com.jhh.jhs.loan.entity.manager.LoanCompanyBorrow;
import com.jhh.jhs.loan.manage.entity.Response;
import com.jhh.jhs.loan.manage.mapper.*;
import com.jhh.jhs.loan.manage.service.borr.BorrListService;
import com.jhh.jhs.loan.manage.service.robot.RobotService;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

/**
 * 合同相关
 */
@Service @Setter
public class BorrListServiceImpl implements BorrListService {
    private static final Logger logger = LoggerFactory.getLogger(BorrListServiceImpl.class);

    @Autowired
    private BorrowListMapper borrowListMapper;
    @Autowired
    private CollectorsMapper collectorsMapper;
    @Autowired
    private CollectorsListMapper collectorsListMapper;
    @Autowired
    private CollectorsRecordMapper collectorsRecordMapper;
    @Autowired
    private LoanCompanyBorrowMapper companyBorrowMapper;
    @Autowired
    private CollectorsListService collectorsListService;
    @Autowired
    private RobotService robotService;
    @Override
    public Response getBorrByPerId(Integer perId) {
        Assertion.isPositive(perId, "合同不能为空");
        Response response = new Response().code(ResponseCode.FIAL);

        List borrs = borrowListMapper.getBorrByPerId(perId);

        if(borrs != null){
            response.data(borrs).msg("success").code(ResponseCode.SUCCESS);
        }

        return response;
    }

    @Override
    public Response cancelBorrList(Integer id) {
        Assertion.isPositive(id,"合同id不能为空");
        Response response = new Response().code(ResponseCode.FIAL).msg("未找到要取消的借款记录!");
        BorrowList borrowList =  borrowListMapper.selectByPrimaryKey(id);
        Assertion.notNull(borrowList,"合同不存在");
        if(borrowList.getBorrStatus().equals(BorrowStatusEnum.LOAN_FAIL.getCode()) ||
                borrowList.getBorrStatus().equals(BorrowStatusEnum.SIGNED.getCode())  ){
            //更新合同状态
            borrowList.setBorrStatus(BorrowStatusEnum.CANCEL.getCode());
            borrowList.setUpdateDate(new Date());
            borrowListMapper.updateByPrimaryKeySelective(borrowList);
            response.code(ResponseCode.SUCCESS).msg("借款已取消!");
        }
        return response;
    }

    @Override @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public Response saveTransferBorrList(List<String> borrIds, String userId, String opUserId) {
        Assertion.notEmpty(borrIds,"转件合同不能为空");
        Assertion.notEmpty(userId,"被转件人不能为空");
        Assertion.notEmpty(opUserId,"转件人不能为空");
        Response response = new Response().code(ResponseCode.FIAL).msg("转件失败");
        Collectors c = new Collectors();
        c.setUserSysno(userId);
        //查询催收人
        Collectors collectors = collectorsMapper.selectOne(c);

        if(collectors != null){
            //转件记录
            List<CollectorsRecord> collectorsRecords = new LinkedList();
            //更新公司催收记录
            List<LoanCompanyBorrow> updateCompanyBorrow = new LinkedList();
            //插入公司催收记录
            List<LoanCompanyBorrow> insertCompanyBorrow = new LinkedList();

            Iterator<String> iter = borrIds.iterator();
            //批量操作减少内存使用
            StringBuffer borrId = new StringBuffer();
            while (iter.hasNext()) {
                borrId.append(iter.next());
                //查询催收单历史记录
                Map<String,Object> queryMap = new HashMap<>(16);
                queryMap.put("contractSysno", borrId.toString());
                CollectorsCompanyVo companyVo = collectorsListMapper.selectCollectorsCompanyVo(queryMap);
                if(companyVo == null ){
                    //未找到，无需更新直接下次操作
                    iter.remove();
                    borrId.delete(0, borrId.length());
                    continue;
                }

                //记录转件记录
                CollectorsRecord collectorsRecord = new CollectorsRecord();
                collectorsRecord.setBedueUser(companyVo.getBedueUserSysno());
                collectorsRecord.setContractId(borrId + "");
                collectorsRecord.setCreateUser(opUserId);
                collectorsRecord.setCreateTime(Calendar.getInstance().getTime());

                collectorsRecords.add(collectorsRecord);

                //转给外包公司
                if(Constants.COLLECTORS_OUT.equals(collectors.getLevelType())){
                    if(companyVo != null && companyVo.getCompanyId() != null){
                        //外包公司更新
                        LoanCompanyBorrow update = new LoanCompanyBorrow();
                        update.setCompanyId(collectors.getUserGroupId());
                        update.setBorrId(Integer.valueOf(borrId.toString()));
                        update.setUpdateUser(opUserId);
                        update.setUpdateDate(Calendar.getInstance().getTime());
                        updateCompanyBorrow.add(update);

                    }else{
                        //外包公司插入
                        LoanCompanyBorrow insert = new LoanCompanyBorrow();
                        insert.setCompanyId(collectors.getUserGroupId());
                        insert.setBorrId(Integer.valueOf(borrId.toString()));
                        insert.setCreateUser(opUserId);
                        insert.setCreateDate(Calendar.getInstance().getTime());
                        insertCompanyBorrow.add(insert);
                    }
                }
                borrId.delete(0, borrId.length());
            }
            //分单更新
            int successCount = collectorsListService.batchUpdate(borrIds,collectors);

            if(Detect.isPositive(successCount)){
                if(Detect.notEmpty(collectorsRecords)){
                    //插入转件历史表
                    collectorsRecordMapper.batchInsertCollectorsRecord(collectorsRecords);
                }

                if(Detect.notEmpty(insertCompanyBorrow)){
                    //插入外包公司转件表
                    companyBorrowMapper.batchInsert(insertCompanyBorrow);
                }

                if(Detect.notEmpty(updateCompanyBorrow) ){
                    //更新外包公司转件表
                    companyBorrowMapper.batchUpdate(updateCompanyBorrow);
                }
                response.code(ResponseCode.SUCCESS).msg("转件成功！条数:" + successCount);
            }

        }
        return response;
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub

       List list = new ArrayList();
       list.add("1");
       list.add("2");
       list.add("3");
        Iterator<String>  it = list.iterator();
        StringBuffer sb = new StringBuffer();
       while (it.hasNext()){
           sb.append(it.next());
           System.out.println(sb);
           sb.delete(0,sb.length());
       }

    }


    @Override
    public Integer submenuTransfer() {
        //查询为分单订单
        List<CollectorsList> collectorsList = borrowListMapper.getCollectorsByOverdue();
        if(Detect.notEmpty(collectorsList)){
            //分配给特殊
            collectorsListMapper.batchInsertCollectorsList(collectorsList);

            Set set = new HashSet();
            for (CollectorsList collectors :collectorsList){
                set.add(collectors.getContractSysno());
            }
            //更新合同冗余字段
            BorrowList borrowList = new BorrowList();
            borrowList.setCollectionUser("9999");
            Example blExample = new Example(CollectorsList.class);
            blExample.createCriteria().andIn("id", set);
            return borrowListMapper.updateByExampleSelective(borrowList, blExample);
        }
        return 0;
    }

    @Override
    public void rejectAudit() {
        //自动拒绝人工审核订单
        borrowListMapper.rejectAudit();
    }

    @Override
    public void rcCallPhone() {
        List<BorrowList> borrowLists = borrowListMapper.selectUnBaikelu();
        if(borrowLists != null){
            for(BorrowList borrowList : borrowLists){
                try {
                    robotService.sendRcOrder(borrowList.getId());
                } catch (Exception e) {
                    logger.error("百可录打电话失败ID：" + borrowList.getId() );
                    e.printStackTrace();
                }
            }
        }
    }
}
