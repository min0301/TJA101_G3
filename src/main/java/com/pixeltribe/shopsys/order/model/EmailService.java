package com.pixeltribe.shopsys.order.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import org.springframework.stereotype.Service;

import com.pixeltribe.shopsys.orderItem.model.OrderItemDTO;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailService {
	
	@Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.from}")
    private String fromEmail;
    
    @Value("${app.company.name:像素部落}")
    private String companyName;
    
    @Value("${app.company.support-email:support@pixeltribe.com}")
    private String supportEmail;
    
    
    // ========== 訂單相關郵件 ========== //
    // *** 發送付款成功郵件 (含現貨序號 + 預購說明) *** //
    public boolean sendPaymentSuccessEmail(
            OrderDTO order, 
            List<String> inStockSerials, 
            List<OrderItemDTO> preOrderItems) {
        
        try {
            log.info("開始發送付款成功郵件：orderNo={}", order.getOrderNo());
            
            String subject = "付款成功確認 - 訂單 #" + order.getOrderNo();
            String emailContent = buildPaymentSuccessEmailContent(order, inStockSerials, preOrderItems);
            
            boolean success = sendEmail(order.getContactEmail(), subject, emailContent);
            
            if (success) {
                log.info("付款成功郵件發送完成：orderNo={}", order.getOrderNo());
                recordEmailSent(order.getOrderNo(), "PAYMENT_SUCCESS", order.getContactEmail());
            }
            
            return success;
            
        } catch (Exception e) {
            log.error("發送付款成功郵件失敗：orderNo={}", order.getOrderNo(), e);
            recordEmailFailed(order.getOrderNo(), "PAYMENT_SUCCESS", order.getContactEmail(), e.getMessage());
            return false;
        }
    }
    
    
    // *** 發送預購產品到貨通知郵件 *** //
    public boolean sendPreOrderDeliveryEmail(
            OrderDTO order, 
            String productName, 
            String serialNumber) {
        
        try {
            log.info("開始發送預購到貨郵件：orderNo={}, product={}", order.getOrderNo(), productName);
            
            String subject = "預購產品到貨通知 - " + productName;
            String emailContent = buildPreOrderDeliveryEmailContent(order, productName, serialNumber);
            
            boolean success = sendEmail(order.getContactEmail(), subject, emailContent);
            
            if (success) {
                log.info("預購到貨郵件發送完成：orderNo={}", order.getOrderNo());
                recordEmailSent(order.getOrderNo(), "PREORDER_DELIVERY", order.getContactEmail());
            }
            
            return success;
            
        } catch (Exception e) {
            log.error("發送預購到貨郵件失敗：orderNo={}", order.getOrderNo(), e);
            recordEmailFailed(order.getOrderNo(), "PREORDER_DELIVERY", order.getContactEmail(), e.getMessage());
            return false;
        }
    }
    
    
    // *** 發送訂單取消通知郵件 *** //
    public boolean sendOrderCancelEmail(OrderDTO order, String reason) {
        try {
            log.info("開始發送訂單取消郵件：orderNo={}", order.getOrderNo());
            
            String subject = "訂單取消通知 - 訂單 #" + order.getOrderNo();
            String emailContent = buildOrderCancelEmailContent(order, reason);
            
            boolean success = sendEmail(order.getContactEmail(), subject, emailContent);
            
            if (success) {
                log.info("訂單取消郵件發送完成：orderNo={}", order.getOrderNo());
                recordEmailSent(order.getOrderNo(), "ORDER_CANCEL", order.getContactEmail());
            }
            
            return success;
            
        } catch (Exception e) {
            log.error("發送訂單取消郵件失敗：orderNo={}", order.getOrderNo(), e);
            recordEmailFailed(order.getOrderNo(), "ORDER_CANCEL", order.getContactEmail(), e.getMessage());
            return false;
        }
    }
    
    
    // *** 發送付款失敗通知郵件 *** //
    public boolean sendPaymentFailedEmail(OrderDTO order, String reason) {
        try {
            log.info("開始發送付款失敗郵件：orderNo={}", order.getOrderNo());
            
            String subject = "付款失敗通知 - 訂單 #" + order.getOrderNo();
            String emailContent = buildPaymentFailedEmailContent(order, reason);
            
            boolean success = sendEmail(order.getContactEmail(), subject, emailContent);
            
            if (success) {
                log.info("付款失敗郵件發送完成：orderNo={}", order.getOrderNo());
                recordEmailSent(order.getOrderNo(), "PAYMENT_FAILED", order.getContactEmail());
            }
            
            return success;
            
        } catch (Exception e) {
            log.error("發送付款失敗郵件失敗：orderNo={}", order.getOrderNo(), e);
            recordEmailFailed(order.getOrderNo(), "PAYMENT_FAILED", order.getContactEmail(), e.getMessage());
            return false;
        }
    }
    
    
    // ========== 核心郵件發送方法 ========== //
    // *** 核心郵件發送方法 ***//
    private boolean sendEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            
            mailSender.send(message);
            
            log.debug("郵件發送成功：to={}, subject={}", to, subject);
            return true;
            
        } catch (Exception e) {
            log.error("郵件發送失敗：to={}, subject={}", to, subject, e);
            return false;
        }
    }
    
    
    // ========== 郵件內容建構方法 ========== //
    
    // *****建立付款成功郵件內容***** //
    private String buildPaymentSuccessEmailContent(OrderDTO order, List<String> inStockSerials, List<OrderItemDTO> preOrderItems) {
        StringBuilder content = new StringBuilder();
        
        content.append(getEmailHeader("付款成功確認"));
        content.append("<p>親愛的客戶您好，您的訂單已付款成功！</p>");
        
        // 訂單基本資訊
        content.append("<h3>訂單明細</h3>");
        content.append("<table border='1' style='border-collapse: collapse; width: 100%; margin: 10px 0;'>");
        content.append("<tr><td style='padding: 8px;'><strong>訂單編號</strong></td><td style='padding: 8px;'>#").append(order.getOrderNo()).append("</td></tr>");
        content.append("<tr><td style='padding: 8px;'><strong>付款時間</strong></td><td style='padding: 8px;'>").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</td></tr>");
        content.append("<tr><td style='padding: 8px;'><strong>總金額</strong></td><td style='padding: 8px;'>NT$ ").append(order.getOrderTotal()).append("</td></tr>");
        content.append("</table>");
        
        // 商品清單
        content.append("<h3>產品清單</h3>");
        content.append("<table border='1' style='border-collapse: collapse; width: 100%; margin: 10px 0;'>");
        content.append("<tr style='background-color: #f5f5f5;'><th style='padding: 8px;'>產品名稱</th><th style='padding: 8px;'>價格</th><th style='padding: 8px;'>狀態</th><th style='padding: 8px;'>序號</th></tr>");
        
        int serialIndex = 0;
        for (OrderItemDTO item : order.getOrderItems()) {
            content.append("<tr>");
            content.append("<td style='padding: 8px;'>").append(item.getProductName()).append("</td>");
            content.append("<td style='padding: 8px;'>NT$ ").append(item.getProPrice()).append("</td>");
            
            // 檢查是否為現貨並有序號
            if (serialIndex < inStockSerials.size()) {
                content.append("<td style='padding: 8px; color: green;'><strong>現貨</strong></td>");
                content.append("<td style='padding: 8px; background-color: #e8f5e8; font-family: monospace; font-weight: bold;'>").append(inStockSerials.get(serialIndex++)).append("</td>");
            } else {
                content.append("<td style='padding: 8px; color: orange;'><strong>預購中</strong></td>");
                content.append("<td style='padding: 8px; color: #666;'>到貨後發送</td>");
            }
            content.append("</tr>");
        }
        
        content.append("</table>");
        
        // 預購說明
        if (!preOrderItems.isEmpty()) {
            content.append("<div style='background-color: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; margin: 15px 0; border-radius: 5px;'>");
            content.append("<h3 style='color: #856404; margin-top: 0;'> 預購產品說明</h3>");
            content.append("<p>您有 <strong>").append(preOrderItems.size()).append("</strong> 項預購產品</p>");
            content.append("<p>預購產品將於到貨後立即為您發放序號，屆時會再發送郵件通知</p>");
            content.append("<p>我們會在產品上架後的 <strong>1個工作天內</strong> 自動發送序號給您</p>");
            content.append("</div>");
        }
        
        content.append(getEmailFooter());
        
        return content.toString();
    }
    
    
    // *****建立預購到貨郵件內容***** //
    private String buildPreOrderDeliveryEmailContent(OrderDTO order, String productName, String serialNumber) {
        StringBuilder content = new StringBuilder();
        
        content.append(getEmailHeader("預購產品到貨通知"));
        content.append("<p>親愛的客戶您好，您預購的產品已到貨！</p>");
        
        content.append("<h3> 產品資訊</h3>");
        content.append("<table border='1' style='border-collapse: collapse; width: 100%; margin: 10px 0;'>");
        content.append("<tr><td style='padding: 8px;'><strong>訂單編號</strong></td><td style='padding: 8px;'>#").append(order.getOrderNo()).append("</td></tr>");
        content.append("<tr><td style='padding: 8px;'><strong>產品名稱</strong></td><td style='padding: 8px;'>").append(productName).append("</td></tr>");
        content.append("<tr><td style='padding: 8px;'><strong>到貨時間</strong></td><td style='padding: 8px;'>").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</td></tr>");
        content.append("</table>");
        
        content.append("<h3> 遊戲序號</h3>");
        content.append("<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 20px; margin: 15px 0; border-radius: 10px; text-align: center; color: white; box-shadow: 0 4px 6px rgba(0,0,0,0.1);'>");
        content.append("<p style='margin: 0; font-size: 14px; opacity: 0.9;'>請複製以下序號到遊戲中啟用</p>");
        content.append("<div style='font-size: 24px; font-weight: bold; margin: 15px 0; font-family: monospace; background: rgba(255,255,255,0.2); padding: 10px; border-radius: 5px;'>");
        content.append(serialNumber);
        content.append("</div>");
        content.append("<p style='margin: 0; font-size: 12px; opacity: 0.8;'>點擊選中後可直接複製</p>");
        content.append("</div>");
        
        content.append("<h3> 使用說明</h3>");
        content.append("<ul>");
        content.append("<li>請儘快使用序號啟用遊戲</li>");
        content.append("<li>序號僅能使用一次，請妥善保存</li>");
        content.append("<li>如有使用問題請聯繫客服：").append(supportEmail).append("</li>");
        content.append("</ul>");
        
        content.append(getEmailFooter());
        
        return content.toString();
    }
    
    
    // *****建立訂單取消郵件內容***** //
    private String buildOrderCancelEmailContent(OrderDTO order, String reason) {
        StringBuilder content = new StringBuilder();
        
        content.append(getEmailHeader("訂單取消通知"));
        content.append("<p>親愛的客戶您好，您的訂單已被取消。</p>");
        
        content.append("<h3>取消詳情</h3>");
        content.append("<table border='1' style='border-collapse: collapse; width: 100%; margin: 10px 0;'>");
        content.append("<tr><td style='padding: 8px;'><strong>訂單編號</strong></td><td style='padding: 8px;'>#").append(order.getOrderNo()).append("</td></tr>");
        content.append("<tr><td style='padding: 8px;'><strong>取消原因</strong></td><td style='padding: 8px;'>").append(reason).append("</td></tr>");
        content.append("<tr><td style='padding: 8px;'><strong>取消時間</strong></td><td style='padding: 8px;'>").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("</td></tr>");
        content.append("</table>");
        
        content.append("<p>如有任何疑問，請聯繫我們的客服團隊。</p>");
        content.append(getEmailFooter());
        
        return content.toString();
    }
    
    // *****建立付款失敗郵件內容***** //
    private String buildPaymentFailedEmailContent(OrderDTO order, String reason) {
        StringBuilder content = new StringBuilder();
        
        content.append(getEmailHeader("付款失敗通知"));
        content.append("<p>親愛的客戶您好，您的訂單付款未能成功完成。</p>");
        
        content.append("<h3>失敗詳情</h3>");
        content.append("<table border='1' style='border-collapse: collapse; width: 100%; margin: 10px 0;'>");
        content.append("<tr><td style='padding: 8px;'><strong>訂單編號</strong></td><td style='padding: 8px;'>#").append(order.getOrderNo()).append("</td></tr>");
        content.append("<tr><td style='padding: 8px;'><strong>失敗原因</strong></td><td style='padding: 8px;'>").append(reason).append("</td></tr>");
        content.append("<tr><td style='padding: 8px;'><strong>訂單金額</strong></td><td style='padding: 8px;'>NT$ ").append(order.getOrderTotal()).append("</td></tr>");
        content.append("</table>");
        
        content.append("<p>您可以重新嘗試付款，或聯繫客服協助處理。</p>");
        content.append("<p><a href='#' style='background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>重新付款</a></p>");
        
        content.append(getEmailFooter());
        
        return content.toString();
    }
    
    // ========== 郵件模板方法 ========== //
    // *****取得郵件標頭***** //
    private String getEmailHeader(String title) {
        StringBuilder header = new StringBuilder();
        header.append("<!DOCTYPE html>");
        header.append("<html><head><meta charset='UTF-8'>");
        header.append("<title>").append(title).append("</title>");
        header.append("<style>");
        header.append("body { font-family: 'Microsoft JhengHei', Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 20px; background-color: #f5f5f5; }");
        header.append(".container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); overflow: hidden; }");
        header.append(".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; text-align: center; }");
        header.append(".content { padding: 20px; }");
        header.append("table { border-collapse: collapse; width: 100%; margin: 10px 0; }");
        header.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        header.append("th { background-color: #f5f5f5; }");
        header.append("</style>");
        header.append("</head><body>");
        header.append("<div class='container'>");
        header.append("<div class='header'><h1 style='margin: 0;'>").append(title).append("</h1></div>");
        header.append("<div class='content'>");
        
        return header.toString();
    }
    
    
    // *****取得郵件頁尾***** //
    private String getEmailFooter() {
        StringBuilder footer = new StringBuilder();
        footer.append("<hr style='margin: 20px 0; border: none; border-top: 1px solid #eee;'>");
        footer.append("<div style='text-align: center; color: #666; font-size: 14px;'>");
        footer.append("<p>感謝您的支持！<br><strong>").append(companyName).append("</strong></p>");
        footer.append("<p style='font-size: 12px;'>");
        footer.append("如有疑問請聯繫客服：").append(supportEmail).append("<br>");
        footer.append("此郵件為系統自動發送，請勿直接回覆");
        footer.append("</p>");
        footer.append("</div>");
        footer.append("</div></div></body></html>");
        
        return footer.toString();
    }
    
    
    // ========== 郵件記錄方法 ========== //
    // *****記錄郵件發送成功***** //
    private void recordEmailSent(Integer orderNo, String emailType, String recipient) {
        log.info("EMAIL_SENT|orderNo={}|type={}|to={}|timestamp={}", 
                orderNo, emailType, recipient, System.currentTimeMillis());
    }
    
    // *****記錄郵件發送失敗***** //
    private void recordEmailFailed(Integer orderNo, String emailType, String recipient, String error) {
        log.error("EMAIL_FAILED|orderNo={}|type={}|to={}|error={}|timestamp={}", 
                orderNo, emailType, recipient, error, System.currentTimeMillis());
    }
    
}