describe('6. Bill Payment Flow', () => {

  const GROUP_ID = 1;
  const BILL_ID = 50;

  // เพิ่มตัวดักจับ Error ที่มาจาก Vite (WebSocket) และ Font
  Cypress.on('uncaught:exception', (err, runnable) => {
    if (err.message.includes('WebSocket closed without opened')) {
      // นี่คือ Error จาก Vite HMR, ให้ข้ามไป
      return false;
    }
    if (err.message.includes('Sarabun-Regular.ttf') || err.message.includes('Sarabun-Bold.ttf')) {
      // Error โหลด Font, ให้ข้ามไป
      console.warn('Font loading failed, but test continues.');
      return false;
    }
    // คืนค่า true เพื่อให้ Cypress fail กับ error อื่นๆ
    return true; 
  });

  beforeEach(() => {
    // 1. Mock API
    // (Mock ข้อมูลบิลจาก Bill.jsx)
    cy.intercept('GET', `/api/bills/group/${GROUP_ID}`, { fixture: 'bill_page.json' }).as('getBillPage');
    // (Mock ข้อมูลการจ่ายเงินของบิลนี้)
    cy.intercept('GET', `/api/payments/bill/${BILL_ID}`, { fixture: 'payments.json' }).as('getPayments');

    // 2. Login และไปหน้า Payment
    cy.login(3, 'Charlie'); // ⭐️ ล็อกอินเป็น "Charlie" (ID 3) ซึ่งเป็นลูกหนี้
    cy.visit(`/#/bill/${GROUP_ID}/payment/${BILL_ID}`);

    // 3. รอ API
    cy.wait('@getBillPage');
    cy.wait('@getPayments');
  });

  it('ควรแสดง QR Code และข้อมูลหนี้ที่ค้างชำระ', () => {
    // (Bill 1500 / 3 = 500) (Charlie จ่ายไปแล้ว 100)
    cy.contains('ยังจ่ายไม่ครบ').should('be.visible');
    cy.contains('ยังค้างชำระ ฿400.00').should('be.visible');

    //หา QR Code ให้เสถียรขึ้น
    cy.contains('สแกนเพื่อจ่ายให้ Alice')
      .parent()
      .find('svg')
      .should('be.visible');
  });



  it('ควรบันทึกการชำระเงินและอัปโหลดสลิปได้', () => {
    // 1. Arrange: Mock API
    cy.intercept('POST', 'https://api.imgbb.com/1/upload*', {
      statusCode: 200,
      body: { success: true, data: { url: 'http://mock.com/slip.jpg' } }
    }).as('imgbbUpload');
    
    cy.intercept('POST', '/api/payments/create', {
      statusCode: 201,
      body: { paymentId: 10, amount: 400.00 }
    }).as('createPayment');

    // 2. Act: กรอกจำนวนเงิน (ค่า default ควรเป็น 400.00)
    cy.get('input[type="number"]').should('have.value', '400.00');
    
    // 3. Act: อัปโหลดสลิป
    cy.get('input[type="file"][id="slip-upload"]')
      .selectFile('cypress/fixtures/test-image.jpg', { force: true });
    cy.get('img[alt="รูปสลิป"]').should('be.visible');

    // 4. Act: คลิกบันทึก
    cy.contains('button', 'บันทึกการชำระเงิน').click();
    
    // 5. Assert: API ถูกเรียก
    cy.wait('@imgbbUpload');
    cy.wait('@createPayment').its('request.body').should('deep.include', {
      billId: BILL_ID,
      payerUserId: 3, // (Charlie)
      amount: 400.00,
      slipImageUrl: 'http://mock.com/slip.jpg'
    });

    // 6. Assert: นำทางกลับ
    cy.url().should('include', `/bill/${GROUP_ID}`);
  });
});