-- สร้างฐานข้อมูลใหม่
CREATE DATABASE IF NOT EXISTS devops_test2;
USE devops_test2;

-- 1. ตารางผู้ใช้ (users)
CREATE TABLE IF NOT EXISTS `users` (
  `user_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `username` VARCHAR(255) UNIQUE NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `full_name` VARCHAR(255) NOT NULL,
  `profile_picture_url` VARCHAR(255) DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- เพิ่มข้อมูลตัวอย่างสำหรับตาราง users
-- INSERT INTO `users` (`username`, `password`, `full_name`) VALUES
-- ('keatikun', 'password_1', 'อิอ๋อง'),
-- ('pornpawee', 'password_2', 'pornpawee'),
-- ('sutarnthip', 'password_3', 'sutarnthip'),
-- ('kan', 'password_4', 'kan');
INSERT INTO `users` (`username`, `password`, `full_name`, `profile_picture_url`) VALUES
('Rengoku', 'password_1', 'เคียวจูโร่ เรนโงคุ', 'https://fbi.dek-d.com/27/0825/5697/130438191?v=7.02'),
('Tomioka', 'password_2', 'กิยู โทมิโอกะ', 'https://hinaboshi.com/rup/rupprakopwalidet/2649902718361816.jpg'),
('Kamado', 'password_3', 'ทันจิโร่ คามาโดะ', 'https://cdn-th.tunwalai.net/files/story/570050/637619372453962081-story.jpg'),
('Agatsuma', 'password_4', 'เซนิตสึ อากัทสึมะ', 'https://fbi.dek-d.com/27/0902/9177/131649897?v=7.02');

-- 2. ตารางกลุ่ม (groups)
CREATE TABLE IF NOT EXISTS `groups_` (
  `group_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `group_name` VARCHAR(255) NOT NULL,
  `created_by_user_id` INT,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`created_by_user_id`) REFERENCES `users`(`user_id`)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- เพิ่มข้อมูลตัวอย่างสำหรับตาราง groups
INSERT INTO `groups_` (`group_name`, `created_by_user_id`) VALUES
('ทริปเที่ยวทะเล', 1), 
('ค่าห้องเดือนนี้', 2);


-- 3. ตารางสมาชิกในกลุ่ม (group_members)
CREATE TABLE IF NOT EXISTS `group_members` (
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
CREATE TABLE IF NOT EXISTS `bills` (
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
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 5. ตารางผู้เข้าร่วมในบิล (bill_participants)
CREATE TABLE IF NOT EXISTS `bill_participants` (
  `bill_id` INT NOT NULL,
  `user_id` INT NOT NULL,
  `split_amount` DECIMAL(10, 2) NOT NULL,
  `is_paid` ENUM('unpaid','partial','paid') DEFAULT 'unpaid',
  PRIMARY KEY (`bill_id`, `user_id`),
  FOREIGN KEY (`bill_id`) REFERENCES `bills`(`bill_id`),
  FOREIGN KEY (`user_id`) REFERENCES `users`(`user_id`)
);


-- 6. ตารางการชำระเงิน (payments)
CREATE TABLE IF NOT EXISTS `payments` (
  `payment_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `bill_id` INT NOT NULL,
  `payer_user_id` INT NOT NULL,
  `amount` DECIMAL(10, 2) NOT NULL,
  `payment_date` DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`bill_id`) REFERENCES `bills`(`bill_id`),
  FOREIGN KEY (`payer_user_id`) REFERENCES `users`(`user_id`)
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- สร้าง Trigger
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