describe("SplitBill Main User Flow", () => {
  beforeEach(() => {
    cy.intercept("GET", "**/api/groups/dashboard/*", {
      fixture: "dashboard.json",
    }).as("getDashboardData");

    cy.visit("/#/dashboard", {
      onBeforeLoad(win) {
        win.localStorage.setItem("user_id", "1");
        win.localStorage.setItem("full_name", "เกียรติกุล เข้มแข็ง");
      },
    });

    cy.wait("@getDashboardData");
  });

  it("should display dashboard data correctly", () => {
    cy.contains("เกียรติกุล เข้มแข็ง").should("be.visible");
    cy.contains("ยอดรวมที่ต้องชำระ: ฿1,000.00").should("be.visible");
    cy.contains("ทริปเที่ยวทะเล").should("be.visible");
  });

  it("should create a new group successfully", () => {
    cy.intercept("GET", "**/api/groups/users", { fixture: "users.json" }).as("getAllUsers");
    cy.intercept("POST", "**/api/groups/create", {
      statusCode: 201,
      body: { groupId: 3, groupName: "กลุ่ม Cypress Test" },
    }).as("createGroup");

    cy.contains("สร้างกลุ่มใหม่").click();
    cy.wait("@getAllUsers");
    cy.url().should("include", "/#/create-group");

    cy.get('input[placeholder="เช่น ทริปเที่ยวทะเล, ค่าห้องเดือนนี้"]')
      .type("กลุ่ม Cypress Test");
      
    cy.contains('label', 'พรพวีร์ พัฒนพรวิวัฒน์')
      .find('input[type="checkbox"]')
      .check({ force: true });

    cy.contains("button", "เสร็จสิ้น").click();
    cy.wait("@createGroup");
    cy.url().should("include", "/#/dashboard");
  });

  it("should add a new expense with equal split successfully", () => {
    cy.intercept("GET", "**/api/bills/group/1", { fixture: "group1_bills.json" }).as("getGroupBills");
    cy.intercept("POST", "**/api/bills/create", {
      statusCode: 201,
      body: { message: "Bill created successfully" },
    }).as("createBill");

    cy.contains("ทริปเที่ยวทะเล").click();
    cy.wait("@getGroupBills");
    cy.url().should("include", "/#/bill/1");
    cy.contains("เพิ่มค่าใช้จ่าย").click();
    cy.url().should("include", "/#/addexpense/1");

    cy.contains('label', 'จำนวนเงิน')
      .next('div')
      .find('input')
      .clear()
      .type("1500");

    cy.contains('label', 'คำอธิบาย')
      .next('input')
      .type("ค่ากาแฟ");
      
    cy.contains('span', 'สุฑาณทิพย์ หลวงทิพย์')
      .parent()
      .find('input[type="checkbox"]')
      .uncheck({ force: true });

    cy.contains("button", "บันทึกค่าใช้จ่าย").click();

    // ✅ FINAL FIX: เพิ่ม .then() เพื่อ Debug และตรวจสอบ Payload อย่างละเอียด
    cy.wait("@createBill").its('request.body').should('deep.equal', {
        groupId: 1,
        title: "ค่าอาหารเย็นค่ากาแฟ",
        description: "ค่าอาหารเย็นค่ากาแฟ",
        amount: 1500,
        paidByUserId: 1,
        splitMethod: "equal",
        // เมื่อ uncheck สุฑาณทิพย์ (ID 3), จะเหลือแค่ ID 1 และ 2
        selectedParticipantIds: [1, 2] 
    });

    cy.url().should("include", "/#/bill/1");
  });
});