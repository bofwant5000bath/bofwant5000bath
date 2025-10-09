// cypress/e2e/group_flow.cy.js

describe('Group Management Flow', () => {
  let loggedInUser;

  beforeEach(() => {
    // ทำการ login ก่อน
    cy.request('POST', 'http://localhost:8080/api/auth/login', {
      username: 'keatikun',
      password: 'password_1',
    }).then((response) => {
      loggedInUser = response.body;
      localStorage.setItem('user_id', loggedInUser.user_id);
      localStorage.setItem('full_name', loggedInUser.full_name);
    });

    // ตั้ง intercepts
    cy.intercept('GET', '**/api/groups/users').as('getUsersRequest');
    cy.intercept('POST', '**/api/groups/create').as('createGroupRequest');
    cy.intercept('GET', '**/api/groups/dashboard/**').as('dashboardRequest');
  });

  it('ควรอนุญาตให้ผู้ใช้ที่ล็อกอินแล้ว สร้างกลุ่มใหม่ได้สำเร็จ', () => {
    const newGroupName = `E2E Test Group ${Date.now()}`;

    // เข้าหน้า dashboard
    cy.visit('/#/dashboard');
    cy.wait('@dashboardRequest');

    // คลิก "สร้างกลุ่มใหม่"
    cy.contains('สร้างกลุ่มใหม่').click();
    cy.url().should('include', '/#/create-group');

    // รอข้อมูล user ที่ดึงมา
    cy.wait('@getUsersRequest').then((interception) => {
      const allUsers = interception.response.body;

      // หาเพื่อนที่ไม่ใช่ user ที่ล็อกอิน
      const friendToSelect = allUsers.find(user => user.userId !== loggedInUser.user_id);
      expect(friendToSelect).to.not.be.undefined;

      // --- แก้ไขตรงนี้เพื่อหลีกเลี่ยงปัญหา re-render ---
      cy.contains(friendToSelect.fullName, { timeout: 7000 })
        .parent()
        .find('input[type="checkbox"]')
        .should('exist') // รอให้ checkbox มีใน DOM
        .check({ force: true }); // ใช้ force เผื่อ element ถูกซ่อนบางส่วน

      // พิมพ์ชื่อกลุ่ม
      cy.get('input[placeholder="เช่น ทริปเที่ยวทะเล, ค่าห้องเดือนนี้"]').type(newGroupName);
    });

    // คลิก "เสร็จสิ้น" เพื่อสร้างกลุ่ม
    cy.contains('button', 'เสร็จสิ้น').click();

    // ตรวจสอบว่า request สำเร็จ
    cy.wait('@createGroupRequest').its('response.statusCode').should('be.oneOf', [200, 201]);

    // กลับมาหน้า dashboard และเห็นชื่อกลุ่มใหม่
    cy.url().should('include', '/#/dashboard');
    cy.contains(newGroupName).should('be.visible');
  });
});
