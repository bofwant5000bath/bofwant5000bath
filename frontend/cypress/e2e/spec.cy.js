// // cypress/e2e/spec.cy.js

// describe('Authentication Flow', () => {

//   // เพิ่มเข้ามาเพื่อล้างสถานะ (Cookies, Local Storage) ของแอปพลิเคชัน
//   // ก่อนเริ่มเทสทุกครั้ง ป้องกันปัญหาข้อมูลเก่าค้างระหว่างเทส
//   beforeEach(() => {
//     cy.clearCookies();
//     cy.clearLocalStorage();
//   });

//   // Test Case 1: การลงทะเบียนผู้ใช้ใหม่ (Success Case)
//   it('ควรจะอนุญาตให้ผู้ใช้ใหม่ลงทะเบียนได้สำเร็จ และ redirect ไปยังหน้า login', () => {
//     // 1. เข้าไปที่หน้า Register
//     cy.visit('/register#/register');

//     // 2. กรอกข้อมูลในฟอร์ม
//     cy.get('input[name="full_name"]').type('Test User E2E');
//     cy.get('input[name="username"]').type('testuser_e2e');
//     cy.get('input[name="password"]').type('password123');
//     cy.get('input[name="confirm_password"]').type('password123');
    
//     // 3. คลิกปุ่มลงทะเบียน
//     cy.get('button[type="submit"]').contains('ลงทะเบียน').click();

//     // 4. ตรวจสอบผลลัพธ์
//     cy.url().should('include', '/login#/login');
//     cy.contains('การลงทะเบียนสำเร็จ').should('be.visible');
//   });


//   // Test Case 2: การ Login ของผู้ใช้ที่ลงทะเบียนแล้ว (Success Case)
//   it('ควรจะอนุญาตให้ผู้ใช้ที่ลงทะเบียนแล้วเข้าระบบได้ และ redirect ไปยังหน้า dashboard', () => {
//     // 1. เข้าไปที่หน้า Login
//     cy.visit('/');

//     // 2. กรอกข้อมูล Username และ Password ที่ถูกต้อง
//     cy.get('input[name="username"]').type('keatikun');
//     cy.get('input[name="password"]').type('password_1');

//     // 3. คลิกปุ่มเข้าสู่ระบบ
//     cy.get('button[type="submit"]').contains('เข้าสู่ระบบ').click();

//     // 4. ตรวจสอบผลลัพธ์
//     cy.url().should('include', '/dashboard');
//     // cy.url().should('match', /\/dashboard\/\d+$/);
//     cy.contains('keatikun').should('be.visible');
//   });
  
//   // Test Case 3: การ Login ด้วยรหัสผ่านที่ผิด (Failure Case)
//   it('ควรจะแสดงข้อความผิดพลาดเมื่อใส่ข้อมูลเข้าระบบไม่ถูกต้อง', () => {
//     // 1. เข้าไปที่หน้า Login
//     cy.visit('/');

//     // 2. กรอกข้อมูลด้วยรหัสผ่านที่ผิด
//     cy.get('input[name="username"]').type('testuser_e2e');
//     cy.get('input[name="password"]').type('wrongpassword');

//     // 3. คลิกปุ่มเข้าสู่ระบบ
//     cy.get('button[type="submit"]').contains('เข้าสู่ระบบ').click();

//     // 4. ตรวจสอบผลลัพธ์
//     cy.url().should('include', '/');
    
//     // แก้ไขข้อความ Error ให้ตรงกับหน้าเว็บจริง
//     cy.contains('ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง').should('be.visible');
//   });

// });

// describe('Authentication Flow', () => {

//   // This runs before each test in this block.
//   // It's great for clearing state to ensure tests are isolated.
//   beforeEach(() => {
//     cy.clearCookies();
//     cy.clearLocalStorage();
//   });

//   // Test Case 1: Successful registration and immediate login (The "Happy Path")
//   // This combines your first two tests into one reliable flow.
//   it('should allow a new user to register and then log in successfully', () => {
//     // ✨ Use a unique username for each test run to prevent "user already exists" errors.
//     const uniqueUsername = `testuser_${Date.now()}`;
//     const password = 'password123';

//     // --- Registration Step ---
//     // 1. Visit the register page
//     cy.visit('/#/register'); 

//     cy.intercept('POST', '**/api/auth/register').as('registerRequest');

//     // 2. Fill out the registration form with unique data
//     cy.get('input[name="name"]').type('Test User E2E');
//     cy.get('input[name="username"]').type(uniqueUsername);
//     cy.get('input[name="password"]').type(password);
//     cy.get('input[id="confirm-password"]').type(password);

//     // 3. Click the register button
//     cy.get('button[type="submit"]').contains('ลงทะเบียน').click();

//     // ✨ Wait for the registration API call to finish before proceeding.
//     cy.wait('@registerRequest').its('response.statusCode').should('be.oneOf', [200, 201]);

//     // --- Login Step ---
//     cy.intercept('POST', '**/api/auth/login').as('loginRequest');
    
//     // ✨ FIX: Intercept the dashboard's data request as well.
//     // This ensures the test waits for the page to be fully populated.
//     cy.intercept('GET', '**/api/groups/dashboard/**').as('dashboardRequest');

//     // 4. The login form should now be visible.
//     cy.get('input[name="username"]').should('be.visible');
    
//     // 5. Log in with the user we just created
//     cy.get('input[name="username"]').type(uniqueUsername);
//     cy.get('input[name="password"]').type(password);
    
//     cy.get('button[type="submit"]').contains('เข้าสู่ระบบ').should('be.visible').and('not.be.disabled').click();

//     // Wait for the login request to finish.
//     cy.wait('@loginRequest').its('response.statusCode').should('eq', 200);
    
//     // Now wait for the dashboard data to load.
//     cy.wait('@dashboardRequest');

//     // 6. Assert that the login was successful and we are on the dashboard
//     cy.url().should('include', '/#/dashboard');
//     // cy.contains(uniqueUsername).should('be.visible');
//   });

//   // Test Case 2: Failed login with incorrect credentials
//   it('should display an error message for incorrect login credentials', () => {
//     // 1. Visit the login page
//     cy.visit('/#/login');

//     // 2. Enter valid username but an incorrect password
//     cy.get('input[name="username"]').type('testuser_e2e'); // Can be any user
//     cy.get('input[name="password"]').type('wrongpassword');

//     // 3. Click the login button
//     cy.get('button[type="submit"]').contains('เข้าสู่ระบบ').click();

//     // 4. Assert that we are still on the login page and the error is shown
//     cy.url().should('not.include', '/dashboard');
//     cy.contains('Invalid credentials').should('be.visible');
//   });

// });

describe('Authentication Flow', () => {

  // This runs before each test in this block.
  // It's great for clearing state to ensure tests are isolated.
  beforeEach(() => {
    cy.clearCookies();
    cy.clearLocalStorage();
  });

  // Test Case 1: Successful registration and immediate login (The "Happy Path")
  // This combines your first two tests into one reliable flow.
  it('should allow a new user to register and then log in successfully', () => {
    // ✨ Use unique/constant variables for test data
    const uniqueUsername = `testuser_${Date.now()}`;
    const password = 'password123';
    const fullName = 'Test User E2E'; // ✨ Use a variable for the full name

    // --- Registration Step ---
    // 1. Visit the register page
    cy.visit('/#/register'); 

    cy.intercept('POST', '**/api/auth/register').as('registerRequest');

    // 2. Fill out the registration form with unique data
    cy.get('input[name="name"]').type(fullName); // Use the variable here
    cy.get('input[name="username"]').type(uniqueUsername);
    cy.get('input[name="password"]').type(password);
    cy.get('input[id="confirm-password"]').type(password);

    // 3. Click the register button
    cy.get('button[type="submit"]').contains('ลงทะเบียน').click();

    // ✨ Wait for the registration API call to finish before proceeding.
    cy.wait('@registerRequest').its('response.statusCode').should('be.oneOf', [200, 201]);

    // --- Login Step ---
    cy.intercept('POST', '**/api/auth/login').as('loginRequest');
    
    cy.intercept('GET', '**/api/groups/dashboard/**').as('dashboardRequest');

    // 4. The login form should now be visible.
    cy.get('input[name="username"]').should('be.visible');
    
    // 5. Log in with the user we just created
    cy.get('input[name="username"]').type(uniqueUsername);
    cy.get('input[name="password"]').type(password);
    
    cy.get('button[type="submit"]').contains('เข้าสู่ระบบ').should('be.visible').and('not.be.disabled').click();

    // Wait for the login request to finish.
    cy.wait('@loginRequest').its('response.statusCode').should('eq', 200);
    
    // Now wait for the dashboard data to load.
    cy.wait('@dashboardRequest');

    // 6. Assert that the login was successful and we are on the dashboard
    cy.url().should('include', '/#/dashboard');
    
    // ✨ FINAL FIX: Assert that the FULL NAME is visible on the dashboard, not the username.
    cy.contains(fullName, { timeout: 10000 }).should('be.visible');
  });

  // Test Case 2: Failed login with incorrect credentials
  it('should display an error message for incorrect login credentials', () => {
    // 1. Visit the login page
    cy.visit('/#/login');

    // 2. Enter valid username but an incorrect password
    cy.get('input[name="username"]').type('testuser_e2e'); // Can be any user
    cy.get('input[name="password"]').type('wrongpassword');

    // 3. Click the login button
    cy.get('button[type="submit"]').contains('เข้าสู่ระบบ').click();

    // 4. Assert that we are still on the login page and the error is shown
    cy.url().should('not.include', '/dashboard');
    cy.contains('Invalid credentials').should('be.visible');
  });

});

