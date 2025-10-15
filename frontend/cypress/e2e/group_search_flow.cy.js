// cypress/e2e/group_search_flow.cy.js
// Final Version: แก้ไขให้ตรวจสอบเฉพาะภายใน List "เพื่อนทั้งหมด"

describe("Create Group - Search Functionality", () => {
  beforeEach(() => {
    cy.intercept("GET", "**/api/groups/users", { fixture: "users.json" }).as("getAllUsers");

    cy.visit("/#/dashboard", {
      onBeforeLoad(win) {
        win.localStorage.setItem("user_id", "1");
        win.localStorage.setItem("full_name", "เกียรติกุล เข้มแข็ง");
      },
    });
    
    cy.contains("สร้างกลุ่มใหม่").click();
    cy.wait("@getAllUsers");
    cy.url().should("include", "/#/create-group");
  });

  it("should filter the user list based on search term", () => {
    // ✅ FIXED: หา Container ของ "เพื่อนทั้งหมด" เก็บไว้ใน alias เพื่อใช้อ้างอิง
    cy.contains('h3', 'เพื่อนทั้งหมด').next('div').as('allUsersList');

    // ตรวจสอบว่าตอนแรกเห็นครบทุกคนใน List
    cy.get('@allUsersList').contains("พรพวีร์ พัฒนพรวิวัฒน์").should("be.visible");
    cy.get('@allUsersList').contains("สุฑาณทิพย์ หลวงทิพย์").should("be.visible");

    // พิมพ์เพื่อค้นหา
    cy.get('input[placeholder="ค้นหาชื่อเพื่อน"]').type("พรพวีร์");

    // ตรวจสอบว่าเหลือแค่คนที่ค้นหาใน List
    cy.get('@allUsersList').contains("พรพวีร์ พัฒนพรวิวัฒน์").should("be.visible");
    
    // ✅ FIXED: ตรวจสอบว่าคนอื่นหายไปจาก List "เพื่อนทั้งหมด" เท่านั้น
    cy.get('@allUsersList').contains("เกียรติกุล เข้มแข็ง").should("not.exist");
    cy.get('@allUsersList').contains("สุฑาณทิพย์ หลวงทิพย์").should("not.exist");

    // ตรวจสอบเพิ่มเติมว่าชื่อ "เกียรติกุล" ยังอยู่ในโซน "เพื่อนที่เลือก"
    cy.contains('h3', 'เพื่อนที่เลือก').parent().contains("เกียรติกุล เข้มแข็ง").should('be.visible');
  });

  it("should restore the full user list when the search term is cleared", () => {
    cy.contains('h3', 'เพื่อนทั้งหมด').next('div').as('allUsersList');
    
    // พิมพ์เพื่อค้นหา
    cy.get('input[placeholder="ค้นหาชื่อเพื่อน"]').type("สุฑา");

    // ตรวจสอบว่ารายชื่อถูกกรอง
    cy.get('@allUsersList').contains("สุฑาณทิพย์ หลวงทิพย์").should("be.visible");
    cy.get('@allUsersList').contains("พรพวีร์ พัฒนพรวิวัฒน์").should("not.exist");

    // ล้างช่องค้นหา
    cy.get('input[placeholder="ค้นหาชื่อเพื่อน"]').clear();
    
    // ตรวจสอบว่ารายชื่อกลับมาครบทุกคนใน List
    cy.get('@allUsersList').contains("เกียรติกุล เข้มแข็ง").should("be.visible");
    cy.get('@allUsersList').contains("พรพวีร์ พัฒนพรวิวัฒน์").should("be.visible");
    cy.get('@allUsersList').contains("สุฑาณทิพย์ หลวงทิพย์").should("be.visible");
  });
});