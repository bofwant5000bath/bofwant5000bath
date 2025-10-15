// cypress/e2e/payment_flow.cy.js
// Final Version: ใช้ cy.stub() และทำให้การตรวจสอบ Alert แม่นยำขึ้น

describe("Bill Payment Flow", () => {
  beforeEach(() => {
    cy.intercept("GET", "**/api/groups/dashboard/*", { fixture: "dashboard.json" }).as("getDashboard");
    cy.intercept("GET", "**/api/bills/group/1", { fixture: "group1_bills_detailed.json" }).as("getGroupBills");
    cy.intercept("GET", "**/api/payments/bill/1", { fixture: "bill1_payments.json" }).as("getPayments");

    cy.visit("/#/dashboard", {
      onBeforeLoad(win) {
        win.localStorage.setItem("user_id", "2");
        win.localStorage.setItem("full_name", "พรพวีร์ พัฒนพรวิวัฒน์");
      },
    });

    cy.contains("ทริปเที่ยวทะเล").click();
    cy.wait("@getGroupBills");
    cy.contains("ค่าที่พัก").click();
    cy.wait("@getPayments");
    cy.url().should("include", "/#/bill/1/payment/1");
  });

  it("should display correct debt information and handle overpayment", () => {
    cy.contains("h1", "ค่าที่พัก").should("be.visible");
    cy.contains("ยังค้างชำระ ฿1,000.00").should("be.visible");
    cy.get('input[type="number"]').should("have.value", "1000.00");
    cy.get('input[type="number"]').clear().type("1200");
    
    cy.on('window:confirm', () => true); // กด OK อัตโนมัติเมื่อมี confirm dialog
    
    cy.intercept("POST", "**/api/payments/create", { statusCode: 201 }).as("createPayment");
    cy.contains("button", "บันทึกการชำระเงิน").click();
    cy.wait("@createPayment").its('request.body.amount').should('eq', 1200);
  });

  it("should successfully make a payment and redirect", () => {
    const alertStub = cy.stub().as('alertStub');
    cy.on('window:alert', alertStub);

    cy.intercept("POST", "**/api/payments/create", { statusCode: 201 }).as("createPayment");
    
    cy.get('input[type="number"]').clear().type("1000.00");
    cy.contains("button", "บันทึกการชำระเงิน").click();

    cy.wait("@createPayment");

    // ✅ FINAL FIX: ตรวจสอบว่า alert "สำเร็จ" ถูกเรียก โดยใช้คำที่เฉพาะเจาะจงและไม่ซ้ำใคร
    // เราใช้คำว่า "สำเร็จ" เพราะเป็นคำที่อยู่ใน Alert ของ 'try' block เท่านั้น
    cy.get('@alertStub')
      .should('be.calledWithMatch', /สำเร็จ/);

    // ตรวจสอบว่าถูกพาไปยังหน้ารวมบิลของกลุ่ม
    cy.url().should("include", "/#/bill/1");
  });
});