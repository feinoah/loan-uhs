package com.jhh.jhs.loan.app.app.capital;


import com.jhh.jhs.loan.api.entity.ResponseDo;
import com.jhh.jhs.loan.app.app.BaseController;
import com.jhh.jhs.loan.entity.app.NoteResult;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Slf4j
public abstract class MyBaseController<T> {

    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    protected static ResponseEntity<NoteResult> buildErrorResp(JhsNotifyException notifyException) {
        NoteResult responseDo = new NoteResult();
        responseDo.setCode(notifyException.getCode());
        responseDo.setInfo(notifyException.getDesc());
        return new ResponseEntity<NoteResult>(responseDo, HttpStatus.OK);
    }

    protected static ResponseEntity<NoteResult> buildSuccessResp(HttpHeaders httpHeaders, Object object) {
        NoteResult respBody = new NoteResult();
        respBody.setCode(ResultStatusEnum.SUCCESS.getCode());
        respBody.setInfo(ResultStatusEnum.SUCCESS.getMessage());
        respBody.setData(object);
        return new ResponseEntity<NoteResult>(respBody, httpHeaders,HttpStatus.OK);
    }

    protected static ResponseEntity<ResponseDo> buildResp(int code,String info
    ) {
        ResponseDo respBody = new ResponseDo();
        respBody.setCode(code);
        respBody.setInfo(info);

        return new ResponseEntity<ResponseDo>(respBody, null,HttpStatus.OK);
    }

    protected static ResponseEntity<ResponseDo> buildResp(int code,String info,Object obj
    ) {
        ResponseDo<Object> respBody = new ResponseDo<>();
        respBody.setCode(code);
        respBody.setInfo(info);
        respBody.setData(obj);
        return new ResponseEntity<>(respBody, null,HttpStatus.OK);
    }
//    public static void doSignCheck(String sign,Object object) {
//        String genSignContent = generateSignContent(object);
//        if(!Md5Util.verify(genSignContent, sign, "key", "UTF-8")) {
//            throw new JhsNotifyException(ResultStatusEnum.CHECK_SIGN_ERROR.getCode(), ResultStatusEnum.CHECK_SIGN_ERROR.getMessage());
//        }
//    }

//    public static String generateSignContent(Object object) {
//        StringBuffer signContent = new StringBuffer();
//        HashMap<String, String> map = new HashMap<String, String>();
//        try {
//            Field[] fs = object.getClass().getDeclaredFields();
//            for (Field f : fs) {
//                if (f.getName().equals("serialVersionUID")) {
//                    continue;
//                }
//                f.setAccessible(true);
//                Object val=f.get(object);
//                if (val == null) {
//                    continue;
//                }
//                String value = f.get(object).toString();
//                String name = f.getName().toLowerCase();
//                map.put(name, value);
//            }
//            map.remove("sign");
//            List<Map.Entry<String, String>> mappingList = new ArrayList<Map.Entry<String, String>>(map.entrySet());
//            Collections.sort(mappingList, new Comparator<Map.Entry<String, String>>() {
//                public int compare(Map.Entry<String, String> mapping1, Map.Entry<String, String> mapping2) {
//                    return mapping1.getKey().compareTo(mapping2.getKey());
//                }
//            });
//            Map.Entry<String, String> mapping = (Map.Entry<String, String>) mappingList.get(0);
//            signContent.append(mapping.getKey().toLowerCase() + "=" + mapping.getValue());
//            for (int i = 1; i < mappingList.size(); i++) {
//                Map.Entry<String, String> maps = (Map.Entry<String, String>) mappingList.get(i);
//                signContent.append("&" + maps.getKey().toLowerCase() + "=" + maps.getValue());
//            }
//
//
//        } catch (Exception e) {
//            log.error("生成签名字符串异常{}",e);
//            throw new JhsNotifyException(ResultStatusEnum.SIGN_ERROR.getCode(), ResultStatusEnum.SIGN_ERROR.getCode());
//        } finally {
////            if(object.getClass().getSimpleName().equals("OperatorAuthDTOSIGN")){
////                logger.info("验签的原始数据=\t"+signContent.toString());
////            }
//            return signContent.toString();
//        }
//    }

    protected abstract ResponseEntity<T> doBusiness(Object object) throws Exception;


}
