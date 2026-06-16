-- data.sql
-- Mật khẩu cho tất cả tài khoản dưới đây là: 123456

-- 1. Tạo ADMIN
INSERT INTO users (UserID, Username, PasswordHash, FirstName, LastName, Email, PhoneNumber, Role, Status)
VALUES (1, 'admin', '$2a$10$DVlvU9hQHR6vjNY7mQQ/lOKbS01W9BBaG6ImavkSpzFspj8c/QBR2', 'Quản Trị', 'Viên', 'admin@medbook.com', '0900000001', 'QuanTriVien', 'Active');

-- 2. Tạo BÁC SĨ (dr_binh)
INSERT INTO users (UserID, Username, PasswordHash, FirstName, LastName, Email, PhoneNumber, Role, Status)
VALUES (2, 'bacsia', '$2a$10$s7MopIdasn955Q4qZ.XXk..co9bWGb6xKLfSa8fwCX6tkVQySVwmm', 'Bình', 'Nguyễn', 'dr.binh@medbook.com', '0900000002', 'BacSi', 'Active');

-- 3. Tạo NHÂN VIÊN (staff_a)
INSERT INTO users (UserID, Username, PasswordHash, FirstName, LastName, Email, PhoneNumber, Role, Status)
VALUES (3, 'staff_a', '$2a$10$s7MopIdasn955Q4qZ.XXk..co9bWGb6xKLfSa8fwCX6tkVQySVwmm', 'Hạnh', 'Trần', 'staff.a@medbook.com', '0900000003', 'NhanVien', 'Active');

-- 4. Tạo BỆNH NHÂN (benhnhan_a)
INSERT INTO users (UserID, Username, PasswordHash, FirstName, LastName, Email, PhoneNumber, Role, Status)
VALUES (4, 'benhnhan_a', '$2a$10$s7MopIdasn955Q4qZ.XXk..co9bWGb6xKLfSa8fwCX6tkVQySVwmm', 'An', 'Lê', 'patient.a@medbook.com', '0900000004', 'BenhNhan', 'Active');
