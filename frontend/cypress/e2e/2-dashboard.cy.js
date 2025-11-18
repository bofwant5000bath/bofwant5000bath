describe('2. Dashboard Flow', () => {

  // เพิ่ม Event Listener นี้เพื่อดัก "uncaught exception"
  Cypress.on('uncaught:exception', (err, runnable) => {
    if (err.message.includes('WebSocket closed without opened')) {
      return false; 
    }

    return true; 
  });


  beforeEach(() => {
    // 1. Mock API (สำหรับหน้า Dashboard)
    cy.intercept('GET', '**/api/groups/dashboard/1*', {
      fixture: 'dashboard.json' 
    }).as('getDashboard');

    // 2. Act: ล็อกอินและเข้าหน้า Dashboard
    cy.login(1, 'Test User', 'https://picsum.photos/id/10/150'); 
    cy.visit('/#/dashboard');
    
    // 3. Assert: รอให้ API โหลดเสร็จ
    cy.wait('@getDashboard');
  });

  it('ควรแสดงข้อมูลสรุป (Owed/Receivable) และ Group Cards', () => {
    cy.contains('ยอดรวมที่ต้องชำระ: ฿500.00').should('be.visible');
    cy.contains('ยอดรวมที่จะได้รับ: ฿1,200.50').should('be.visible');
    cy.contains('h2', 'ทริปเที่ยวทะเล').should('be.visible');
    cy.contains('h2', 'ค่าห้องเดือนตุลา').should('be.visible');
  });

  it('ควรปักหมุด (Pin) กลุ่มได้ และ UI ควรอัปเดต', () => {
    cy.intercept('POST', '**/api/groups/pin', { statusCode: 200 }).as('pinGroup');

    cy.contains('h2', 'ค่าห้องเดือนตุลา')
      .closest('div.relative') 
      .find('button[title="ปักหมุดกลุ่มนี้"]')
      .click();

    cy.wait('@pinGroup').its('request.body').should('deep.include', {
      groupId: 2,
      pin: true
    });
    
    cy.contains('h2', 'ปักหมุด')
      .parent() 
      .should('contain', 'ค่าห้องเดือนตุลา');
  });

  it('ควรนำทางไปหน้า Bill เมื่อคลิก Group Card', () => {
    cy.contains('h2', 'ทริปเที่ยวทะเล').click();
    cy.url().should('include', '/bill/1'); 
  });

  it('ควรนำทางไปหน้า Create Group', () => {
    
    // 1. Arrange: Mock API ที่หน้า CreateGroup จะเรียกใช้
    cy.intercept('GET', '**/api/groups/users*', { fixture: 'users.json' }).as('getUsers');
    cy.intercept('GET', '**/api/groups/all-with-members/1*', { fixture: 'all_groups.json' }).as('getGroups');

    // 2. Act: คลิกปุ่ม
    cy.contains('สร้างกลุ่มใหม่').click();
    
    // 3. Assert: ตรวจสอบ URL
    cy.url().should('include', '/create-group');

    // 4. Assert: รอให้ API ของหน้าใหม่โหลดเสร็จ
    cy.wait('@getUsers');
    cy.wait('@getGroups');
    
    // 5. Assert: ตรวจสอบว่าหน้า CreateGroup render ถูกต้อง
    cy.contains('h3', 'สมาชิกที่เลือก (1 คน)').should('be.visible');
  });

  it('ควร Logout เมื่อคลิกปุ่ม Logout', () => {
    cy.get('polyline[points="10 17 15 12 10 7"]')
      .parent('svg') 
      .parent('button') 
      .click(); 
      
    cy.url().should('include', '/login');
    
    cy.window().its('localStorage').should((localStorage) => {
      expect(localStorage.getItem('user_id')).to.be.null;
    });
  });

});