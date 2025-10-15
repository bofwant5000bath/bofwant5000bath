describe("Add Expense Scenarios", () => {
  beforeEach(() => {
    cy.intercept("GET", "**/api/bills/group/1", { fixture: "group1_bills.json" }).as("getGroupBills");
    cy.visit("/#/dashboard", {
      onBeforeLoad(win) {
        win.localStorage.setItem("user_id", "1");
        win.localStorage.setItem("full_name", "เกียรติกุล เข้มแข็ง");
      },
    });
    cy.contains("ทริปเที่ยวทะเล").click();
    cy.wait("@getGroupBills");
    cy.contains("เพิ่มค่าใช้จ่าย").click();
    cy.url().should("include", "/#/addexpense/1");
  });

  it('should successfully add an expense with "Custom Split"', () => {
    cy.intercept("POST", "**/api/bills/create", { statusCode: 201 }).as("createBill");

    cy.contains('label', 'จำนวนเงิน').parent().find('input').clear().type("1000");
    cy.contains('label', 'คำอธิบาย').parent().find('input').type("ค่าบุฟเฟ่ต์");
    cy.contains("label", "กำหนดจำนวนเงินเอง").click();

    cy.contains('span', 'เกียรติกุล เข้มแข็ง')
      .parent().parent().find('input[type="number"]').clear().type("500.25");

    cy.contains('span', 'พรพวีร์ พัฒนพรวิวัฒน์')
      .parent().parent().find('input[type="number"]').clear().type("300.50");

    cy.contains('เหลือที่ต้องแบ่ง:').next('p').invoke('text').then((remainingText) => {
      const remainingValue = remainingText.replace('฿', '').replace(',', '').trim();
      cy.contains('span', 'สุฑาณทิพย์ หลวงทิพย์')
        .parent().parent().find('input[type="number"]').clear().type(remainingValue);
    });

    cy.contains("button", "บันทึกค่าใช้จ่าย").click();
    cy.wait("@createBill");
    cy.url().should("include", "/#/bill/1");
  });

  it('should successfully add an expense with "By Tag Split"', () => {
    const alertStub = cy.stub();
    cy.on("window:alert", alertStub);

    cy.intercept("POST", "**/api/bills/create/by-tag", {
      statusCode: 201,
      body: { success: true, bill_id: 1 }
    }).as("createBillByTag");

    cy.contains('label', 'จำนวนเงิน').parent().find('input').clear().type("1200");
    cy.contains('label', 'คำอธิบาย').parent().find('input').type("ค่ากิจกรรม");
    cy.contains("label", "แบ่งตามแท็ก").click();

    cy.get('input[placeholder="แท็ก 1"]').clear().type("อาหาร");
    cy.get('input[placeholder="แท็ก 1"]').parent().parent().find('input[type="number"]').first().clear().type("800");

    cy.wait(500);
    cy.get('input[type="checkbox"]').eq(2).uncheck({ force: true });

    cy.get('input[type="number"]').eq(2).clear().type("400");
    cy.get('input[type="number"]').eq(3).clear().type("400");

    cy.contains("button", "เพิ่มแท็ก").click();
    cy.wait(500);

    cy.get('input[placeholder="แท็ก 2"]').should('be.visible').clear().type("เครื่องดื่ม");
    cy.get('input[placeholder="แท็ก 2"]').parent().parent().find('input[type="number"]').first().clear().type("400");

    cy.get('input[type="number"]').its('length').then((length) => {
      const startIndex = length - 4;
      cy.get('input[type="number"]').eq(startIndex + 1).clear().type("100"); // เกียรติกุล
      cy.get('input[type="number"]').eq(startIndex + 2).clear().type("100"); // พรพวีร์
      cy.get('input[type="number"]').eq(startIndex + 3).clear().type("200"); // สุฑาณทิพย์
    });

    cy.contains("button", "บันทึกค่าใช้จ่าย").click();
    cy.wait(500);

    // ✅ ตรวจสอบว่าไม่มี validation error alert (เช่น “กรุณา...”)
    cy.then(() => {
      const alerts = alertStub.getCalls().map(call => call.args[0]);
      alerts.forEach(msg => {
        expect(msg).to.not.include("กรุณา");
      });
    });

    cy.wait("@createBillByTag").its("response.statusCode").should("eq", 201);
    cy.url().should("include", "/#/bill/1");
  });

  it("should show validation alerts for invalid form submission", () => {
    const alertStub = cy.stub();
    cy.on("window:alert", alertStub);

    // กรณีที่ไม่กรอกคำอธิบาย
    cy.contains('label', 'จำนวนเงิน').parent().find('input').clear().type("500");
    cy.contains('label', 'คำอธิบาย').parent().find('input').clear();
    cy.contains("button", "บันทึกค่าใช้จ่าย").click().then(() => {
      expect(alertStub.getCall(0)).to.be.calledWith("กรุณากรอกคำอธิบาย");
    });

    // กรณีจำนวนเงิน = 0
    cy.contains('label', 'คำอธิบาย').parent().find('input').type("ค่าขนม");
    cy.contains('label', 'จำนวนเงิน').parent().find('input').clear().type("0");
    cy.contains("button", "บันทึกค่าใช้จ่าย").click().then(() => {
      expect(alertStub.getCall(1)).to.be.calledWith("กรุณากรอกจำนวนเงินรวมให้ถูกต้อง (ต้องมากกว่า 0)");
    });
  });
});
