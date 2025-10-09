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
//     // ✨ Use unique/constant variables for test data
//     const uniqueUsername = `testuser_${Date.now()}`;
//     const password = 'password123';
//     const fullName = 'Test User E2E'; // ✨ Use a variable for the full name

//     // --- Registration Step ---
//     // 1. Visit the register page
//     cy.visit('/#/register'); 

//     cy.intercept('POST', '**/api/auth/register').as('registerRequest');

//     // 2. Fill out the registration form with unique data
//     cy.get('input[name="name"]').type(fullName); // Use the variable here
//     cy.get('input[name="username"]').type(uniqueUsername);
//     cy.get('input[name="password"]').type(password);
//     cy.get('input[id="confirm-password"]').type(password);

//     // 3. Click the register button
//     cy.get('button[type="submit"]').contains('ลงทะเบียน').click();

//     // ✨ Wait for the registration API call to finish before proceeding.
//     cy.wait('@registerRequest').its('response.statusCode').should('be.oneOf', [200, 201]);

//     // --- Login Step ---
//     cy.intercept('POST', '**/api/auth/login').as('loginRequest');
    
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
    
//     // ✨ FINAL FIX: Assert that the FULL NAME is visible on the dashboard, not the username.
//     cy.contains(fullName, { timeout: 10000 }).should('be.visible');
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
// ตัดตรงนี้555555555555555555555555555555555555555
// cypress/e2e/spec.cy.js

// describe('Authentication Flow', () => {

//   // This runs before each test in this block to ensure a clean state.
//   beforeEach(() => {
//     cy.clearCookies();
//     cy.clearLocalStorage();
//   });

//   // Test Case 1: The "Happy Path" for a new user.
//   // This test covers both successful registration and the subsequent login.
//   it('should allow a new user to register and then log in successfully', () => {
//     // --- Test Data ---
//     // Using a dynamic username ensures the test won't fail on re-runs
//     // due to the user already existing.
//     const uniqueUsername = `testuser_${Date.now()}`;
//     const password = 'password123';
//     const fullName = 'Test User E2E';

//     // --- Intercept API Calls ---
//     // Intercepting network requests is key to preventing flaky tests.
//     // We tell Cypress to "watch" for these calls to happen.
//     cy.intercept('POST', '**/api/auth/register').as('registerRequest');
//     cy.intercept('POST', '**/api/auth/login').as('loginRequest');
//     cy.intercept('GET', '**/api/groups/dashboard/**').as('dashboardRequest');

//     // --- Registration Step ---
//     // 1. Visit the register page
//     cy.visit('/#/register'); 

//     // 2. Fill out the registration form with our test data
//     cy.get('input[name="name"]').type(fullName);
//     cy.get('input[name="username"]').type(uniqueUsername);
//     cy.get('input[name="password"]').type(password);
//     cy.get('input[id="confirm-password"]').type(password);

//     // 3. Click the register button
//     cy.get('button[type="submit"]').contains('ลงทะเบียน').click();

//     // 4. Wait for the registration API call to complete successfully
//     cy.wait('@registerRequest').its('response.statusCode').should('be.oneOf', [200, 201]);

//     // --- Login Step ---
//     // After registration, the app should navigate to the login page.
    
//     // 5. Log in with the new user's credentials
//     cy.get('input[name="username"]').type(uniqueUsername);
//     cy.get('input[name="password"]').type(password);
//     cy.get('button[type="submit"]').contains('เข้าสู่ระบบ').click();

//     // 6. Wait for both login and dashboard data loading to complete
//     cy.wait('@loginRequest').its('response.statusCode').should('eq', 200);
//     cy.wait('@dashboardRequest');

//     // --- Assertion Step ---
//     // 7. Verify we are on the dashboard and the correct user's data is displayed
//     cy.url().should('include', '/#/dashboard');
    
//     // FINAL FIX: The dashboard displays the Full Name, not the username.
//     // This assertion now correctly checks for the full name.
//     cy.contains(fullName, { timeout: 10000 }).should('be.visible');
//   });

//   // Test Case 2: Failed login with incorrect credentials.
//   it('should display an error message for incorrect login credentials', () => {
//     // 1. Visit the login page
//     cy.visit('/#/login');

//     // 2. Enter an incorrect password
//     cy.get('input[name="username"]').type('keatikun'); // A valid user
//     cy.get('input[name="password"]').type('wrongpassword');

//     // 3. Click the login button
//     cy.get('button[type="submit"]').contains('เข้าสู่ระบบ').click();

//     // 4. Assert that we are still on the login page and the correct error is shown.
//     // The error message "Invalid credentials" comes directly from the backend.
//     cy.url().should('not.include', '/dashboard');
//     cy.contains('Invalid credentials').should('be.visible');
//   });

// });

// cypress/e2e/spec.cy.js

describe('ขั้นตอนการยืนยันตัวตน (Authentication Flow)', () => {

  // ฟังก์ชันนี้จะรันก่อนทุกๆ เทสเพื่อล้างสถานะ ทำให้แต่ละเทสไม่ขึ้นต่อกัน
  beforeEach(() => {
    cy.clearCookies();
    cy.clearLocalStorage();
  });

  // Test Case 1: Happy Path สำหรับผู้ใช้ใหม่
  it('ควรให้ผู้ใช้ใหม่ลงทะเบียนและล็อกอินเข้าสู่ระบบได้สำเร็จ', () => {
    // --- ข้อมูลสำหรับเทส ---
    const uniqueUsername = `testuser_${Date.now()}`;
    const password = 'password123';
    const fullName = 'Test User E2E';

    // --- ดักจับ API Calls ---
    cy.intercept('POST', '**/api/auth/register').as('registerRequest');
    cy.intercept('POST', '**/api/auth/login').as('loginRequest');
    cy.intercept('GET', '**/api/groups/dashboard/**').as('dashboardRequest');

    // --- ขั้นตอนการลงทะเบียน ---
    cy.visit('/#/register'); 
    cy.get('input[name="name"]').type(fullName);
    cy.get('input[name="username"]').type(uniqueUsername);
    cy.get('input[name="password"]').type(password);
    cy.get('input[id="confirm-password"]').type(password);
    cy.get('button[type="submit"]').contains('ลงทะเบียน').click();

    // รอให้ API การลงทะเบียนทำงานเสร็จ และตรวจสอบว่า Backend ตอบกลับมาด้วยสถานะ "สำเร็จ"
    cy.wait('@registerRequest').its('response.statusCode').should('be.oneOf', [200, 201]);

    // --- ขั้นตอนการล็อกอิน ---
    cy.get('input[name="username"]').type(uniqueUsername);
    cy.get('input[name="password"]').type(password);
    cy.get('button[type="submit"]').contains('เข้าสู่ระบบ').click();
    
    // รอให้ API การล็อกอิน และ API โหลดข้อมูล Dashboard ทำงานเสร็จ
    cy.wait('@loginRequest').its('response.statusCode').should('eq', 200);
    cy.wait('@dashboardRequest');

    // --- ขั้นตอนการตรวจสอบ (Assertion) ---
    cy.url().should('include', '/#/dashboard');
    // ตรวจสอบว่า "ชื่อ-นามสกุล" ถูกแสดงบนหน้าจอตามที่คาดหวัง
    cy.contains(fullName, { timeout: 10000 }).should('be.visible');
  });

  // Test Case 2: กรณีล็อกอินด้วยข้อมูลที่ไม่ถูกต้อง
  it('ควรแสดงข้อความข้อผิดพลาดเมื่อล็อกอินด้วยข้อมูลที่ไม่ถูกต้อง', () => {
    cy.visit('/#/login');
    cy.get('input[name="username"]').type('keatikun');
    cy.get('input[name="password"]').type('wrongpassword');
    cy.get('button[type="submit"]').contains('เข้าสู่ระบบ').click();
    
    // ตรวจสอบว่ายังอยู่ที่หน้าเดิม และแสดงข้อความ Error จาก Backend
    cy.url().should('not.include', '/dashboard');
    cy.contains('Invalid credentials').should('be.visible');
  });

});