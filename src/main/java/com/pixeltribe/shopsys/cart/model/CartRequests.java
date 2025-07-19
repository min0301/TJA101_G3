package com.pixeltribe.shopsys.cart.model;

import java.util.List;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// *** 購物車相關的所有請求 DTO *** //

public class CartRequests {

    // ============ 加入商品到購物車 Request ============ //
    @Getter
    @Setter
    public static class AddToCart {
        @NotNull
        private Integer proNo;  // 前端從商品頁面傳入要加入的商品編號
        
        @NotNull(message = "商品數量不能為空")
        @Min(value = 1, message = "加入購物車的數量至少為1")
        private Integer proNum;
        
        public AddToCart() {}
        
        public AddToCart(Integer proNo, Integer proNum) {
            this.proNo = proNo;
            this.proNum = proNum;
        }
    }

    // ============ 更新商品數量 Request ============ //
    @Getter
    @Setter
    public static class UpdateQuantity {
        @NotNull
        private Integer proNo;  // 前端從購物車頁面傳入要更新的商品編號
        
        @NotNull(message = "商品數量不能為空")
        @Min(value = 0)  // 允許0，代表移除
        private Integer proNum;
        
        public UpdateQuantity() {}
        
        public UpdateQuantity(Integer proNo, Integer proNum) {
            this.proNo = proNo;
            this.proNum = proNum;
        }
    }

    // ============ 批量移除商品 Request ============ //
    @Getter
    @Setter
    public static class BatchRemove {
        @NotNull
        @Size(min = 1, message = "至少需要選擇一個商品")
        private List<Integer> proNos;  // 前端從購物車頁面選中要刪除的商品編號
        
        public BatchRemove() {}
        
        public BatchRemove(List<Integer> proNos) {
            this.proNos = proNos;
        }
    }

    // ============ 批量更新數量 Request ============ //
    @Getter
    @Setter
    public static class BatchUpdate {
        @NotNull(message = "更新項目列表不能為空")
        @Size(min = 1, message = "至少需要更新一個商品")
        private List<CartUpdateItem> updateItems;
        
        public BatchUpdate() {}
        
        public BatchUpdate(List<CartUpdateItem> updateItems) {
            this.updateItems = updateItems;
        }
        
        // 內部類別：單個更新項目
        @Getter
        @Setter
        public static class CartUpdateItem {
            @NotNull
            private Integer proNo;  // 前端從購物車頁面傳入要更新的商品編號
            
            @NotNull(message = "商品數量不能為空")
            @Min(value = 0, message = "商品數量不能為負數")  // 允許0，代表移除
            private Integer quantity;
            
            public CartUpdateItem() {}
            
            public CartUpdateItem(Integer proNo, Integer quantity) {
                this.proNo = proNo;
                this.quantity = quantity;
            }
        }
    }

    // ============ 選中商品 Request ============ //
    @Getter
    @Setter
    public static class SelectedItems {
        @NotNull
        @Size(min = 1, message = "至少需要選擇一個商品")
        private List<Integer> selectedProNos;  // 前端從購物車頁面選中的商品編號
        
        public SelectedItems() {}
        
        public SelectedItems(List<Integer> selectedProNos) {
            this.selectedProNos = selectedProNos;
        }
    }

    // ============ 結帳驗證 Request ============ //
    @Getter
    @Setter
    public static class CheckoutValidation {
        private List<Integer> selectedProNos; // 可選，前端選中要結帳的商品編號
        
        public CheckoutValidation() {}
        
        public CheckoutValidation(List<Integer> selectedProNos) {
            this.selectedProNos = selectedProNos;
        }
    }
}