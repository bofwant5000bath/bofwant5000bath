// cypress/e2e/empty_state_flow.cy.js
// Final Version: แก้ไขให้ตรวจสอบตามสิ่งที่ Frontend แสดงผลจริง

describe("Empty State Flow", () => {
  beforeEach(() => {
    cy.intercept("GET", "**/api/groups/dashboard/*", { fixture: "dashboard_with_empty_group.json" }).as("getDashboard");
    cy.visit("/#/dashboard", {
      onBeforeLoad(win) {
        win.localStorage.setItem("user_id", "1");
        win.localStorage.setItem("full_name", "เกียรติกุล เข้มแข็ง");
      },
    });
    cy.wait("@getDashboard");
  });

  it("should not display any bill cards when a group is empty", () => {
    cy.intercept("GET", "**/api/bills/group/2", { fixture: "empty_group.json" }).as("getEmptyGroup");

    cy.contains("ค่าห้องเดือนนี้").click();
    cy.wait("@getEmptyGroup");
    cy.url().should("include", "/#/bill/2");

    // ✅ FIXED: ตรวจสอบตามความเป็นจริงของ Frontend
    // 1. ตรวจสอบว่ายอดรวมเป็น 0.00
    cy.contains("ยอดรวมของกลุ่ม").parent().contains("฿0.00").should("be.visible");

    // 2. ตรวจสอบว่าไม่มีข้อความที่เป็นส่วนหนึ่งของ Bill Card แสดงอยู่เลย
    //    เช่น คำว่า "จ่ายโดย:" ซึ่งจะปรากฏก็ต่อเมื่อมีบิลเท่านั้น
    cy.contains("จ่ายโดย:").should("not.exist");

    // 3. ตรวจสอบว่าปุ่ม "เพิ่มค่าใช้จ่าย" ยังคงอยู่และใช้งานได้
    cy.contains("เพิ่มค่าใช้จ่าย").should("be.visible").click();
    cy.url().should("include", "/#/addexpense/2");
  });
});