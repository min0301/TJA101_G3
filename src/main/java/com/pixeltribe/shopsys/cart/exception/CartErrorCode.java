package com.pixeltribe.shopsys.cart.exception;


//========== 前後台合併 ==========//
public enum CartErrorCode {
	
	//前台錯誤
	CART_001("CART_001", "購物車小精靈正在努力中，請稍後再試"),
	CART_002("CART_002", "商品資料有問題，購物車小精靈確認中"),
	CART_003("CART_003", "部落沒貨了，補貨小精靈努力中"),
	CART_004("CART_004", "部落存貨沒那麼多~買少一點"),
	CART_005("CART_005", "數量更新失敗，購物車小精靈需要休息一下"),
	CART_006("CART_006", "小精靈找不到這個商品，可能已經下架囉"),
	CART_008("CART_008", "移除商品時出了點小狀況，請稍新再試"),
	CART_009("CART_009", "會員資料有問題，請重新登入"),
	CART_010("CART_010", "清空購物車時遇到問題，購物車小精靈正在處理"),
	CART_011("CART_011", "購物車商品種類已達上限(50種)，無法再新增商品"),
	CART_012("CART_012", "商品數量必須大於0，小精靈不懂你要什麼"),
	CART_013("CART_013", "單項商品數量不能超過99個，小精靈搬不動這麼多"),
	CART_014("CART_014", "購物車總金額超過20萬限制，小精靈的錢包要破洞了"),
	CART_015("CART_015", "批量操作參數錯誤，小精靈不知道要處理什麼"),
	
	
	//後台錯誤
	ADM_001("ADM_001", "權限不足"),
	ADM_003("ADM_003", "統計資料查詢失敗");
	
	private final String code;
	private final String message;
	
	CartErrorCode(String code, String message) {
		this.code =code;
		this.message = message;
	}
	
	public String getCode() {return code;}
	public String getMessage() {return message;}
}