package com.jhh.jhs.loan.app.app;

import com.jhh.jhs.loan.api.entity.ResponseDo;
import com.jhh.jhs.loan.api.product.ProductService;
import com.jhh.jhs.loan.app.common.constant.Admin;
import io.github.yedaxia.apidocs.ApiDoc;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 产品模块
 * @author xuepengfei
 */
@RestController
@RequestMapping("/product")
public class ProductController extends BaseController{

    @Autowired
    private ProductService productService;

    /**
     *  根据设备查询产品信息
     *  device 设备名称
     * @return
     */
    @RequestMapping("/getProduct")
    @ApiDoc(Admin.class)
    public ResponseDo<Map<String,Object>> getProduct(@RequestParam String device, HttpServletRequest request){
        String productId = request.getParameter("productId");
        if (StringUtils.isEmpty(productId)) {
            return productService.getProduct(device);
        }
        return productService.getProduct(device, productId);
    }
}
