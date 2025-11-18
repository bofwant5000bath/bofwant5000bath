// --- สร้างคำสั่ง cy.login() ---
// เราจะเปลี่ยนมาใช้ cy.session() ซึ่งเป็นวิธีที่ถูกต้องและเสถียรที่สุด
// ในการจัดการ localStorage สำหรับการล็อกอิน
Cypress.Commands.add('login', (userId = 1, fullName = 'Test User', profileUrl = null) => {
  
  // cy.session() จะ "จำ" สถานะการล็อกอินนี้ไว้
  // และจะรันโค้ดข้างใน (setup function) แค่ครั้งเดียว
  cy.session(
    // นี่คือ "คีย์" ของเซสชัน
    `user-${userId}`, 
    
    // --- นี่คือ "Setup Function" (จะรันแค่ครั้งแรก) ---
    () => {
      cy.log(`Setting up login session for ${fullName} (ID: ${userId})`);
      
      // 1. เราต้องไปที่หน้าใดหน้าหนึ่งในแอปก่อน (เช่น /login)
      //    เพื่อที่ Cypress จะได้ "โดเมน" ที่ถูกต้องสำหรับตั้ง localStorage
      cy.visit('/#/login'); 
      
      // 2. ตั้งค่า localStorage ในหน้าต่างของแอป
      cy.window().then((win) => {
        win.localStorage.setItem('user_id', `${userId}`); // (แปลงเป็น string)
        win.localStorage.setItem('full_name', fullName);
        if (profileUrl) {
          win.localStorage.setItem('profile_picture_url', profileUrl);
        }
      });

      // 3. (ไม่บังคับ) ตรวจสอบว่าตั้งค่าสำเร็จ
      cy.window().its('localStorage').invoke('getItem', 'user_id').should('eq', `${userId}`);

    }, 
    
    // --- นี่คือ "Validation Function" (จะรันทุกครั้งที่เรียก cy.login) ---
    {
      validate() {
        // ตรวจสอบเร็วๆ ว่า localStorage ยังอยู่หรือไม่
        cy.window().its('localStorage').invoke('getItem', 'user_id').should('eq', `${userId}`);
      },
      // (เปิด cacheAcrossSpecs เผื่อคุณต้องการใช้ในไฟล์เทสอื่น)
      cacheAcrossSpecs: true, 
    }
  );
});