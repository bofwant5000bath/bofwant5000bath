// // cypress/e2e/bill_flow.cy.js

// describe('Bill Management Flow', () => {
//   let loggedInUser;
//   let testGroup;

//   beforeEach(() => {
//     // 1. ล็อกอินผ่าน API เพื่อความรวดเร็วและเสถียร
//     cy.request('POST', 'http://localhost:8080/api/auth/login', {
//       username: 'keatikun',
//       password: 'password_1',
//     }).then((response) => {
//       loggedInUser = response.body;
//       localStorage.setItem('user_id', loggedInUser.user_id);
//       localStorage.setItem('full_name', loggedInUser.full_name);
//     });

//     // 2. สร้างกลุ่มใหม่ผ่าน API เพื่อให้มีข้อมูลที่สะอาดสำหรับเทส
//     // โดยเพิ่ม user id 1 (keatikun) และ 2 (pornpawee) เข้ากลุ่ม
//     cy.request({
//       method: 'POST',
//       url: 'http://localhost:8080/api/groups/create',
//       body: {
//         groupName: `Test Group for Bills ${Date.now()}`,
//         createdByUserId: 1,
//         memberIds: [1, 2],
//       },
//     }).then((response) => {
//       testGroup = response.body;
//     });

//     // 3. ดักจับ API ที่จะใช้ในหน้านี้
//     cy.intercept('POST', '**/api/bills/create').as('createBillRequest');
//     cy.intercept('GET', `**/api/bills/group/**`).as('getGroupBills');
//   });

//   it('ควรอนุญาตให้ผู้ใช้สร้างบิลใหม่แบบหารเท่าได้สำเร็จ', () => {
//     const billDescription = `E2E Lunch Bill ${Date.now()}`;
//     const billAmount = 1000;

//     // 1. เข้าไปที่หน้าบิลของกลุ่มที่เพิ่งสร้าง
//     // เราใช้ cy.then() เพื่อให้แน่ใจว่า `testGroup` มีข้อมูลแล้วก่อนที่จะ visit
//     cy.then(() => {
//       cy.visit(`/#/bill/${testGroup.groupId}`);
//     });

//     // 2. รอให้ข้อมูลบิลของกลุ่มโหลดเสร็จ (ถึงแม้จะยังไม่มีบิลก็ตาม)
//     cy.wait('@getGroupBills');

//     // 3. คลิกปุ่ม "เพิ่มค่าใช้จ่าย"
//     cy.contains('เพิ่มค่าใช้จ่าย').click();

//     // 4. กรอกฟอร์ม (จำลองจาก Figma)
//     // สมมติว่าหน้าฟอร์มมี input ตาม id หรือ data-cy attribute
//     cy.get('input[name="amount"]').type(billAmount);
//     cy.get('input[name="title"]').type(billDescription);
    
//     // (Optional) ตรวจสอบว่ามีสมาชิก 2 คนให้หาร
//     cy.get('.participant-list').children().should('have.length', 2);
//     // (Optional) ตรวจสอบว่า "แบ่งเท่าๆ กัน" ถูกเลือกอยู่
//     cy.get('input[value="equal"]').should('be.checked');

//     // 5. คลิกปุ่มบันทึก
//     cy.contains('button', 'บันทึกค่าใช้จ่าย').click();

//     // 6. รอให้ API สร้างบิลสำเร็จ
//     cy.wait('@createBillRequest').its('response.statusCode').should('eq', 201);
    
//     // 7. ตรวจสอบว่าบิลใหม่ปรากฏบนหน้ารายการบิล
//     cy.contains(billDescription).should('be.visible');
//     // ตรวจสอบจำนวนเงิน (อาจจะต้องปรับ selector ให้ตรงกับหน้าเว็บจริง)
//     cy.contains(billDescription)
//       .parents('.bill-card') // สมมติว่าแต่ละบิลมี class .bill-card
//       .should('contain', '1,000.00'); // ตรวจสอบ formatted currency
//   });
// });