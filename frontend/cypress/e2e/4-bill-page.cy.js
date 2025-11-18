describe('Bill Page Flow (/bill/:groupId)', () => {

  Cypress.on('uncaught:exception', (err, runnable) => {
    if (err.message.includes('WebSocket closed without opened')) return false;
    if (err.message.includes('Sarabun-Regular.ttf') || err.message.includes('Sarabun-Bold.ttf')) {
      console.warn('Font loading failed, but test continues.');
      return false;
    }
    return true; 
  });

  beforeEach(() => {
    // 1. Login
    cy.login(1, 'Test User');

    // 2. Mock API
    cy.intercept('GET', '**/bills/group/1', {
      fixture: 'group-bill-details.json'
    }).as('getGroupBills');

    cy.intercept('GET', '**/groups/1/settle', {
      fixture: 'smart-settlement.json'
    }).as('getSettlement');

    // 3. Visit page
    cy.visit('/#/bill/1');

    // 4. Wait for API with timeout
    cy.wait(['@getGroupBills', '@getSettlement'], { timeout: 10000 });
  });

  
  it('ควรแสดงรายละเอียดกลุ่ม, ยอดรวม, และรายการบิลถูกต้อง', () => {
    cy.contains('กลุ่มของฉัน', { timeout: 10000 }).should('be.visible');
    cy.contains('฿1,500.00', { timeout: 10000 }).should('be.visible');
    cy.contains('ชำระหนี้อัจฉริยะ', { timeout: 10000 }).should('be.visible');
    cy.contains('Charlie').should('exist');
    cy.contains('Bob').should('exist');
    cy.contains('ค่าอาหารเย็น (บิล 101)', { timeout: 10000 }).should('be.visible');
    cy.contains('ค่าที่พัก (บิล 102)', { timeout: 10000 }).should('be.visible');
  });

  it('ควรนำทางไปยังหน้า Add Expense เมื่อคลิกปุ่ม "เพิ่มค่าใช้จ่าย"', () => {
    cy.contains('button', '+ เพิ่มค่าใช้จ่าย', { timeout: 10000 })
      .should('be.visible')
      .click();
    cy.url().should('include', '/addexpense/1');
  });

  it('ควรนำทางไปยังหน้า Bill Payment เมื่อคลิกที่รายการบิล', () => {
    cy.contains('ค่าอาหารเย็น (บิล 101)', { timeout: 10000 })
      .should('be.visible')
      .click();
    cy.url().should('include', '/bill/1/payment/101');
  });

  it('ควรเปิด Modal PDF Preview เมื่อคลิกปุ่ม PDF', () => {
    cy.get('button').contains('picture_as_pdf', { timeout: 10000 })
      .should('be.visible')
      .click();
    cy.contains('ตัวอย่างใบเสร็จ (PDF)', { timeout: 10000 }).should('be.visible');
    cy.contains('a', 'ดาวน์โหลด PDF').should('be.visible');
    cy.contains('button', '×').click();
    cy.contains('ตัวอย่างใบเสร็จ (PDF)').should('not.exist');
  });


  it('ควรเปิด Modal เพิ่มผู้เข้าร่วม และเพิ่มสมาชิกใหม่ได้', () => {
    // 1. Arrange: Mock API 
    cy.intercept('GET', '**/api/groups/users*', { 
      fixture: 'users.json' 
    }).as('getUsers');
    
    cy.intercept('GET', '**/api/groups/all-with-members/1*', { 
      fixture: 'all_groups.json' 
    }).as('getGroups');

    cy.intercept('POST', '**/api/groups/1/members', {
      statusCode: 200,
      body: { message: 'เพิ่มสมาชิกสำเร็จ' }
    }).as('addMember');

    // 2. Act: คลิกปุ่มเปิด Modal
    cy.get('button').contains('group_add', { timeout: 10000 })
      .should('be.visible')
      .click();

    // 3. Assert: Modal เปิดขึ้นมา
    cy.wait('@getUsers'); 
    cy.wait('@getGroups');
    cy.contains('เพิ่มผู้เข้าร่วม').should('be.visible');

    // 4. Act: ค้นหาและเลือก User
    // (*** สมมติว่า User 'David' มี ID 4 ***)
    // (ถ้าใน 'users.json' คุณใช้ชื่ออื่น ให้เปลี่ยน 'David' และ ID ที่คาดหวัง)
    cy.contains('label', 'David')
      .find('input[type="checkbox"]')
      .check();

    // 5. Act: กดยืนยัน
    cy.contains('button', 'ยืนยัน').click();

    // 6. Assert: API ถูกเรียกด้วย
    // +++ แก้ไขตรงนี้: คาดหวังเฉพาะ ID ใหม่ [4] +++
    cy.wait('@addMember').its('request.body').should('deep.equal', {
      memberIds: [4] 
    });

    // 7. Assert: Modal ปิด
    cy.contains('เพิ่มผู้เข้าร่วม').should('not.exist');
  });

});