package com.interswitch.middleware.integrations.bills;

import com.interswitch.middleware.integrations.bills.params.Bill;
import com.interswitch.middleware.integrations.bills.params.PaidBillResponse;
import com.interswitch.middleware.integrations.bills.params.ValidateBill;
import com.interswitch.middleware.integrations.bills.params.ValidateBillResponse;
import com.interswitch.middleware.params.ApiResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MockBillsOperation implements BillsOperation {
    @Override
    public ApiResponse<List<Bill>> getBills() {
        List<Bill> bills = new ArrayList<>();
        Bill bill1 = new Bill();
        bill1.setCode("BILL001");
        bill1.setName("Electricity");
        bill1.setAmount(BigDecimal.valueOf(5000.0));
        bills.add(bill1);

        Bill bill2 = new Bill();
        bill2.setCode("BILL002");
        bill2.setName("Water");
        bill2.setAmount(BigDecimal.valueOf(5000.0));
        bills.add(bill2);

        return ApiResponse.Success(bills);
    }

    @Override
    public ApiResponse<ValidateBillResponse> validateBill(ValidateBill billReq) {
        ValidateBillResponse resp = new ValidateBillResponse();
        resp.setBillCode(billReq.getCode());
        resp.setAmount(BigDecimal.valueOf(5000.0));
        resp.setBeneficiary(billReq.getBeneficiary());
        resp.setBillName("BillTest");
        return ApiResponse.Success(resp);
    }

    @Override
    public ApiResponse<PaidBillResponse> payBills(ValidateBill billReq) {
        PaidBillResponse resp = new PaidBillResponse();
        resp.setBillCode(billReq.getCode());
        resp.setAmount(BigDecimal.valueOf(5000.0));
        resp.setBeneficiary(billReq.getBeneficiary());
        resp.setBillName("BillTest");
        resp.setTransactionRef("BILL" + UUID.randomUUID().toString());
        return ApiResponse.Success(resp);
    }
}
