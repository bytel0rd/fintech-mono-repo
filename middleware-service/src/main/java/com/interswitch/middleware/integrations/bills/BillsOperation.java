package com.interswitch.middleware.integrations.bills;

import com.interswitch.middleware.integrations.bills.params.Bill;
import com.interswitch.middleware.integrations.bills.params.PaidBillResponse;
import com.interswitch.middleware.integrations.bills.params.ValidateBill;
import com.interswitch.middleware.integrations.bills.params.ValidateBillResponse;
import com.interswitch.middleware.params.AccountDetailResponse;
import com.interswitch.middleware.params.ApiResponse;

import java.util.List;

public interface BillsOperation {
    public ApiResponse<List<Bill>> getBills();

    public ApiResponse<ValidateBillResponse> validateBill(ValidateBill billReq);

    public ApiResponse<PaidBillResponse> payBills(ValidateBill billReq);
}
