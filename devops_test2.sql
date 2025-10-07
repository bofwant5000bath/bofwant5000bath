-- สร้างฐานข้อมูลใหม่
CREATE DATABASE devops_test2;
USE devops_test2;
-- drop database devops_test2;

-- 1. ตารางผู้ใช้ (users)
CREATE TABLE `users` (
  `user_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `username` VARCHAR(255) UNIQUE NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `full_name` VARCHAR(255) NOT NULL,
  `profile_picture_url` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) CHARACTER SET utf8 COLLATE utf8_unicode_ci;

-- เพิ่มข้อมูลตัวอย่างสำหรับตาราง users
INSERT INTO `users` (`username`, `password`, `full_name`) VALUES
('keatikun', 'password_1', 'เกียรติกุล เข้มแข็ง'),
('pornpawee', 'password_2', 'พรพวีร์ พัฒนพรวิวัฒน์'),
('sutarnthip', 'password_3', 'สุฑาณทิพย์ หลวงทิพย์'),
('kan', 'password_4', 'กานต์');

-- 2. ตารางกลุ่ม (groups)
CREATE TABLE `groups_` (
  `group_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `group_name` VARCHAR(255) NOT NULL,
  `created_by_user_id` INT,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`created_by_user_id`) REFERENCES `users`(`user_id`)
) CHARACTER SET utf8 COLLATE utf8_unicode_ci;

-- เพิ่มข้อมูลตัวอย่างสำหรับตาราง groups
INSERT INTO `groups_` (`group_name`, `created_by_user_id`) VALUES
('ทริปเที่ยวทะเล', 1), -- กลุ่มนี้สร้างโดย เกียรติกุล (user_id 1)
('ค่าห้องเดือนนี้', 2); -- กลุ่มนี้สร้างโดย พรพวีร์ (user_id 2)

-- 3. ตารางสมาชิกในกลุ่ม (group_members)
CREATE TABLE `group_members` (
  `group_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  PRIMARY KEY (`group_id`, `user_id`),
  FOREIGN KEY (`group_id`) REFERENCES `groups_`(`group_id`),
  FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`)
);

-- เพิ่มข้อมูลตัวอย่างสำหรับตาราง group_members
-- สมาชิกกลุ่ม "ทริปเที่ยวทะเล" (group_id 1)
INSERT INTO `group_members` (`group_id`, `user_id`) VALUES
(1, 1), -- เกียรติกุล
(1, 2), -- พรพวีร์
(1, 3); -- สุฑาณทิพย์

-- สมาชิกกลุ่ม "ค่าห้องเดือนนี้" (group_id 2)
INSERT INTO `group_members` (`group_id`, `user_id`) VALUES
(2, 1), -- เกียรติกุล
(2, 2), -- พรพวีร์
(2, 4); -- กานต์

-- 4. ตารางบิล (bills)
CREATE TABLE `bills` (
  `bill_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `group_id` INT NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `description` TEXT,
  `amount` DECIMAL(10, 2) NOT NULL,
  `paid_by_user_id` INT NOT NULL,
  `split_method` ENUM('equal', 'unequal', 'by_tag') NOT NULL,
  `bill_date` DATETIME NOT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`group_id`) REFERENCES `groups_`(`group_id`),
  FOREIGN KEY (`paid_by_user_id`) REFERENCES `users`(`user_id`)
) CHARACTER SET utf8 COLLATE utf8_unicode_ci;

-- 5. ตารางผู้เข้าร่วมในบิล (bill_participants)
CREATE TABLE `bill_participants` (
  `bill_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `split_amount` DECIMAL(10, 2) NOT NULL,
  `is_paid` ENUM('unpaid','partial','paid') DEFAULT 'unpaid',
  PRIMARY KEY (`bill_id`, `user_id`),
  FOREIGN KEY (`bill_id`) REFERENCES `bills`(`bill_id`),
  FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`)
);


-- 6. ตารางการชำระเงิน (payments)
CREATE TABLE `payments` (
  `payment_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `bill_id` INT NOT NULL,
  `payer_user_id` INT NOT NULL,
  `amount` DECIMAL(10, 2) NOT NULL,
  `payment_date` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`bill_id`) REFERENCES `bills`(`bill_id`),
  FOREIGN KEY (`payer_user_id`) REFERENCES `users`(`user_id`)
) CHARACTER SET utf8 COLLATE utf8_unicode_ci;

--   สร้าง Trigger (ต้องอยู่หลังจากสร้าง payments และ bill_participants แล้ว)
DELIMITER $$

CREATE TRIGGER trg_after_insert_payments
AFTER INSERT ON payments
FOR EACH ROW
BEGIN
    UPDATE bill_participants bp
    JOIN (
        SELECT 
            p.bill_id,
            p.payer_user_id AS user_id,
            SUM(p.amount) AS total_paid
        FROM payments p
        WHERE p.bill_id = NEW.bill_id
          AND p.payer_user_id = NEW.payer_user_id
        GROUP BY p.bill_id, p.payer_user_id
    ) pay 
    ON bp.bill_id = pay.bill_id 
       AND bp.user_id = pay.user_id
    SET bp.is_paid = CASE
        WHEN IFNULL(pay.total_paid, 0) = 0 THEN 'unpaid'
        WHEN IFNULL(pay.total_paid, 0) >= bp.split_amount THEN 'paid'
        ELSE 'partial'
    END;
END$$

DELIMITER ;

-- equal
-- bills
INSERT INTO `bills` (`group_id`, `title`, `description`, `amount`, `paid_by_user_id`, `split_method`, `bill_date`)
VALUES (1, 'ค่าที่พัก', 'ค่าที่พักทริปเที่ยวทะเล', 4800, 1, 'equal', '2023-05-28 10:00:00');

-- bill_participants
INSERT INTO `bill_participants` (`bill_id`, `user_id`, `split_amount`, `is_paid`) VALUES
(1, 1, 1600.00, 'paid'),     -- เกียรติกุล จ่ายครบ
(1, 2, 1600.00, 'partial'),  -- พรพวีร์ จ่ายบางส่วน
(1, 3, 1600.00, 'unpaid');   -- สุฑาณทิพย์ ยังไม่จ่าย

-- payments
INSERT INTO `payments` (`bill_id`, `payer_user_id`, `amount`) VALUES
(1, 1, 1600.00),  -- เกียรติกุลจ่ายครบ
(1, 2, 600.00);   -- พรพวีร์ทยอยจ่าย

-- unequal
-- bills
INSERT INTO `bills` (`group_id`, `title`, `description`, `amount`, `paid_by_user_id`, `split_method`, `bill_date`)
VALUES (1, 'ค่าอาหารเย็น', 'ร้านอาหารทะเล', 4500, 3, 'unequal', '2023-05-28 20:00:00');

-- bill_participants
INSERT INTO `bill_participants` (`bill_id`, `user_id`, `split_amount`, `is_paid`) VALUES
(2, 1, 2500.00, 'unpaid'),   -- เกียรติกุล ยังไม่จ่าย
(2, 2, 1500.00, 'paid'),     -- พรพวีร์ จ่ายครบ
(2, 3, 500.00, 'paid');      -- สุฑาณทิพย์ จ่ายครบ

-- payments
INSERT INTO `payments` (`bill_id`, `payer_user_id`, `amount`) VALUES
(2, 2, 1500.00), -- พรพวีร์จ่ายครบ
(2, 3, 500.00);  -- สุฑาณทิพย์จ่ายครบ


-- by_tag
-- bills
INSERT INTO `bills` (`group_id`, `title`, `description`, `amount`, `paid_by_user_id`, `split_method`, `bill_date`)
VALUES (2, 'ค่าไฟฟ้า', 'ค่าไฟฟ้าห้องเช่า แบ่งตามการใช้จริง', 1200, 4, 'by_tag', '2023-06-01 18:00:00');

-- bill_participants
INSERT INTO `bill_participants` (`bill_id`, `user_id`, `split_amount`, `is_paid`) VALUES
(3, 1, 600.00, 'partial'),   -- เกียรติกุล จ่ายบางส่วน
(3, 2, 400.00, 'unpaid'),    -- พรพวีร์ ยังไม่จ่าย
(3, 4, 200.00, 'paid');      -- กานต์ จ่ายครบ

-- payments
INSERT INTO `payments` (`bill_id`, `payer_user_id`, `amount`) VALUES
(3, 1, 300.00), -- เกียรติกุลจ่ายไปก่อนครึ่งนึง
(3, 4, 200.00); -- กานต์จ่ายครบ


-- คำสั่ง SELECT เพื่อตรวจสอบข้อมูลในแต่ละตาราง
SELECT * FROM users;
SELECT * FROM groups_;
SELECT * FROM group_members;
SELECT * FROM bills;
SELECT * FROM bill_participants;
SELECT * FROM payments;


-- test Trigger 
-- INSERT INTO `payments` (`bill_id`, `payer_user_id`, `amount`) VALUES
-- (1, 3, 300.00);

-- INSERT INTO `payments` (`bill_id`, `payer_user_id`, `amount`) VALUES
-- (1, 3, 1300.00);

-- INSERT INTO `payments` (`bill_id`, `payer_user_id`, `amount`) VALUES
-- (3, 2, 200.00);