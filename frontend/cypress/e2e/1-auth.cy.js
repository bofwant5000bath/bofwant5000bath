describe('1. Authentication Flow (Login & Register)', () => {

  Cypress.on('uncaught:exception', (err, runnable) => {
    if (err.message.includes('WebSocket closed without opened')) {
      return false;
    }
    return true;
  });

  beforeEach(() => {
    cy.clearLocalStorage();
    cy.visit('/#/login'); 
  });

  it('ควรแสดงหน้า Login เป็นหน้าแรก', () => {
    cy.url().should('include', '/login');
    cy.contains('h2', 'เข้าสู่ระบบ').should('be.visible');
  });

  it('ควรนำทางไปหน้า Register เมื่อคลิกลิงก์ลงทะเบียน', () => {
    cy.contains('ลงทะเบียนที่นี่').click();
    cy.url().should('include', '/register');
    cy.contains('h1', 'สร้างบัญชีใหม่').should('be.visible');
  });

  it('ควรแสดง Error เมื่อ Login ผิดพลาด', () => {
    cy.intercept('POST', '/api/auth/login', {
      statusCode: 401,
      body: { message: 'ชื่อผู้ใช้หรือหัสผ่านไม่ถูกต้อง' }
    }).as('loginRequest');

    cy.get('input[name="username"]').type('wronguser');
    cy.get('input[name="password"]').type('wrongpass');
    cy.get('button[type="submit"]').click();

    cy.wait('@loginRequest');
    cy.contains('ชื่อผู้ใช้หรือหัสผ่านไม่ถูกต้อง').should('be.visible');
    cy.url().should('include', '/login'); 
  });

  it('ควร Login สำเร็จ และนำทางไป Dashboard', () => {
    // 1. Arrange
    cy.intercept('POST', '/api/auth/login', {
      statusCode: 200,
      body: {
        user_id: 1,
        full_name: 'Test User',
        profile_picture_url: 'http://example.com/pic.jpg'
      }
    }).as('loginRequest');

    // 2. Act
    cy.get('input[name="username"]').type('testuser');
    cy.get('input[name="password"]').type('password123');
    cy.get('button[type="submit"]').click();

    // 3. Assert: รอ API
    cy.wait('@loginRequest');
    
    // 4. Assert รอยืนยันว่าหน้านำทางไป Dashboard แล้ว
    cy.url().should('include', '/dashboard');

    // 5. Assert เช็ค localStorage
    cy.window().its('localStorage').should('have.property', 'user_id', '1');
    cy.window().its('localStorage').should('have.property', 'full_name', 'Test User');
  });
  


  it('ควรลงทะเบียนสำเร็จ (Register)', () => {
    cy.intercept('POST', 'https://api.imgbb.com/1/upload*', {
      statusCode: 200,
      body: { success: true, data: { url: 'http://mock.com/img.jpg' } }
    }).as('imgbbUpload');

    cy.intercept('POST', '/api/auth/register', {
      statusCode: 201,
      body: { message: 'การลงทะเบียนสำเร็จ' }
    }).as('registerRequest');
    
    cy.contains('ลงทะเบียนที่นี่').click();

    cy.get('input[name="name"]').type('New User');
    cy.get('input[name="username"]').type('newuser');
    cy.get('input[name="password"]').type('newpass123');
    cy.get('input[name="confirm-password"]').type('newpass123');
    
    // (จำลองการอัปโหลดไฟล์)
    cy.get('input[type="file"]').selectFile('cypress/fixtures/test-image2.jpg', { force: true });

    cy.get('button[type="submit"]').click();

    cy.wait('@imgbbUpload');
    cy.wait('@registerRequest');

    cy.contains('การลงทะเบียนสำเร็จ').should('be.visible');
    cy.url().should('include', '/login');
  });

});