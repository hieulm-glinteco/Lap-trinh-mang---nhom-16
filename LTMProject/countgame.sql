-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Máy chủ: 127.0.0.1
-- Thời gian đã tạo: Th10 16, 2025 lúc 09:51 AM
-- Phiên bản máy phục vụ: 10.4.32-MariaDB
-- Phiên bản PHP: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Cơ sở dữ liệu: `countgame`
--

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `gameround`
--

CREATE TABLE `gameround` (
  `id` int(11) NOT NULL,
  `gameSessionId` int(11) DEFAULT NULL,
  `imageId` int(11) DEFAULT NULL,
  `roundNumber` int(11) DEFAULT NULL,
  `winner` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `gamesession`
--

CREATE TABLE `gamesession` (
  `id` int(11) NOT NULL,
  `playerid1` int(11) NOT NULL,
  `playerid2` int(11) NOT NULL,
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `playerscore1` int(11) DEFAULT 0,
  `playerscore2` int(11) DEFAULT 0,
  `winner` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `gamesession`
--

INSERT INTO `gamesession` (`id`, `playerid1`, `playerid2`, `start_time`, `end_time`, `playerscore1`, `playerscore2`, `winner`) VALUES
(2, 3, 4, '2025-10-02 14:00:00', '2025-10-02 14:20:00', 6, 8, 4),
(3, 5, 6, '2025-10-03 09:30:00', '2025-10-03 09:50:00', 9, 7, 5),
(4, 7, 8, '2025-10-04 19:00:00', '2025-10-04 19:15:00', 5, 10, 8),
(5, 9, 10, '2025-10-05 13:00:00', '2025-10-05 13:30:00', 6, 9, 10),
(7, 2, 5, '2025-10-07 08:45:00', '2025-10-07 09:05:00', 7, 9, 5),
(8, 4, 9, '2025-10-08 15:30:00', '2025-10-08 15:55:00', 10, 6, 4),
(9, 6, 10, '2025-10-09 16:00:00', '2025-10-09 16:25:00', 4, 10, 10),
(33, 1, 12, '2025-11-14 09:03:50', '2025-11-14 09:03:50', 0, 0, 1),
(34, 12, 1, '2025-11-15 14:39:05', '2025-11-15 14:39:05', 10, 0, 12),
(35, 12, 1, '2025-11-15 14:39:39', '2025-11-15 14:39:39', 0, 0, 12),
(36, 1, 12, '2025-11-15 14:47:22', '2025-11-15 14:47:22', 0, 0, 1),
(37, 1, 12, '2025-11-15 14:53:37', '2025-11-15 14:53:37', 0, 0, 12),
(38, 1, 12, '2025-11-15 14:54:34', '2025-11-15 14:54:34', 0, 0, 0),
(39, 12, 1, '2025-11-16 15:26:25', '2025-11-16 15:26:25', 0, 10, 1),
(40, 1, 12, '2025-11-16 15:31:04', '2025-11-16 15:31:04', 50, 0, 1),
(41, 1, 12, '2025-11-16 15:31:53', '2025-11-16 15:31:53', 0, 0, 1),
(42, 12, 1, '2025-11-16 15:35:21', '2025-11-16 15:35:21', 0, 0, 12),
(43, 12, 1, '2025-11-16 15:35:55', '2025-11-16 15:35:55', 0, 0, 12),
(44, 12, 1, '2025-11-16 15:39:48', '2025-11-16 15:39:48', 0, 0, 12),
(45, 12, 1, '2025-11-16 15:40:05', '2025-11-16 15:40:05', 0, 0, 1),
(46, 1, 12, '2025-11-16 15:42:23', '2025-11-16 15:42:23', 0, 0, 12),
(47, 12, 1, '2025-11-16 15:44:54', '2025-11-16 15:44:54', 20, 30, 1);

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `images`
--

CREATE TABLE `images` (
  `id` int(11) NOT NULL,
  `number1` int(11) DEFAULT NULL,
  `number2` int(11) DEFAULT NULL,
  `number3` int(11) DEFAULT NULL,
  `filepath` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `images`
--

INSERT INTO `images` (`id`, `number1`, `number2`, `number3`, `filepath`) VALUES
(1, 11, 15, 13, 'images/pic_1.png'),
(2, 15, 10, 12, 'images/pic_2.png'),
(3, 8, 12, 11, 'images/pic_3.png'),
(4, 11, 8, 6, 'images/pic_4.png'),
(5, 12, 9, 5, 'images/pic_5.png'),
(6, 19, 11, 14, 'images/pic_6.png'),
(7, 16, 12, 13, 'images/pic_7.png'),
(8, 11, 11, 9, 'images/pic_8.png'),
(9, 10, 8, 6, 'images/pic_9.png'),
(10, 9, 14, 11, 'images/pic_10.png');

-- --------------------------------------------------------

--
-- Cấu trúc bảng cho bảng `player`
--

CREATE TABLE `player` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `totalRankScore` int(11) DEFAULT 0,
  `dob` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Đang đổ dữ liệu cho bảng `player`
--

INSERT INTO `player` (`id`, `name`, `username`, `password`, `email`, `phone`, `totalRankScore`, `dob`) VALUES
(1, 'hung', 'hung', '123456', 'manhung@gmail.com', '0377207108', 19, '2025-09-30'),
(2, 'Nguyen Van A', 'nva', '123456', 'a@gmail.com', '0901111111', 1200, '2000-01-01'),
(3, 'Tran Thi B', 'ttb', '123456', 'b@gmail.com', '0902222222', 980, '2002-05-10'),
(4, 'Le Van C', 'lvc', '123456', 'c@gmail.com', '0903333333', 850, '1999-12-20'),
(5, 'Pham Thi D', 'ptd', '123456', 'd@gmail.com', '0904444444', 1050, '2001-04-15'),
(6, 'Hoang Van E', 'hve', '123456', 'e@gmail.com', '0905555555', 1150, '1998-09-09'),
(7, 'Do Thi F', 'dtf', '123456', 'f@gmail.com', '0906666666', 920, '2003-06-25'),
(8, 'Nguyen Van G', 'nvg', '123456', 'g@gmail.com', '0907777777', 1100, '2000-02-02'),
(9, 'Tran Van H', 'tvh', '123456', 'h@gmail.com', '0908888888', 990, '2001-08-18'),
(10, 'Le Thi I', 'lti', '123456', 'i@gmail.com', '0909999999', 870, '1997-03-12'),
(11, 'Pham Van K', 'pvk', '123456', 'k@gmail.com', '0910000000', 1300, '1999-11-11'),
(12, 'hieu', 'hieu', '123456', 'hieu@gmail.com', '01234132070', 22, '2025-10-02');

--
-- Chỉ mục cho các bảng đã đổ
--

--
-- Chỉ mục cho bảng `gameround`
--
ALTER TABLE `gameround`
  ADD PRIMARY KEY (`id`),
  ADD KEY `gameSessionId` (`gameSessionId`),
  ADD KEY `imageId` (`imageId`);

--
-- Chỉ mục cho bảng `gamesession`
--
ALTER TABLE `gamesession`
  ADD PRIMARY KEY (`id`),
  ADD KEY `playerid1` (`playerid1`),
  ADD KEY `playerid2` (`playerid2`);

--
-- Chỉ mục cho bảng `images`
--
ALTER TABLE `images`
  ADD PRIMARY KEY (`id`);

--
-- Chỉ mục cho bảng `player`
--
ALTER TABLE `player`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT cho các bảng đã đổ
--

--
-- AUTO_INCREMENT cho bảng `gameround`
--
ALTER TABLE `gameround`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT cho bảng `gamesession`
--
ALTER TABLE `gamesession`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=48;

--
-- AUTO_INCREMENT cho bảng `images`
--
ALTER TABLE `images`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT cho bảng `player`
--
ALTER TABLE `player`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- Các ràng buộc cho các bảng đã đổ
--

--
-- Các ràng buộc cho bảng `gameround`
--
ALTER TABLE `gameround`
  ADD CONSTRAINT `gameround_ibfk_1` FOREIGN KEY (`gameSessionId`) REFERENCES `gamesession` (`id`),
  ADD CONSTRAINT `gameround_ibfk_2` FOREIGN KEY (`imageId`) REFERENCES `images` (`id`);

--
-- Các ràng buộc cho bảng `gamesession`
--
ALTER TABLE `gamesession`
  ADD CONSTRAINT `gamesession_ibfk_1` FOREIGN KEY (`playerid1`) REFERENCES `player` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `gamesession_ibfk_2` FOREIGN KEY (`playerid2`) REFERENCES `player` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
