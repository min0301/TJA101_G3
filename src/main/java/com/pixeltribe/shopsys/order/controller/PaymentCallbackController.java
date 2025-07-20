package com.pixeltribe.shopsys.order.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.pixeltribe.shopsys.order.model.PaymentService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class PaymentCallbackController {
    
    @Autowired
    private PaymentService paymentService;
    
    // ***** 綠界付款通知回調 ***** //
    @PostMapping("/payment/notify")
    @ResponseBody
    public String handlePaymentNotify(@RequestParam Map<String, String> params) {
        try {
            log.info("收到綠界付款通知：{}", params);
            String result = paymentService.handlePaymentCallback(params);
            return result;
        } catch (Exception e) {
            log.error("處理付款通知失敗", e);
            return "0|系統錯誤";
        }
    }
    
    // ***** 綠界付款返回頁面 ***** //
    @GetMapping("/payment/return")
    @ResponseBody  
    public String handlePaymentReturn(@RequestParam Map<String, String> params) {
        try {
            log.info("用戶付款返回：{}", params);
            return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"><title>付款完成</title></head>
                <body>
                    <h2>付款成功！</h2>
                    <script>
                        setTimeout(() => window.location.href = '/front-end/shopsys/order/order-list.html', 2000);
                    </script>
                </body>
                </html>
                """;
        } catch (Exception e) {
            log.error("處理付款返回失敗", e);
            return "系統錯誤";
        }
    }
}