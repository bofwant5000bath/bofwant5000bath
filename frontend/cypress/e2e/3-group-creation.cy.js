// 3-group-creation.cy.js (ฉบับแก้ไขให้ตรงกับ Frontend ปัจจุบัน)

describe('3. Group Creation Flow', () => {

  
  Cypress.on('uncaught:exception', (err, runnable) => {
    if (err.message.includes('WebSocket closed without opened')) {
      return false; 
    }
    return true;
  });

  beforeEach(() => {
    // 1. Login
    cy.login(1, 'Test User');
    
    // 2. Mock API
    cy.intercept('GET', '**/api/groups/users*', { fixture: 'users.json' }).as('getUsers');
    cy.intercept('GET', '**/api/groups/all-with-members/1*', { fixture: 'all_groups.json' }).as('getGroups');
    
    // 3. Visit
    cy.visit('/#/create-group');
    
    // 4. Wait
    cy.wait('@getUsers');
    cy.wait('@getGroups');
  });

  it('ควรสร้างกลุ่มสำเร็จเมื่อกรอกข้อมูลครบ', () => {
    // 1. Arrange: Mock API สร้างกลุ่ม
    cy.intercept('POST', '**/api/groups/create', {
      statusCode: 201,
      body: { groupId: 3, groupName: 'กลุ่ม Cypress Test' }
    }).as('createGroup');

    // 2. Act: กรอกฟอร์ม
    cy.get('input[placeholder="เช่น ทริปเที่ยวทะเล, ค่าห้องเดือนนี้"]').type('กลุ่ม Cypress Test');
    
    // 3. Act: เลือกเพื่อน (User ID 2: 'Bob' จาก users.json)
    cy.contains('label', 'Bob').find('input[type="checkbox"]').check();

    // 4. Act: คลิก Submit
    cy.contains('button', 'เสร็จสิ้น').click();

    // 5. Assert: ตรวจสอบ API
    cy.wait('@createGroup').its('request.body').should('deep.include', {
      groupName: 'กลุ่ม Cypress Test',
      createdByUserId: 1,
      memberIds: [1, 2] // (1 คือตัวเอง, 2 คือ Bob)
    });

    // 6. Assert: นำทางกลับ Dashboard
    cy.url().should('include', '/dashboard');
  });
  
  it('ควรค้นหา (filter) เพื่อนใน Tab "เพื่อน" ได้', () => {
    // 1. Assert (Initial State)
    cy.contains('label', 'Bob').should('be.visible');
    cy.contains('label', 'Charlie').should('be.visible');

    // 2. Act: พิมพ์ "Bo" ในช่องค้นหา
    // (*** หมายเหตุ: "ค้นหาชื่อเพื่อน" ต้องตรงกับ placeholder ของคุณ ***)
    cy.get('input[placeholder="ค้นหาชื่อเพื่อน"]').should('be.visible').type('Bo');

    // 3. Assert (Filtered State)
    cy.contains('label', 'Bob').should('be.visible');
    cy.contains('label', 'Charlie').should('not.exist');

    // 4. Act: ลบข้อความ
    cy.get('input[placeholder="ค้นหาชื่อเพื่อน"]').clear();

    // 5. Assert (Reset State)
    cy.contains('label', 'Bob').should('be.visible');
    cy.contains('label', 'Charlie').should('be.visible');
  });

  it('ควรแสดงรูป default สำหรับเพื่อนที่ไม่มีรูปโปรไฟล์', () => {
    // 1. Arrange: Override mock ให้ 'Bob' ไม่มีรูป
    cy.intercept('GET', '**/api/groups/users*', {
      statusCode: 200,
      body: [
        { "userId": 2, "fullName": "Bob (No Pic)", "profilePictureUrl": null }, // ❗️ Bob ไม่มีรูป
        { "userId": 3, "fullName": "Charlie", "profilePictureUrl": "http://example.com/charlie.jpg" }
      ]
    }).as('getUsersNoPic');
    
    // (เราต้องดัก 'getGroups' อีกครั้ง เพราะ cy.reload() จะเรียก API ทั้งหมดใหม่)
    cy.intercept('GET', '**/api/groups/all-with-members/1*', { fixture: 'all_groups.json' }).as('getGroupsReload');

    // 2. Act: ใช้ cy.reload() บังคับโหลดหน้าใหม่
    cy.reload();

    // 3. Assert: รอ API ที่โหลดใหม่
    cy.wait('@getUsersNoPic');
    cy.wait('@getGroupsReload');

    // 4. Assert: ตรวจสอบรูปภาพ
    
    // ✅✅✅ [แก้ไขแล้ว] ✅✅✅
    // เปลี่ยนกลับมาใช้ URL ที่หน้าเว็บแสดงผลจริง
    const DEFAULT_IMG_URL = "https://via.placeholder.com/150"; 

    cy.contains('label', 'Bob (No Pic)')
      .find('img')
      .should('have.attr', 'src', DEFAULT_IMG_URL); // <-- แก้ไขจุดนี้

    // เทสของ Charlie ยังเหมือนเดิม
    cy.contains('label', 'Charlie')
      .find('img')
      .should('have.attr', 'src', 'http://example.com/charlie.jpg');
  });

  it('ควรดึงสมาชิกจากกลุ่มอื่นมาเพิ่มได้ (Tab กลุ่ม)', () => {
    // 1. Act: เปลี่ยน Tab
    cy.contains('button', 'กลุ่ม').click();

    // 2. Act: ค้นหา "กลุ่มเก่า" และคลิก "เพิ่มสมาชิก"
    // (*** หมายเหตุ: "กลุ่มเก่า" ต้องมีอยู่ใน fixture 'all_groups.json' ***)
    cy.contains('span', 'กลุ่มเก่า')
      .parents('div.flex.items-center.justify-between')
      .first()
      .contains('button', 'เพิ่มสมาชิก')
      .click();

    // 3. Assert: ตรวจสอบว่าสมาชิกถูกเพิ่มใน List
    cy.contains('h3', 'สมาชิกที่เลือก (2 คน)').should('be.visible'); 
    cy.contains('span', 'Charlie').should('be.visible');
  });
});